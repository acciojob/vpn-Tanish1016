// CountryName.java
package com.driver.model;

public enum CountryName {
    IND("001"),
    USA("002"),
    AUS("003"),
    CHI("004"),
    JPN("005");

    private final String code;

    CountryName(String code) {
        this.code = code;
    }

    public String toCode() {
        return code;
    }
}
