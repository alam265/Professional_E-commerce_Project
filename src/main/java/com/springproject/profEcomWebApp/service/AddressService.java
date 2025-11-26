package com.springproject.profEcomWebApp.service;

import com.springproject.profEcomWebApp.exception.ResourceNotFoundException;
import com.springproject.profEcomWebApp.model.Address;
import com.springproject.profEcomWebApp.model.User;
import com.springproject.profEcomWebApp.payload.AddressDTO;
import com.springproject.profEcomWebApp.repository.AddressRepository;
import com.springproject.profEcomWebApp.repository.UserRepository;
import com.springproject.profEcomWebApp.util.AuthUtil;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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


    public List<AddressDTO> getAllAddress() {
        List<Address> addresses = addressRepo.findAll();
        List<AddressDTO> addressDTOs = addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();
        return  addressDTOs;
    }

    public AddressDTO getAddressesById(Long addressId) {
        Address address = addressRepo.findById(addressId).orElseThrow(()-> new ResourceNotFoundException("Address","addressId", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addresses = user.getAddresses();
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address addressFromDatabase = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressFromDatabase.setCity(addressDTO.getCity());
        addressFromDatabase.setPincode(addressDTO.getPincode());
        addressFromDatabase.setState(addressDTO.getState());
        addressFromDatabase.setCountry(addressDTO.getCountry());
        addressFromDatabase.setStreet(addressDTO.getStreet());
        addressFromDatabase.setBuildingName(addressDTO.getBuildingName());

        Address updatedAddress = addressRepo.save(addressFromDatabase);

        User user = addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepo.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    }

    public String deleteAddress(Long addressId) {
        Address addressFromDatabase = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        User user = addressFromDatabase.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        userRepo.save(user);

        addressRepo.delete(addressFromDatabase);

        return "Address deleted successfully with addressId: " + addressId;
    }

}
