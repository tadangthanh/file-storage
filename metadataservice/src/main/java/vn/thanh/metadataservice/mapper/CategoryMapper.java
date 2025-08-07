package vn.thanh.metadataservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.thanh.metadataservice.dto.CategoryDto;
import vn.thanh.metadataservice.entity.Category;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface CategoryMapper {
    Category toEntity(CategoryDto categoryDto);

    CategoryDto toDto(Category category);

    @Mapping(target = "id",ignore = true)
    void update(@MappingTarget Category category, CategoryDto categoryDto);
}
