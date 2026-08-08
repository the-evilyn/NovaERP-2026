package com.novaerp.product.service;

import com.novaerp.exception.ResourceAlreadyExistsException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.product.dto.ProductDTO;
import com.novaerp.product.entity.Category;
import com.novaerp.product.entity.Product;
import com.novaerp.product.entity.ProductStatus;
import com.novaerp.product.repository.CategoryRepository;
import com.novaerp.product.repository.ProductRepository;
import com.novaerp.stock.entity.Stock;
import com.novaerp.stock.entity.Warehouse;
import com.novaerp.stock.repository.StockRepository;
import com.novaerp.stock.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(Pageable pageable, String search) {
        Page<Product> page = StringUtils.hasText(search)
                ? productRepository.searchProducts(search, pageable)
                : productRepository.findAll(pageable);

        return page.map(p -> {
            BigDecimal stockQty = stockRepository.getTotalQuantityOnHandByProductId(p.getId());
            return ProductDTO.fromEntity(p, stockQty);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        BigDecimal stockQty = stockRepository.getTotalQuantityOnHandByProductId(product.getId());
        return ProductDTO.fromEntity(product, stockQty);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));

        BigDecimal stockQty = stockRepository.getTotalQuantityOnHandByProductId(product.getId());
        return ProductDTO.fromEntity(product, stockQty);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO dto) {
        log.info("Creating product: {}", dto.getNom());

        String sku = StringUtils.hasText(dto.getReference())
                ? dto.getReference().trim().toUpperCase()
                : generateSku();

        if (productRepository.existsBySku(sku)) {
            throw new ResourceAlreadyExistsException("Product with SKU / reference " + sku + " already exists");
        }

        Category category = resolveCategory(dto.getCategorie(), dto.getCategoryId());

        Product product = Product.builder()
                .name(dto.getNom().trim())
                .sku(sku)
                .category(category)
                .purchasePrice(dto.getPrixAchat() != null ? dto.getPrixAchat() : BigDecimal.ZERO)
                .sellingPrice(dto.getPrixVente() != null ? dto.getPrixVente() : BigDecimal.ZERO)
                .minStockLevel(dto.getSeuilMinimum() != null ? dto.getSeuilMinimum() : BigDecimal.ZERO)
                .unitOfMeasure(StringUtils.hasText(dto.getUnitOfMeasure()) ? dto.getUnitOfMeasure() : "UNITE")
                .barcode(dto.getBarcode())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product);

        // Initialize default stock record in default warehouse
        BigDecimal initialStock = dto.getQuantiteStock() != null ? dto.getQuantiteStock() : BigDecimal.ZERO;
        Warehouse defaultWarehouse = warehouseRepository.findAll().stream().findFirst()
                .orElseGet(() -> warehouseRepository.save(Warehouse.builder().code("WH-MAIN").name("Entrepôt Principal").city("Casablanca").active(true).build()));

        Stock stock = Stock.builder()
                .product(savedProduct)
                .warehouse(defaultWarehouse)
                .quantityOnHand(initialStock)
                .quantityAllocated(BigDecimal.ZERO)
                .quantityAvailable(initialStock)
                .build();
        stockRepository.save(stock);

        return ProductDTO.fromEntity(savedProduct, initialStock);
    }

    @Override
    @Transactional
    public List<ProductDTO> createProducts(List<ProductDTO> dtos) {
        log.info("Batch creating {} products", dtos.size());
        List<ProductDTO> results = new ArrayList<>();
        for (ProductDTO dto : dtos) {
            results.add(createProduct(dto));
        }
        return results;
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (StringUtils.hasText(dto.getNom())) {
            product.setName(dto.getNom().trim());
        }
        if (StringUtils.hasText(dto.getReference())) {
            product.setSku(dto.getReference().trim().toUpperCase());
        }
        if (dto.getPrixAchat() != null) {
            product.setPurchasePrice(dto.getPrixAchat());
        }
        if (dto.getPrixVente() != null) {
            product.setSellingPrice(dto.getPrixVente());
        }
        if (dto.getSeuilMinimum() != null) {
            product.setMinStockLevel(dto.getSeuilMinimum());
        }
        if (dto.getCategorie() != null || dto.getCategoryId() != null) {
            product.setCategory(resolveCategory(dto.getCategorie(), dto.getCategoryId()));
        }
        if (dto.getBarcode() != null) {
            product.setBarcode(dto.getBarcode());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getUnitOfMeasure() != null) {
            product.setUnitOfMeasure(dto.getUnitOfMeasure());
        }
        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        }

        Product updated = productRepository.save(product);
        BigDecimal currentStock = stockRepository.getTotalQuantityOnHandByProductId(updated.getId());
        return ProductDTO.fromEntity(updated, currentStock);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteProducts(List<Long> ids) {
        log.info("Batch deleting products: {}", ids);
        productRepository.deleteAllById(ids);
    }

    private Category resolveCategory(String categoryName, Long categoryId) {
        if (categoryId != null) {
            return categoryRepository.findById(categoryId).orElse(null);
        }
        if (StringUtils.hasText(categoryName)) {
            return categoryRepository.findByName(categoryName.trim())
                    .orElseGet(() -> categoryRepository.save(Category.builder()
                            .name(categoryName.trim())
                            .code("CAT-" + categoryName.trim().toUpperCase().replaceAll("[^A-Z0-9]", ""))
                            .active(true)
                            .build()));
        }
        return null;
    }

    private String generateSku() {
        long count = productRepository.count() + 1;
        return String.format("PRD-%04d", count);
    }
}
