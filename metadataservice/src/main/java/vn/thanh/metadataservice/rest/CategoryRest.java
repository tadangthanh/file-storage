package vn.thanh.metadataservice.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.thanh.metadataservice.dto.CategoryDto;
import vn.thanh.metadataservice.dto.PageResponse;
import vn.thanh.metadataservice.dto.ResponseData;
import vn.thanh.metadataservice.service.ICategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Validated
public class CategoryRest {
    private final ICategoryService categoryService;

    @PostMapping
    public ResponseData<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        return new ResponseData<>(201, "thành công", categoryService.createCategory(categoryDto));
    }

    @GetMapping("/search")
    public ResponseData<PageResponse<List<CategoryDto>>> search(Pageable pageable, @RequestParam(required = false, value = "categories") String[] categories) {
        return new ResponseData<>(200, "thành công", categoryService.getPage(pageable, categories));
    }

    @PutMapping("/{categoryId}")
    public ResponseData<CategoryDto> updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CategoryDto categoryDto) {
        return new ResponseData<>(200, "thành công", categoryService.updateCategory(categoryId, categoryDto));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseData<CategoryDto> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return new ResponseData<>(204, "thành công", null);
    }
}
