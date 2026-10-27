package com.servet.saucedemo.data;

import java.util.List;

/** checkout-data.json'un Java karşılığı. Jackson record'ları doğrudan doldurabilir (2.12+). */
public record CheckoutData(double taxRate, List<Order> orders) {

    public record Order(String name, Customer customer, List<Product> products, Expected expected) {

        public List<String> productNames() {
            return products.stream().map(Product::name).toList();
        }

        public List<Double> productPrices() {
            return products.stream().map(Product::price).toList();
        }
    }

    public record Customer(String firstName, String lastName, String postalCode) {
    }

    public record Product(String name, double price) {
    }

    public record Expected(double itemTotal, double tax, double total) {
    }
}
