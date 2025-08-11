package vn.thanh.storageservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.entity.Version;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VersionMapper {
    VersionDto toDto(Version version);

    Version toEntity(VersionDto versionDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "versionNumber", ignore = true)
    void updateVersionIgnoreVNumber(@MappingTarget Version version, VersionDto versionDto);
}
