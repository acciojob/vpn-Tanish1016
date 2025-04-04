package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;

    @Autowired
    ServiceProviderRepository serviceProviderRepository2;

    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).orElseThrow(() -> new Exception("User not found"));
        if (user.getConnected()) throw new Exception("Already connected");

        CountryName originalCountryName = user.getOriginalCountry().getCountryName();
        if (originalCountryName.toString().equalsIgnoreCase(countryName)) return user;

        CountryName targetCountry;
        try {
            targetCountry = CountryName.valueOf(countryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Country not found");
        }

        List<ServiceProvider> providers = user.getServiceProviderList();
        if (providers.isEmpty()) throw new Exception("Unable to connect");

        ServiceProvider selectedProvider = null;
        for (ServiceProvider sp : providers) {
            for (Country country : sp.getCountryList()) {
                if (country.getCountryName() == targetCountry) {
                    if (selectedProvider == null || sp.getId() < selectedProvider.getId()) {
                        selectedProvider = sp;
                        break;
                    }
                }
            }
        }

        if (selectedProvider == null) throw new Exception("Unable to connect");

        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(selectedProvider);
        connectionRepository2.save(connection);

        user.setConnected(true);
        user.setMaskedIp(targetCountry.toCode() + "." + selectedProvider.getId() + "." + user.getId());
        user.getConnectionList().add(connection);
        selectedProvider.getConnectionList().add(connection);

        userRepository2.save(user);
        serviceProviderRepository2.save(selectedProvider);

        return user;
    }

    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).orElseThrow(() -> new Exception("User not found"));
        if (!user.getConnected()) throw new Exception("Already disconnected");
        user.setConnected(false);
        user.setMaskedIp(null);
        userRepository2.save(user);
        return user;
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).orElseThrow(() -> new Exception("Sender not found"));
        User receiver = userRepository2.findById(receiverId).orElseThrow(() -> new Exception("Receiver not found"));

        CountryName receiverCountry = getCurrentCountry(receiver);
        CountryName senderCountry = getCurrentCountry(sender);

        if (senderCountry == receiverCountry) return sender;

        if (sender.getConnected()) throw new Exception("Cannot establish communication");

        try {
            return connect(senderId, receiverCountry.toString().toLowerCase());
        } catch (Exception e) {
            throw new Exception("Cannot establish communication");
        }
    }

    private CountryName getCurrentCountry(User user) {
        if (user.getConnected()) {
            String code = user.getMaskedIp().split("\\.")[0];
            for (CountryName cn : CountryName.values()) {
                if (cn.toCode().equals(code)) return cn;
            }
            throw new IllegalArgumentException("Invalid code");
        } else {
            return user.getOriginalCountry().getCountryName();
        }
    }
}