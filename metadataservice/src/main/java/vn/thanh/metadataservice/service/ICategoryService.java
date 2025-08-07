package vn.thanh.metadataservice.service;

import org.springframework.data.domain.Pageable;
import vn.thanh.metadataservice.dto.CategoryDto;
import vn.thanh.metadataservice.dto.PageResponse;
import vn.thanh.metadataservice.entity.Category;

import java.util.List;

public interface ICategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);

    void deleteCategoryById(Long id);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    Category getCategoryByIdOrThrow(Long id);

    PageResponse<List<CategoryDto>> getPage(Pageable pageable, String[] categories);
}
