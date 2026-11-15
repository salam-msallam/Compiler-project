package com.compiler.controller;

import com.compiler.model.Product;
import com.compiler.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. اختبار عرض جميع المنتجات (GET Request + Model + List)
    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "قائمة المنتجات");
        return "products/list";
    }

    // 2. اختبار عرض تفاصيل منتج محدد (PathVariable + الشرط + Null check)
    @GetMapping("/details/{id}")
    public String productDetails(@PathVariable("id") int productId, Model model) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        return "products/details";
    }

    // 3. اختبار عرض صفحة إضافة منتج جديد (Object Creation)
    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "products/add";
    }

    // 4. اختبار معالجة إضافة منتج جديد (PostMapping + ModelAttribute + RequestParam)
    @PostMapping("/add")
    public String saveProduct(
            @ModelAttribute("product") Product product,
            @RequestParam(name = "discount", required = false, defaultValue = "0.0") double discount,
            Model model) {

        if (product.getPrice() > 0 && discount >= 0.0f) {
            double finalPrice = product.getPrice() - (product.getPrice() * discount);
            product.setPrice(finalPrice);
        }

        productService.save(product);
        return "redirect:/products";
    }
}