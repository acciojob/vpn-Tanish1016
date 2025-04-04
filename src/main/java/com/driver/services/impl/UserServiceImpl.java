package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;

    @Autowired
    ServiceProviderRepository serviceProviderRepository3;

    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception {
        CountryName countryNameEnum;
        try {
            countryNameEnum = CountryName.valueOf(countryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Country not found");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setConnected(false);
        user.setServiceProviderList(new ArrayList<>());
        user.setConnectionList(new ArrayList<>());
        user = userRepository3.save(user);

        Country country = new Country();
        country.setCountryName(countryNameEnum);
        country.setCode(countryNameEnum.toCode());
        country.setUser(user);
        user.setOriginalCountry(country);
        user.setOriginalIp(country.getCode() + "." + user.getId());

        countryRepository3.save(country);
        userRepository3.save(user);

        return user;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user = userRepository3.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).orElseThrow(() -> new RuntimeException("ServiceProvider not found"));
        user.getServiceProviderList().add(serviceProvider);
        serviceProvider.getUsers().add(user);
        userRepository3.save(user);
        serviceProviderRepository3.save(serviceProvider);
        return user;
    }
}