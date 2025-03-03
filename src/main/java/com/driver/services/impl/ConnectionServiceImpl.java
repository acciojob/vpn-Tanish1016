package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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
        User user = userRepository2.findById(userId).get();
        if (user.getConnected() != null && user.getConnected()) {
            throw new Exception("Already connected");
        }
        // Validate and convert target country name
        CountryName targetCountry;
        try {
            targetCountry = CountryName.valueOf(countryName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Country not found");
        }
        // If user is trying to connect to his original country, no need to connect
        if (user.getOriginalCountry().getCountryName() == targetCountry) {
            return user;
        }
        // Check if user is subscribed to any service provider
        if (user.getServiceProviderList() == null || user.getServiceProviderList().isEmpty()) {
            throw new Exception("Unable to connect");
        }
        // Find a service provider (with smallest id) that has the required country
        ServiceProvider chosenProvider = null;
        for (ServiceProvider provider : user.getServiceProviderList()) {
            if (provider.getCountryList() != null) {
                for (Country country : provider.getCountryList()) {
                    if (country.getCountryName() == targetCountry) {
                        if (chosenProvider == null || provider.getId() < chosenProvider.getId()) {
                            chosenProvider = provider;
                        }
                        break;
                    }
                }
            }
        }
        if (chosenProvider == null) {
            throw new Exception("Unable to connect");
        }
        // Create and save the connection
        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(chosenProvider);
        connectionRepository2.save(connection);

        // Update user's connection status and masked IP format: "countryCode.serviceProviderId.userId"
        user.setConnected(true);
        String maskedIp = targetCountry.toCode() + "." + chosenProvider.getId() + "." + user.getId();
        user.setMaskedIp(maskedIp);
        if (user.getConnectionList() == null) {
            user.setConnectionList(new ArrayList<>());
        }
        user.getConnectionList().add(connection);
        userRepository2.save(user);
        return user;
    }

    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (user.getConnected() == null || !user.getConnected()) {
            throw new Exception("Already disconnected");
        }
        user.setConnected(false);
        user.setMaskedIp(null);
        userRepository2.save(user);
        return user;
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        // Determine receiver's current country:
        // If receiver is connected, parse the maskedIp; otherwise, use the original country.
        CountryName receiverCountry;
        if (receiver.getConnected() != null && receiver.getConnected()) {
            String maskedIp = receiver.getMaskedIp();
            if (maskedIp == null || maskedIp.isEmpty()) {
                receiverCountry = receiver.getOriginalCountry().getCountryName();
            } else {
                String code = maskedIp.split("\\.")[0];
                receiverCountry = null;
                for (CountryName cn : CountryName.values()) {
                    if (cn.toCode().equals(code)) {
                        receiverCountry = cn;
                        break;
                    }
                }
                if (receiverCountry == null) {
                    receiverCountry = receiver.getOriginalCountry().getCountryName();
                }
            }
        } else {
            receiverCountry = receiver.getOriginalCountry().getCountryName();
        }

        // If sender's original country is same as receiver's current country, communication can happen directly.
        if (sender.getOriginalCountry().getCountryName() == receiverCountry) {
            return sender;
        } else {
            // Otherwise, try to connect sender to the receiver's country using a subscribed service provider.
            if (sender.getServiceProviderList() == null || sender.getServiceProviderList().isEmpty()) {
                throw new Exception("Cannot establish communication");
            }
            ServiceProvider chosenProvider = null;
            for (ServiceProvider provider : sender.getServiceProviderList()) {
                if (provider.getCountryList() != null) {
                    for (Country country : provider.getCountryList()) {
                        if (country.getCountryName() == receiverCountry) {
                            if (chosenProvider == null || provider.getId() < chosenProvider.getId()) {
                                chosenProvider = provider;
                            }
                            break;
                        }
                    }
                }
            }
            if (chosenProvider == null) {
                throw new Exception("Cannot establish communication");
            }
            // Create and save a connection for the sender
            Connection connection = new Connection();
            connection.setUser(sender);
            connection.setServiceProvider(chosenProvider);
            connectionRepository2.save(connection);

            sender.setConnected(true);
            String maskedIp = receiverCountry.toCode() + "." + chosenProvider.getId() + "." + sender.getId();
            sender.setMaskedIp(maskedIp);
            if (sender.getConnectionList() == null) {
                sender.setConnectionList(new ArrayList<>());
            }
            sender.getConnectionList().add(connection);
            userRepository2.save(sender);
            return sender;
        }
    }
}
