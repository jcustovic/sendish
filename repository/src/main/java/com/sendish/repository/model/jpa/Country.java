package com.sendish.repository.model.jpa;

import javax.persistence.*;

@Entity
@Table(name = "country")
@SequenceGenerator(name = "idSequence", sequenceName = "country_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name="c_id"))
public class Country extends BaseEntity {

    private static final long serialVersionUID = 1L;

    public Country() {
        // Hibernate
    }

    public Country(String name, String iso, String iso3, String currency, String currencyCode) {
        this.name = name;
        this.iso = iso;
        this.iso3 = iso3;
        this.currency = currency;
        this.currencyCode = currencyCode;
    }

    @Column(name="c_name", nullable = false, length = 64)
    private String name;

    @Column(name="c_iso", nullable = false, unique = true, length = 2)
    private String iso;

    @Column(name="c_iso3", nullable = false, unique = true, length = 3)
    private String iso3;

    @Column(name="c_currency", nullable = false)
    private String currency;

    @Column(name="c_currency_code", nullable = false, length = 3)
    private String currencyCode;

    // Getters & setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getIso3() {
        return iso3;
    }

    public void setIso3(String iso3) {
        this.iso3 = iso3;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

}
