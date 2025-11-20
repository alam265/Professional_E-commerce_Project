package com.springproject.profEcomWebApp.service;

import com.springproject.profEcomWebApp.exception.APIExceptionHandler;
import com.springproject.profEcomWebApp.exception.ResourceNotFoundException;
import com.springproject.profEcomWebApp.model.Cart;
import com.springproject.profEcomWebApp.model.Category;
import com.springproject.profEcomWebApp.model.Product;
import com.springproject.profEcomWebApp.payload.CartDTO;
import com.springproject.profEcomWebApp.payload.ProductDTO;
import com.springproject.profEcomWebApp.payload.ProductResponse;
import com.springproject.profEcomWebApp.repository.CartRepository;
import com.springproject.profEcomWebApp.repository.CategoryRepo;
import com.springproject.profEcomWebApp.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Value("${project.image}")
    private String path;

    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();

        for(Product product:products){
            if(product.getProductName().equals(productDTO.getProductName())){
                isProductNotPresent = false;
                break;
            }
        }

        if (isProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setCategory(category);
            product.setImage("default.png");
            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        } else throw new APIExceptionHandler("Product Already Exists");
    }


    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageDetails.getPageNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages((long) pageProducts.getTotalPages());
        return productResponse;
    }

    public ProductResponse searchByCategoty(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageProducts = productRepository.findByCategory(category, pageDetails);
        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageDetails.getPageNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages((long) pageProducts.getTotalPages());
        return productResponse;
    }

    public ProductResponse searchByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase("%" +keyword + "%", pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageDetails.getPageNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages((long) pageProducts.getTotalPages());
        return productResponse;
    }

    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product existingProduct = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product","productId", productId));
        Product product = modelMapper.map(productDTO, Product.class);
        existingProduct.setProductName(product.getProductName());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        double specialPrice = product.getPrice() - ((product.getDiscount()*0.01)*product.getPrice());
        existingProduct.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(existingProduct);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();

            cartDTO.setProducts(products);

            return cartDTO;

        }).toList();

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    public ProductDTO deleteProduct(Long productId) {
        Product existingProduct = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product","productId", productId));

        // DELETE
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.deleteById(productId);
        return modelMapper.map(existingProduct, ProductDTO.class);

    }

    public ProductDTO updatetheImage(Long productId, MultipartFile image) {
        Product existingProduct = productRepository.findById(productId).orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));


        String fileName = null;
        try {
            fileName = fileService.uploadImage(path, image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        existingProduct.setImage(fileName);
        Product savedProduct = productRepository.save(existingProduct);
        return modelMapper.map(savedProduct, ProductDTO.class);


    }


}
