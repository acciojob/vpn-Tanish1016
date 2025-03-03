// Note: Do not write @Enumerated annotation above CountryName in this model.
package com.driver.model;

import javax.persistence.*;

@Entity
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private CountryName countryName;

    @OneToOne
    private User user; // For user original country; null when added under a service provider

    @ManyToOne
    private ServiceProvider serviceProvider;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public CountryName getCountryName() { return countryName; }
    public void setCountryName(CountryName countryName) { this.countryName = countryName; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public ServiceProvider getServiceProvider() { return serviceProvider; }
    public void setServiceProvider(ServiceProvider serviceProvider) { this.serviceProvider = serviceProvider; }
}
