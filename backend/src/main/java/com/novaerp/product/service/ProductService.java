package com.novaerp.product.service;

import com.novaerp.product.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductDTO> getProducts(Pageable pageable, String search);
    ProductDTO getProductById(Long id);
    ProductDTO getProductBySku(String sku);
    ProductDTO createProduct(ProductDTO dto);
    List<ProductDTO> createProducts(List<ProductDTO> dtos);
    ProductDTO updateProduct(Long id, ProductDTO dto);
    void deleteProduct(Long id);
    void deleteProducts(List<Long> ids);
}
