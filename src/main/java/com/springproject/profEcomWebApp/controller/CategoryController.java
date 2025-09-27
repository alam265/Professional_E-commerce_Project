package com.springproject.profEcomWebApp.controller;


import com.springproject.profEcomWebApp.config.AppConstants;
import com.springproject.profEcomWebApp.payload.CategoryDTO;
import com.springproject.profEcomWebApp.payload.CategoryResponse;
import com.springproject.profEcomWebApp.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;



    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                             @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                             @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY) String sortBy,
                                                             @RequestParam(name = "sortDirection", defaultValue = AppConstants.SORT_DIR) String sortDirection){
        CategoryResponse categoryResponse = categoryService.findAllCategory(pageNumber, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> createCategories( @Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);

    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<?> updateCategories(@Valid @RequestBody CategoryDTO categoryDTO, @PathVariable Long categoryId){
         return categoryService.updateCategory(categoryDTO, categoryId);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public CategoryDTO dltCategory(@PathVariable Long categoryId){
        return categoryService.deleteCategory(categoryId);
    }



}
