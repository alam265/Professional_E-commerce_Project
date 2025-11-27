package com.springproject.profEcomWebApp.repository;


import com.springproject.profEcomWebApp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{

}