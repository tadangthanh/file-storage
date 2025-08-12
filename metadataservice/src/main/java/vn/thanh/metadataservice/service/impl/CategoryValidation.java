package vn.thanh.metadataservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.thanh.metadataservice.exception.ResourceAlreadyExistsException;
import vn.thanh.metadataservice.repository.CategoryRepo;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "CATEGORY_VALIDATION")
public class CategoryValidation {
    private final CategoryRepo categoryRepo;

    // check category not exist by owner
    public void validNotExistsByNameAndOwnerId(String name, UUID ownerId) {
        boolean isExists = categoryRepo.existsByOwnerAndNameIgnoreCase(name.trim(), ownerId);
        if (isExists) {
            log.info("category {} is exists", name);
            throw new ResourceAlreadyExistsException("Danh mục: " + name + " đã tồn tại");
        }
    }

    // check category belong to user
    public void validIsOwnerCategory(Long categoryId, UUID userId) {
        boolean isExists = categoryRepo.existsByOwnerAndCategoryId(categoryId, userId);
        if (!isExists) {
            log.info("user categoryId {} are not owner category {}",userId,categoryId);
            throw new ResourceAlreadyExistsException("Bạn không phải người tạo ra danh mục này");
        }
    }

}
