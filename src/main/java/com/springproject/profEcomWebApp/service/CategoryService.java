package com.springproject.profEcomWebApp.service;

import com.springproject.profEcomWebApp.model.Category;
import com.springproject.profEcomWebApp.repository.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;

    public List<Category> findAllCategory(){
        return categoryRepo.findAll();
    }

    public void createCategory(Category category){
        categoryRepo.save(category);
    }

    public ResponseEntity<?> deleteCategory(Long categoryId){
//        categoryRepo.deleteById(categoryId);
        Optional<Category> optionalCategory = categoryRepo.findById(categoryId);

        if (optionalCategory.isPresent()) {
            categoryRepo.deleteById(categoryId);
            return new  ResponseEntity<>("Deleted Successfully", HttpStatus.OK);

        }
        else {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> updateCategory(Category category, Long categoryId){
        Optional<Category> optionalCategory = categoryRepo.findById(categoryId);

        if (optionalCategory.isPresent()) {
            Category cat =  optionalCategory.get();
            cat.setCategoryName(category.getCategoryName());
            Category savedCategory = categoryRepo.save(cat);
            return new ResponseEntity<>(savedCategory, HttpStatus.OK);

        }
        else {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }





    }

}
