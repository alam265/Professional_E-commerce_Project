package com.springproject.profEcomWebApp.service;

import com.springproject.profEcomWebApp.model.Address;
import com.springproject.profEcomWebApp.model.User;
import com.springproject.profEcomWebApp.payload.AddressDTO;
import com.springproject.profEcomWebApp.repository.AddressRepository;
import com.springproject.profEcomWebApp.repository.UserRepository;
import com.springproject.profEcomWebApp.util.AuthUtil;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);
        List<Address> addressesList = user.getAddresses();
        addressesList.add(address);
        user.setAddresses(addressesList);
        Address savedAddress = addressRepo.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }
}
