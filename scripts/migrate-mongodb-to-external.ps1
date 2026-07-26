param(
    [string]$EnvFile = (Join-Path $PSScriptRoot '..\.env')
)

$ErrorActionPreference = 'Stop'
$backendRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$envPath = (Resolve-Path -LiteralPath $EnvFile).Path
$uriLine = Get-Content -LiteralPath $envPath | Where-Object { $_ -match '^MONGODB_URI=' } | Select-Object -First 1
if (-not $uriLine) {
    throw 'Không tìm thấy MONGODB_URI trong D:\backend\.env'
}

$externalUri = $uriLine.Substring('MONGODB_URI='.Length).Trim()
if ($externalUri -notmatch '^mongodb(\+srv)?://' -or $externalUri -match 'localhost|127\.0\.0\.1|@?mongodb:27017') {
    throw 'MONGODB_URI phải trỏ tới MongoDB bên ngoài, không phải MongoDB local/Docker.'
}
if ($externalUri -notmatch '/PulseTech(?:\?|$)') {
    throw 'URI đích phải chọn database PulseTech.'
}

$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$archiveName = "pulsetech-$timestamp.archive.gz"
$containerArchive = "/tmp/$archiveName"
$backupDir = Join-Path $backendRoot 'backups'
$archivePath = Join-Path $backupDir $archiveName
[IO.Directory]::CreateDirectory($backupDir) | Out-Null

Write-Host 'Đang tạo backup BSON từ MongoDB Docker local...'
docker compose -f (Join-Path $backendRoot 'docker-compose.yml') exec -T mongodb mongodump `
    --uri=mongodb://localhost:27017/PulseTech `
    --archive=$containerArchive --gzip
if ($LASTEXITCODE -ne 0) { throw 'mongodump thất bại.' }

docker compose -f (Join-Path $backendRoot 'docker-compose.yml') cp "mongodb:$containerArchive" $archivePath
if ($LASTEXITCODE -ne 0) { throw 'Không thể sao chép file backup ra máy host.' }

Write-Host 'Đang kiểm tra kết nối và restore vào MongoDB bên ngoài...'
$env:TARGET_MONGODB_URI = $externalUri
$env:ARCHIVE_NAME = $archiveName
try {
    docker run --rm `
        --env TARGET_MONGODB_URI `
        --env ARCHIVE_NAME `
        --mount "type=bind,source=$backupDir,target=/backup,readonly" `
        mongo:8.0.26 sh -c `
        'mongorestore --uri="$TARGET_MONGODB_URI" --archive="/backup/$ARCHIVE_NAME" --gzip --nsInclude="PulseTech.*" --stopOnError'
    if ($LASTEXITCODE -ne 0) {
        throw "Restore thất bại. Backup vẫn được giữ tại $archivePath"
    }
}
finally {
    Remove-Item Env:TARGET_MONGODB_URI -ErrorAction SilentlyContinue
    Remove-Item Env:ARCHIVE_NAME -ErrorAction SilentlyContinue
}

Write-Host 'Restore thành công. Đang chuyển các service sang MongoDB bên ngoài...'
docker compose -f (Join-Path $backendRoot 'docker-compose.yml') up -d --force-recreate auth-service product-service order-service
if ($LASTEXITCODE -ne 0) { throw 'Dữ liệu đã restore nhưng không thể khởi động lại service.' }

Write-Host "Hoàn tất. Backup an toàn: $archivePath"