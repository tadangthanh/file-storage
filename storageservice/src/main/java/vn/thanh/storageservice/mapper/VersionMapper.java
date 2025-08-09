package vn.thanh.storageservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.entity.Version;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VersionMapper {
    VersionDto toDto(Version version);

    Version toEntity(VersionDto versionDto);
}
