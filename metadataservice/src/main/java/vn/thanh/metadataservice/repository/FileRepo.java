package vn.thanh.metadataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.thanh.metadataservice.entity.File;

import java.util.List;

@Repository
public interface FileRepo extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {
    @Query("select f from File f where f.category.id = ?1")
    List<File> getFilesByCategoryId(Long categoryId);
}
