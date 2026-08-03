db = db.getSiblingDB('PulseTech');
db.banners.updateOne({_id: 'ban-1'}, {$set: {imageUrl: '/hot-sale/samsung-galaxy-s24-ultra.png', subtitle: 'Quyền năng Galaxy AI trong tay bạn', promoText: 'Giảm ngay 7 triệu', bgColor: 'from-blue-950 to-indigo-950'}});
db.banners.updateOne({_id: 'ban-2'}, {$set: {imageUrl: '/hot-sale/iphone-15-pro-max.png', subtitle: 'Titan cực đỉnh - Hiệu năng vượt trội', promoText: 'Trợ giá lên đời đến 2 triệu', bgColor: 'from-zinc-900 to-zinc-800'}});
