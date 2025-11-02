package com.springproject.profEcomWebApp.repository;

import com.springproject.profEcomWebApp.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserName(String userName);


    boolean existsByUserName(@NotBlank String userName);


    boolean existsByEmail(@NotBlank @Email String email);


}
