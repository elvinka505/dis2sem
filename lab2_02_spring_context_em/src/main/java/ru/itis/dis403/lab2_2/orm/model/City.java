package ru.itis.dis403.lab2_2.orm.model;

import ru.itis.dis403.lab2_2.orm.annotation.*;

@Entity
public class City {
    @Id
    Long id;
    @Column
    String name;
    @ManyToOne
    Country country;  // country_id в БД

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    @Override
    public String toString() {
        return "City{id=" + id + ", name='" + name + "', country=" + country + "}";
    }
}
