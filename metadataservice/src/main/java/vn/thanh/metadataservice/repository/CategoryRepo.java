package vn.thanh.metadataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.thanh.metadataservice.entity.Category;

import java.util.UUID;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> , JpaSpecificationExecutor<Category> {
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name) and c.ownerId = :ownerId")
    boolean existsByOwnerAndNameIgnoreCase(@Param("name") String name,@Param("ownerId")  UUID ownerId);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.id = :id AND c.ownerId = :userId")
    boolean existsByOwnerAndCategoryId(@Param("id") Long id, @Param("userId") UUID userId);


}
