package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);
        // Initialize the list to avoid null pointer issues
        admin.setServiceProviders(new ArrayList<>());
        admin = adminRepository1.save(admin);
        return admin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {
        Optional<Admin> adminOptional = adminRepository1.findById(adminId);
        Admin admin = adminOptional.get();

        ServiceProvider sp = new ServiceProvider();
        sp.setName(providerName);
        sp.setAdmin(admin);
        // Initialize lists for countries, users and connections
        sp.setCountryList(new ArrayList<>());
        sp.setUsers(new ArrayList<>());
        sp.setConnectionList(new ArrayList<>());

        sp = serviceProviderRepository1.save(sp);

        // Update the admin's list of service providers
        if(admin.getServiceProviders() == null) {
            admin.setServiceProviders(new ArrayList<>());
        }
        admin.getServiceProviders().add(sp);
        admin = adminRepository1.save(admin);
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception {
        Optional<ServiceProvider> spOptional = serviceProviderRepository1.findById(serviceProviderId);
        ServiceProvider sp = spOptional.get();
        String targetCountry = countryName.toUpperCase();

        CountryName countryEnum;
        try {
            countryEnum = CountryName.valueOf(targetCountry);
        } catch (IllegalArgumentException e) {
            throw new Exception("Country not found");
        }

        Country country = new Country();
        country.setCountryName(countryEnum);
        country.setServiceProvider(sp);
        // For admin-added country, user remains null
        country.setUser(null);

        // Add country to service provider’s country list
        if(sp.getCountryList() == null) {
            sp.setCountryList(new ArrayList<>());
        }
        sp.getCountryList().add(country);

        // Save country and update service provider
        countryRepository1.save(country);
        serviceProviderRepository1.save(sp);
        return sp;
    }
}

