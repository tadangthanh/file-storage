package vn.thanh.metadataservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto extends BaseDto {
    @NotBlank(message = "name not empty")
    private String name;
}
