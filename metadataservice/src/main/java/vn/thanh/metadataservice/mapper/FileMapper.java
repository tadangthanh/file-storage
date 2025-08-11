package vn.thanh.metadataservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.thanh.metadataservice.dto.FileRequest;
import vn.thanh.metadataservice.dto.FileResponse;
import vn.thanh.metadataservice.dto.MetadataRequest;
import vn.thanh.metadataservice.dto.MetadataUpdate;
import vn.thanh.metadataservice.entity.File;

import java.util.List;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface FileMapper {
    FileResponse toResponse(File file);

    void updateFile(@MappingTarget File file, FileRequest fileRequest);

    @Mapping(target = "id", ignore = true)
    void copyFile(@MappingTarget File fileDes, File file);

    void updateMetadata(@MappingTarget File file, MetadataUpdate metadataUpdate);

    File toFile(MetadataRequest metadataRequest);

    List<FileResponse> toResponse(List<File> files);
}
