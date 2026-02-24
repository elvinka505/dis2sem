package ru.itis.dis403.lab2_2.context.model;

public class ImportProduct {
    private Product product;
    private Integer count;
    private String supplier;

    public Product getProduct() {
        return product;
    }

    public ImportProduct(String supplier, Integer count, Product product) {
        this.supplier = supplier;
        this.count = count;
        this.product = product;
    }

    public Integer getCount() {
        return count;
    }

    public String getSupplier() {
        return supplier;
    }
}
