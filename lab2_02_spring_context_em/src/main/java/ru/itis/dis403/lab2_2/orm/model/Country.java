package ru.itis.dis403.lab2_2.orm.model;

import ru.itis.dis403.lab2_2.orm.annotation.*;

@Entity
public class Country {
    @Id
    Long id;
    @Column
    String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Country{id=" + id + ", name='" + name + "'}";
    }
}
