package vn.thanh.metadataservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.thanh.metadataservice.dto.CategoryDto;
import vn.thanh.metadataservice.dto.PageResponse;
import vn.thanh.metadataservice.dto.PaginationUtils;
import vn.thanh.metadataservice.entity.Category;
import vn.thanh.metadataservice.exception.ResourceNotFoundException;
import vn.thanh.metadataservice.mapper.CategoryMapper;
import vn.thanh.metadataservice.repository.CategoryRepo;
import vn.thanh.metadataservice.repository.specification.EntitySpecificationsBuilder;
import vn.thanh.metadataservice.repository.specification.SpecificationUtil;
import vn.thanh.metadataservice.service.ICategoryService;
import vn.thanh.metadataservice.service.IFileService;
import vn.thanh.metadataservice.utils.AuthUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "CATEGORY_SERVICE")
public class CategoryServiceImpl implements ICategoryService {
    private final CategoryRepo categoryRepo;
    private final CategoryMapper categoryMapper;
    private final CategoryValidation categoryValidation;
    private final IFileService fileService;

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        UUID userId = AuthUtils.getUserId();
        categoryValidation.validNotExistsByNameAndOwnerId(categoryDto.getName(), userId);
        log.info("create category: {} by: {}", categoryDto.getName(), userId);
        Category category = categoryMapper.toEntity(categoryDto);
        category.setOwnerId(userId);
        category = categoryRepo.save(category);
        return categoryMapper.toDto(category);
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryValidation.validIsOwnerCategory(id, AuthUtils.getUserId());
        log.info("delete category by id: {}", id);
        // di chuyển file ra khỏi entity sẽ xóa 
        fileService.detachedCategory(id);
        categoryRepo.deleteById(id);
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        log.info("update category id: {}, new name: {}", categoryId, categoryDto.getName());
        UUID userId= AuthUtils.getUserId();
        categoryValidation.validIsOwnerCategory(categoryId, userId);
        categoryValidation.validNotExistsByNameAndOwnerId(categoryDto.getName(),userId);
        Category categoryExists = getCategoryByIdOrThrow(categoryId);
        categoryMapper.update(categoryExists, categoryDto);
        categoryExists = categoryRepo.save(categoryExists);
        return categoryMapper.toDto(categoryExists);
    }

    @Override
    public Category getCategoryByIdOrThrow(Long id) {
        return categoryRepo.findById(id).orElseThrow(() -> {
            log.info("category not found: {}", id);
            return new ResourceNotFoundException("không tìm thấy danh mục này");
        });
    }

    @Override
    public PageResponse<List<CategoryDto>> getPage(Pageable pageable, String[] categories) {
        EntitySpecificationsBuilder<Category> builder = new EntitySpecificationsBuilder<>();
        Specification<Category> spec = Specification.allOf();
        UUID ownerId = AuthUtils.getUserId();
        if (categories != null && categories.length > 0) {
            spec = SpecificationUtil.buildSpecificationFromFilters(categories, builder);
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ownerId"), ownerId));
            Page<Category> pageAccessByResource = categoryRepo.findAll(spec, pageable);
            return PaginationUtils.convertToPageResponse(pageAccessByResource, pageable, categoryMapper::toDto);
        }
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ownerId"), ownerId));
        return PaginationUtils.convertToPageResponse(categoryRepo.findAll(spec, pageable), pageable, categoryMapper::toDto);
    }
}
