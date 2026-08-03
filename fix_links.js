db = db.getSiblingDB('PulseTech');
db.banners.updateOne({_id: 'ban-1'}, {$set: {link: '/products/samsung-galaxy-s24-ultra'}});
db.banners.updateOne({_id: 'ban-2'}, {$set: {link: '/products/iphone-15-pro-max'}});
