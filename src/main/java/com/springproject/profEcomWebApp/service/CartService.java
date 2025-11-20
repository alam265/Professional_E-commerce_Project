package com.springproject.profEcomWebApp.service;

import com.springproject.profEcomWebApp.exception.APIExceptionHandler;
import com.springproject.profEcomWebApp.exception.ResourceNotFoundException;
import com.springproject.profEcomWebApp.model.Cart;
import com.springproject.profEcomWebApp.model.CartItem;
import com.springproject.profEcomWebApp.model.Product;
import com.springproject.profEcomWebApp.payload.CartDTO;
import com.springproject.profEcomWebApp.payload.ProductDTO;
import com.springproject.profEcomWebApp.repository.CartItemRepository;
import com.springproject.profEcomWebApp.repository.CartRepository;
import com.springproject.profEcomWebApp.repository.ProductRepository;
import com.springproject.profEcomWebApp.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartService {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthUtil authUtil;


    public CartDTO addProductToCart(Long productId, Integer quantity) {

        Cart cart = createCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "product_id", productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), product.getProductId());
        if (cartItem != null) {
            throw new APIExceptionHandler("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIExceptionHandler(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIExceptionHandler("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity()); // not needed here

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;

    }

    private Cart createCart() {
        Cart userCart  = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart =  cartRepository.save(cart);
        return newCart;
    }

    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()){
            throw new APIExceptionHandler("No cart exists");
        }
        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> productDTOs = cart.getCartItems().stream()
                    .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).toList();
            cartDTO.setProducts(productDTOs);
            return cartDTO;

        }).toList();

        return cartDTOs;

    }

    public CartDTO getCartByEmailAndId(String email, Long cartId) {
        Cart cart = cartRepository.getCartByEmailAndId(email, cartId);
        if (cart==null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(item -> item.getProduct().setQuantity(item.getQuantity()));
        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).toList();
        cartDTO.setProducts(productDTOs);

        return  cartDTO;
    }

    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, int quantity) {
        String email = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("cartId","Cart", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "product_id", productId));

        if (product.getQuantity() == 0) {
            throw new APIExceptionHandler(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIExceptionHandler("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new APIExceptionHandler("Product " + product.getProductName() + " Not in the cart");
        }

        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setDiscount(product.getDiscount());
        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice()*quantity));
        cartRepository.save(cart);
        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        if (updatedCartItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });


        cartDTO.setProducts(productStream.toList());

        return cartDTO;



    }

    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }
}
