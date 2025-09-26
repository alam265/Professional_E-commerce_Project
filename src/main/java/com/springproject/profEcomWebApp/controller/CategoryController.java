package com.springproject.profEcomWebApp.controller;


import com.springproject.profEcomWebApp.model.Category;
import com.springproject.profEcomWebApp.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/public/categories")
    public List<Category> getAllCategories(){
        return categoryService.findAllCategory();
    }

    @PostMapping("/public/categories")
    public String createCategories( @Valid @RequestBody Category category){
        categoryService.createCategory(category);
        return "Successfully created";
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<?> updateCategories(@RequestBody Category category, @PathVariable Long categoryId){
         return categoryService.updateCategory(category, categoryId);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<?> dltCategory(@PathVariable Long categoryId){
        return categoryService.deleteCategory(categoryId);
    }



}
