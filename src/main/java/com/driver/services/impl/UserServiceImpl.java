package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Optional;

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
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setConnected(false);
        user.setMaskedIp(null);
        // Initialize empty lists for providers and connections
        user.setServiceProviderList(new ArrayList<>());
        user.setConnectionList(new ArrayList<>());

        // Validate and convert country name
        String targetCountry = countryName.toUpperCase();
        CountryName countryEnum;
        try {
            countryEnum = CountryName.valueOf(targetCountry);
        } catch(Exception e) {
            throw new Exception("Country not found");
        }

        // Save user first to generate an id
        user = userRepository3.save(user);

        // Set originalIp as "countryCode.userId"
        String originalIp = countryEnum.toCode() + "." + user.getId();
        user.setOriginalIp(originalIp);

        // Create the original Country object for the user
        Country country = new Country();
        country.setCountryName(countryEnum);
        // For user's original country, serviceProvider remains null
        country.setServiceProvider(null);
        country.setUser(user);
        country = countryRepository3.save(country);
        user.setOriginalCountry(country);

        user = userRepository3.save(user);
        return user;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        Optional<User> userOptional = userRepository3.findById(userId);
        User user = userOptional.get();
        ServiceProvider sp = serviceProviderRepository3.findById(serviceProviderId).get();

        if(user.getServiceProviderList() == null){
            user.setServiceProviderList(new ArrayList<>());
        }
        // Add the service provider if not already subscribed
        if(!user.getServiceProviderList().contains(sp)){
            user.getServiceProviderList().add(sp);
        }
        user = userRepository3.save(user);
        return user;
    }
}
