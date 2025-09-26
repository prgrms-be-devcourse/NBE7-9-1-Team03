package com.coffee.product.controller;

import com.coffee.global.rsData.RsData;
import com.coffee.product.dto.ProductDto;
import com.coffee.product.entity.Product;
import com.coffee.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "ApiV1ProductController", description = "상품 API")
public class ProductController {

    private final ProductService productService;

    // 1) 전체 조회
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "상품 다건 조회")
    public List<ProductDto> getItems() {
        return productService.findAll().reversed().stream()
                .map(ProductDto::new)
                .toList();
    }

    // 2) 단건 조회
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "상품 단건 조회")
    public ProductDto getItem(@PathVariable Long id) {
        Product p = productService.findById(id).get();
        return new ProductDto(p);
    }

    public record ProductWriteReqBody(
            @NotBlank @Size(min=2, max=50) String name,
            @Min(0) int price,
            @Min(0) int stock
    ) {}

    public record ProductWriteResBody(ProductDto productDto) {}

    // 3) 생성
    @PostMapping
    @Transactional
    @Operation(summary = "상품 생성")
    public RsData<ProductWriteResBody> createItem(@RequestBody @Valid ProductWriteReqBody req) {
        Product p = productService.create(req.name(), req.price(), req.stock());
        return new RsData<>(
                "201-1",
                "%d번 상품이 생성되었습니다.".formatted(p.getId()),
                new ProductWriteResBody(new ProductDto(p))
        );
    }

    // 수정 바디
    public record ProductModifyReqBody(
            @NotBlank @Size(min=2, max=50) String name,
            @Min(0) int price,
            @Min(0) int stock
    ) {}

    // 4) 수정
    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "상품 수정")
    public RsData<Void> modifyItem(@PathVariable Long id, @RequestBody @Valid ProductModifyReqBody req) {
        Product p = productService.findById(id).get();
        productService.modify(p, req.name(), req.price(), req.stock());
        return new RsData<>("200-1", "%d번 상품이 수정되었습니다.".formatted(id));
    }

    // 5) 삭제
    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "상품 삭제")
    public RsData<Void> deleteItem(@PathVariable Long id) {
        Product p = productService.findById(id).get();
        productService.delete(p);
        return new RsData<>("200-1", "%d번 상품이 삭제되었습니다.".formatted(id));
    }
}
