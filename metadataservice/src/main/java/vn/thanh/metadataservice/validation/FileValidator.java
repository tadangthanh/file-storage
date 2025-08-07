package vn.thanh.metadataservice.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.thanh.metadataservice.entity.File;
import vn.thanh.metadataservice.exception.BadRequestException;
import vn.thanh.metadataservice.exception.ResourceNotFoundException;

@RequiredArgsConstructor
@Component
@Slf4j(topic = "FILE_VALIDATOR")
public class FileValidator {

    /**
     * Kiểm tra Item chưa bị xóa (deletedAt == null)
     */
    public void validateFileNotDeleted(File file) {
        if (file.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Resource đã bị xóa");
        }
    }

    /**
     * Kiểm tra Item đã bị xóa (deletedAt != null)
     */
    public void validateFileDeleted(File file) {
        if (file.getDeletedAt() == null) {
            throw new BadRequestException("Resource chưa bị xóa");
        }

    }


}
