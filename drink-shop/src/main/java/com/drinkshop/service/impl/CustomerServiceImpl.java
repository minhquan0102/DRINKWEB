package com.drinkshop.service.impl;

import com.drinkshop.model.Address;
import com.drinkshop.model.Customer;
import com.drinkshop.model.User;
import com.drinkshop.repository.AddressRepository;
import com.drinkshop.repository.CustomerRepository;
import com.drinkshop.repository.UserRepository;
import com.drinkshop.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AddressRepository addressRepository,
                               UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getByUserId(Integer userId) {
        return customerRepository.findByUser_Id(userId).orElse(null);
    }

    @Override
    public Customer ensureCustomer(Integer userId) {
        return customerRepository.findByUser_Id(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng " + userId));
            Customer customer = new Customer(user);
            customer.setLoyaltyPoints(0);
            return customerRepository.save(customer);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> addressesOf(Integer userId) {
        return addressRepository.findByCustomer_UserIdOrderByIsDefaultDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddress(Integer addressId) {
        return addressRepository.findById(addressId).orElse(null);
    }

    @Override
    public Address addAddress(Integer userId, Address address) {
        Customer customer = ensureCustomer(userId);
        address.setCustomer(customer);
        if (address.isDefault()) {
            List<Address> existing = addressRepository.findByCustomer_UserIdOrderByIsDefaultDesc(userId);
            existing.forEach(a -> a.setDefault(false));
            addressRepository.saveAll(existing);
        } else if (addressRepository.findByCustomer_UserIdOrderByIsDefaultDesc(userId).isEmpty()) {
            address.setDefault(true);
        }
        return addressRepository.save(address);
    }
}
