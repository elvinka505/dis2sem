package ru.itis.dis403.lab2_2.orm;

import ru.itis.dis403.lab2_2.orm.model.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // Передаём список сущностей вручную
        EntityManagerFactory factory = new EntityManagerFactory(
                List.of(Country.class, City.class, Street.class)
        );

        EntityManager em = factory.getEntityManager();

        // Сохраняем страну
        Country country = new Country();
        country.setName("Росси");
        em.save(country);  // INSERT => id присвоится автоматически

        // Сохраняем город
        City city = new City();
        city.setName("Казань");
        city.setCountry(country);
        em.save(city);

        // Сохраняем улицу
        Street street = new Street();
        street.setName("Баумана");
        street.setCity(city);
        em.save(street);

        // findAll
        System.out.println("\n--- Все улицы ---");
        em.findAll(Street.class).forEach(System.out::println);

        // find by id
        System.out.println("\n--- Найти город по id=1 ---");
        System.out.println(em.find(City.class, 1L));

        // update
        country.setName("РФ");
        em.save(country);  // UPDATE (id уже есть)

        // remove
        em.remove(street);

        ((EntityManagerImpl) em).close();
        factory.close();
    }
}
