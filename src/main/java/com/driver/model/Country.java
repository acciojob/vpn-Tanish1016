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

    // For countries added under a service provider
    @ManyToOne
    private ServiceProvider serviceProvider;

    // For user’s original country
    @OneToOne
    private User user;

    // Getters and Setters

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public CountryName getCountryName() {
        return countryName;
    }
    public void setCountryName(CountryName countryName) {
        this.countryName = countryName;
    }
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
