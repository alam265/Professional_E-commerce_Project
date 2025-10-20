package com.springproject.profEcomWebApp.controller;

import com.springproject.profEcomWebApp.model.Product;
import com.springproject.profEcomWebApp.payload.ProductDTO;
import com.springproject.profEcomWebApp.payload.ProductResponse;
import com.springproject.profEcomWebApp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody ProductDTO productDTO,@PathVariable Long categoryId){
        ProductDTO savedproductDTO = productService.addProduct(productDTO, categoryId);
        return new ResponseEntity<>(savedproductDTO, HttpStatus.CREATED);

    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(){
        ProductResponse response = productService.getAllProducts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/product")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId){
        ProductResponse productResponse = productService.searchByCategoty(categoryId);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);

    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductByKeyword(@PathVariable String keyword){
       ProductResponse productResponse = productService.searchByKeyword(keyword);
       return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("/public/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO productDTO, @PathVariable Long productId){
        ProductDTO updatedProductDTO =  productService.updateProduct(productDTO, productId);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);

    }
    @DeleteMapping("/public/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct( @PathVariable Long productId){
        ProductDTO updatedProductDTO =  productService.deleteProduct(productId);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);

    }

    @PutMapping("/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateImage(@PathVariable Long productId, @RequestParam("image") MultipartFile image){
        ProductDTO updatedImage = productService.updatetheImage(productId, image);
        return new ResponseEntity<>(updatedImage, HttpStatus.OK);
    }


}
