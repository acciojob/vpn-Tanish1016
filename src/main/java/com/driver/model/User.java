// User.java
package com.driver.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String username;
    private String password;
    private String originalIp;
    private Boolean connected = false;
    private String maskedIp;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_service_provider",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "service_provider_id"))
    private List<ServiceProvider> serviceProviderList = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Country originalCountry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Connection> connectionList = new ArrayList<>();

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getOriginalIp() { return originalIp; }
    public void setOriginalIp(String originalIp) { this.originalIp = originalIp; }
    public Boolean getConnected() { return connected; }
    public void setConnected(Boolean connected) { this.connected = connected; }
    public String getMaskedIp() { return maskedIp; }
    public void setMaskedIp(String maskedIp) { this.maskedIp = maskedIp; }
    public List<ServiceProvider> getServiceProviderList() { return serviceProviderList; }
    public void setServiceProviderList(List<ServiceProvider> serviceProviderList) { this.serviceProviderList = serviceProviderList; }
    public Country getOriginalCountry() { return originalCountry; }
    public void setOriginalCountry(Country originalCountry) { this.originalCountry = originalCountry; }
    public List<Connection> getConnectionList() { return connectionList; }
    public void setConnectionList(List<Connection> connectionList) { this.connectionList = connectionList; }
}