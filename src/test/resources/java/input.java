package com.example.shop.models;

import java.util.List;

/* 
 * هذا تعليق متعدد الأسطر يجب أن يتجاهله الـ Lexer
 * يمثل موديل المنتج: صورة، اسم، سعر، تفاصيل
 */
@Entity
public class Product {

    // الحقول الأساسية للمنتج (Fields)
    private int id = 101;
    private String name = "Laptop\tPro \"15\"";
    private double price = 999.99;
    private float discount = .15f;
    private float tax = 50f;
    private char categoryCode = 'A';
    private boolean isAvailable = true;
    private String description = null;

    // Constructor
    public Product(int id, String name, double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    // Method لاختبار الشروط والحلقات والمعاملات
    public double calculateFinalPrice(List<String> coupons) {
        double finalPrice = this.price;
        
        if (this.isAvailable && finalPrice >= 100.0d) {
            finalPrice -= (finalPrice * this.discount);
            finalPrice += this.tax;
        } else {
            return 0.0;
        }

        for (int i = 0; i < 3; i++) {
            if (i != 2 || !this.isAvailable) {
                finalPrice--;
            }
        }

        return finalPrice;
    }
}