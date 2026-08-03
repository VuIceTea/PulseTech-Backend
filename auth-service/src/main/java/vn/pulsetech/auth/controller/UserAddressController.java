package vn.pulsetech.auth.controller;

import org.springframework.web.bind.annotation.*;
import vn.pulsetech.auth.domain.UserAddress;
import vn.pulsetech.auth.repository.UserAddressRepository;
import java.util.List;

@RestController
@RequestMapping("/api/users/addresses")
public class UserAddressController {
    private final UserAddressRepository userAddressRepository;

    public UserAddressController(UserAddressRepository userAddressRepository) {
        this.userAddressRepository = userAddressRepository;
    }

    @GetMapping
    public List<UserAddress> getAddresses(@RequestParam String userId) {
        return userAddressRepository.findByUserId(userId);
    }

    @PostMapping
    public UserAddress addAddress(@RequestBody UserAddress address) {
        return userAddressRepository.save(address);
    }
}
