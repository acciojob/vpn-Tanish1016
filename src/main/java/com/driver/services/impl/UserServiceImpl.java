package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
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
        CountryName cName;
        try {
            cName = CountryName.valueOf(countryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Country not found");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setConnected(false);
        user.setMaskedIp(null);
        user.setServiceProviderList(new ArrayList<>());
        user.setConnectionList(new ArrayList<>());
        // Save user first to generate the id
        userRepository3.save(user);

        // Create a new Country for the user’s original country
        Country country = new Country();
        country.setCountryName(cName);
        country.setUser(user);
        country.setServiceProvider(null);
        countryRepository3.save(country);

        user.setOriginalCountry(country);
        // Set originalIp as "countryCode.userId"
        String originalIp = cName.toCode() + "." + user.getId();
        user.setOriginalIp(originalIp);
        userRepository3.save(user);
        return user;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user = userRepository3.findById(userId).get();
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();
        if (user.getServiceProviderList() == null) {
            user.setServiceProviderList(new ArrayList<>());
        }
        if (!user.getServiceProviderList().contains(serviceProvider)) {
            user.getServiceProviderList().add(serviceProvider);
        }
        if (serviceProvider.getUsers() == null) {
            serviceProvider.setUsers(new ArrayList<>());
        }
        if (!serviceProvider.getUsers().contains(user)) {
            serviceProvider.getUsers().add(user);
        }
        userRepository3.save(user);
        serviceProviderRepository3.save(serviceProvider);
        return user;
    }
}
