package vn.pulsetech.order.service;

import org.springframework.stereotype.Service;
import vn.pulsetech.order.domain.Cart;
import vn.pulsetech.order.domain.CartItem;
import vn.pulsetech.order.repository.CartRepository;

import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCart(String userId) {
        return cartRepository.findById(userId).orElseGet(() -> new Cart(userId));
    }

    public Cart updateCart(String userId, CartItem item) {
        Cart cart = getCart(userId);
        
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getId().equals(item.getId()) 
                          && i.getColor().equals(item.getColor()) 
                          && i.getStorage().equals(item.getStorage()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(item.getQuantity());
        } else {
            cart.getItems().add(item);
        }
        
        return cartRepository.save(cart);
    }

    public Cart removeItem(String userId, String productId, String color, String storage) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(i -> i.getId().equals(productId) 
                                   && i.getColor().equals(color) 
                                   && i.getStorage().equals(storage));
        return cartRepository.save(cart);
    }

    public Cart clearCart(String userId) {
        Cart cart = getCart(userId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    public Cart mergeCarts(String guestId, String userId) {
        Cart guestCart = cartRepository.findById(guestId).orElse(null);
        Cart userCart = getCart(userId);

        if (guestCart != null && !guestCart.getItems().isEmpty()) {
            for (CartItem guestItem : guestCart.getItems()) {
                Optional<CartItem> existingUserItem = userCart.getItems().stream()
                        .filter(i -> i.getId().equals(guestItem.getId()) 
                                  && i.getColor().equals(guestItem.getColor()) 
                                  && i.getStorage().equals(guestItem.getStorage()))
                        .findFirst();

                if (existingUserItem.isPresent()) {
                    existingUserItem.get().setQuantity(
                            Math.max(existingUserItem.get().getQuantity(), guestItem.getQuantity())
                    );
                } else {
                    userCart.getItems().add(guestItem);
                }
            }
            cartRepository.save(userCart);
            
            guestCart.getItems().clear();
            cartRepository.save(guestCart);
        }
        
        return userCart;
    }
}
