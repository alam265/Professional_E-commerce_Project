package com.springproject.profEcomWebApp.repository;

import com.springproject.profEcomWebApp.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {

    Category findByCategoryName(@NotBlank @Size(min=4, message = "Category size must be greater or equal to 4 words") String categoryName);
}
