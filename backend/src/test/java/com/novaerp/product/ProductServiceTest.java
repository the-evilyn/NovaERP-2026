package com.novaerp.product;

import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.product.dto.ProductDTO;
import com.novaerp.product.entity.Category;
import com.novaerp.product.entity.Product;
import com.novaerp.product.entity.ProductStatus;
import com.novaerp.product.repository.CategoryRepository;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.product.service.ProductServiceImpl;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder().id(1L).code("CAT-ALIM").name("Alimentation").build();
        sampleProduct = Product.builder()
                .id(1L)
                .sku("HUI-005")
                .name("Huile de table 5L")
                .category(sampleCategory)
                .purchasePrice(BigDecimal.valueOf(85.0))
                .sellingPrice(BigDecimal.valueOf(105.0))
                .minStockLevel(BigDecimal.valueOf(30.0))
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void testGetProducts() {
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(any(PageRequest.class))).thenReturn(productPage);
        when(stockRepository.getTotalQuantityOnHandByProductId(1L)).thenReturn(BigDecimal.valueOf(120.0));

        Page<ProductDTO> result = productService.getProducts(PageRequest.of(0, 10), null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Huile de table 5L", result.getContent().get(0).getNom());
        assertEquals(BigDecimal.valueOf(120.0), result.getContent().get(0).getQuantiteStock());
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(stockRepository.getTotalQuantityOnHandByProductId(1L)).thenReturn(BigDecimal.valueOf(120.0));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("HUI-005", result.getReference());
        assertEquals("Huile de table 5L", result.getNom());
    }

    @Test
    void testCreateProduct() {
        ProductDTO input = ProductDTO.builder()
                .nom("Sucre 2kg")
                .reference("SUC-002")
                .prixAchat(BigDecimal.valueOf(18.0))
                .prixVente(BigDecimal.valueOf(24.0))
                .quantiteStock(BigDecimal.valueOf(50.0))
                .seuilMinimum(BigDecimal.valueOf(25.0))
                .categorie("Alimentation")
                .build();

        when(productRepository.existsBySku("SUC-002")).thenReturn(false);
        when(categoryRepository.findByName("Alimentation")).thenReturn(Optional.of(sampleCategory));
        when(warehouseRepository.findAll()).thenReturn(List.of(Warehouse.builder().id(1L).code("WH-01").build()));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductDTO result = productService.createProduct(input);

        assertNotNull(result);
        assertEquals("Sucre 2kg", result.getNom());
        assertEquals("SUC-002", result.getReference());
        assertEquals(BigDecimal.valueOf(50.0), result.getQuantiteStock());
    }

    @Test
    void testDeleteProductNotFoundThrowsException() {
        when(productRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
    }
}
