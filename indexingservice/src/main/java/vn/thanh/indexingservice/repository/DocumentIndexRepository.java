package vn.thanh.indexingservice.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import vn.thanh.indexingservice.entity.DocumentIndex;

@Repository
public interface DocumentIndexRepository
        extends ElasticsearchRepository<DocumentIndex, String>, DocumentIndexRepositoryCustom {
}