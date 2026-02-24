package ru.itis.dis403.lab2_2.context.model;

public class Order {
    private Product product;
    private Integer count;
    private String client;

    public Order(Product компьютер, int i, String s) {}

    public Order(Product product) {
        this.product = product;
        this.count = count;
        this.client = client;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getCount() {
        return count;
    }
}
