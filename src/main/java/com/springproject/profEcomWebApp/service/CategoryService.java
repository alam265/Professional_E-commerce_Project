package com.springproject.profEcomWebApp.service;

import com.springproject.profEcomWebApp.exception.APIExceptionHandler;
import com.springproject.profEcomWebApp.exception.ResourceNotFoundException;
import com.springproject.profEcomWebApp.model.Category;
import com.springproject.profEcomWebApp.payload.CategoryDTO;
import com.springproject.profEcomWebApp.payload.CategoryResponse;
import com.springproject.profEcomWebApp.repository.CategoryRepo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private ModelMapper modelMapper;

    public CategoryResponse findAllCategory(){
        List<Category> categories = categoryRepo.findAll();
        if(categories.isEmpty()){
            throw new APIExceptionHandler("Categoy Empty");
        }
        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        return categoryResponse;

    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO){
        Category category = modelMapper.map(categoryDTO, Category.class);
        Optional<Category> existingCategory = Optional.ofNullable(categoryRepo.findByCategoryName(category.getCategoryName()));
        if (existingCategory.isPresent()){
            throw  new APIExceptionHandler("Category: "+category.getCategoryName()+ " is already exits!");
        }
        Category savedCategory = categoryRepo.save(category);
        CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory, CategoryDTO.class);
        return savedCategoryDTO;
    }

    public CategoryDTO deleteCategory(Long categoryId){
        Optional<Category> optionalCategory = categoryRepo.findById(categoryId);

        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
           categoryRepo.deleteById(categoryId);
           CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
           return categoryDTO;

        }
        else throw new ResourceNotFoundException("Category", "CategoryId", categoryId);
    }

    public ResponseEntity<?> updateCategory(CategoryDTO categoryDTO, Long categoryId){

        Category category = modelMapper.map(categoryDTO, Category.class);
        Optional<Category> optionalCategory = categoryRepo.findById(categoryId);

        if (optionalCategory.isPresent()) {
            Category cat =  optionalCategory.get();
            cat.setCategoryName(category.getCategoryName());
            Category savedCategory = categoryRepo.save(cat);
            CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory, CategoryDTO.class);
            return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);

        }
        else {
            return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }





    }

}
