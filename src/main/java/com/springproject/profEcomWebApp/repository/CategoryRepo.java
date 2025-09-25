package com.springproject.profEcomWebApp.repository;

import com.springproject.profEcomWebApp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {

}
