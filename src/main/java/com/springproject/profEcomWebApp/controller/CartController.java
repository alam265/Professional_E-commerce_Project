package com.springproject.profEcomWebApp.controller;
import com.springproject.profEcomWebApp.model.Cart;
import com.springproject.profEcomWebApp.payload.CartDTO;
import com.springproject.profEcomWebApp.repository.CartRepository;
import com.springproject.profEcomWebApp.service.CartService;
import com.springproject.profEcomWebApp.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    AuthUtil authUtil;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity){
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public  ResponseEntity<List<CartDTO>> getCarts(){
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new  ResponseEntity<>(cartDTOs, HttpStatus.FOUND);
    }

    @GetMapping("/carts/user/cart")
    public ResponseEntity<CartDTO> getCart(){
        String email = authUtil.loggedInEmail();
        Long cartId = cartRepository.findCartByEmail(email).getCartId();
        CartDTO cartDTO = cartService.getCartByEmailAndId(email, cartId);

        return new ResponseEntity<>(cartDTO, HttpStatus.FOUND);
    }

    @PutMapping("/cart/product/{productId}/quantity/{operation}")
    public  ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId, @PathVariable String operation){
            CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, operation.equalsIgnoreCase("delete")? -1:1);

            return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }
}
