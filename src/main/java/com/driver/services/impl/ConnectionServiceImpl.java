package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        Optional<User> userOptional = userRepository2.findById(userId);
        User user = userOptional.get();

        // If already connected, throw exception
        if(user.getConnected() != null && user.getConnected()){
            throw new Exception("Already connected");
        }

        String targetCountry = countryName.toUpperCase();
        // If the desired country is the same as the user's original country, do nothing
        if(user.getOriginalCountry().getCountryName().name().equalsIgnoreCase(targetCountry)){
            return user;
        }

        // Check among subscribed service providers for one that has the country available
        List<ServiceProvider> providers = user.getServiceProviderList();
        if(providers == null || providers.isEmpty()){
            throw new Exception("Unable to connect");
        }

        ServiceProvider chosenProvider = null;
        for(ServiceProvider sp : providers) {
            List<Country> countryList = sp.getCountryList();
            if(countryList != null) {
                for(Country c : countryList) {
                    if(c.getCountryName().name().equalsIgnoreCase(targetCountry)){
                        if(chosenProvider == null || sp.getId() < chosenProvider.getId()){
                            chosenProvider = sp;
                        }
                    }
                }
            }
        }

        if(chosenProvider == null) {
            throw new Exception("Unable to connect");
        }

        // Establish the connection by creating a Connection entity
        Connection connection = new Connection();
        connection.setUser(user);
        connection.setServiceProvider(chosenProvider);
        connectionRepository2.save(connection);

        // Update user's attributes to reflect connection
        user.setConnected(true);
        CountryName cn = CountryName.valueOf(targetCountry);
        String maskedIp = cn.toCode() + "." + chosenProvider.getId() + "." + user.getId();
        user.setMaskedIp(maskedIp);
        if(user.getConnectionList() == null) {
            user.setConnectionList(new ArrayList<>());
        }
        user.getConnectionList().add(connection);
        userRepository2.save(user);
        return user;
    }

    @Override
    public User disconnect(int userId) throws Exception {
        Optional<User> userOptional = userRepository2.findById(userId);
        User user = userOptional.get();

        if(user.getConnected() == null || !user.getConnected()){
            throw new Exception("Already disconnected");
        }

        // Disconnect the user by resetting the connection attributes
        user.setConnected(false);
        user.setMaskedIp(null);
        userRepository2.save(user);
        return user;
    }

    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();
        String receiverCurrentCountry = "";

        // Determine receiver's current country
        if(receiver.getConnected() != null && receiver.getConnected()){
            // If connected, extract the country code from maskedIp (format: code.spId.userId)
            String maskedIp = receiver.getMaskedIp();
            String[] parts = maskedIp.split("\\.");
            String countryCode = parts[0];
            // Map the code back to the enum value
            for(CountryName cn : CountryName.values()){
                if(cn.toCode().equals(countryCode)){
                    receiverCurrentCountry = cn.name();
                    break;
                }
            }
            if(receiverCurrentCountry.equals("")){
                throw new Exception("Cannot establish communication");
            }
        } else {
            // If not connected, use original country
            receiverCurrentCountry = receiver.getOriginalCountry().getCountryName().name();
        }

        // If sender's original country is same as receiver's current country, communication is possible
        String senderOriginalCountry = sender.getOriginalCountry().getCountryName().name();
        if(senderOriginalCountry.equalsIgnoreCase(receiverCurrentCountry)){
            return sender;
        } else {
            // Otherwise, try to connect sender to the receiver's country
            try {
                // In case sender is already connected (to a different country), disconnect first
                if(sender.getConnected() != null && sender.getConnected()){
                    sender = disconnect(senderId);
                }
                sender = connect(senderId, receiverCurrentCountry);
            } catch(Exception e) {
                throw new Exception("Cannot establish communication");
            }
            return sender;
        }
    }
}

