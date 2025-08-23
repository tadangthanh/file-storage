package vn.thanh.indexingservice.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import vn.thanh.indexingservice.exception.CustomIOException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class DocumentIndexRepositoryImpl implements DocumentIndexRepositoryCustom {

    private final ElasticsearchClient elasticsearchClient;


    @Override
    public void markDocumentsIndex(List<Long> documentIds, boolean value) {
        try {
            // Xây dựng UpdateByQuery request
            UpdateByQueryRequest.Builder updateRequestBuilder = new UpdateByQueryRequest.Builder()
                    .index("documents")  // Tên của index
                    .query(q -> q
                            .terms(t -> t
                                    .field("documentId")  // Trường id hoặc có thể là "documentId"
                                    .terms(tq -> tq
                                            .value(documentIds.stream()
                                                    .map(FieldValue::of)
                                                    .collect(Collectors.toList()))  // Duyệt qua documentIds và chuyển thành FieldValue
                                    )
                            )
                    )
                    .script(s -> s
                            .source("ctx._source.isDeleted = params.isDeleted")  // Đánh dấu xóa (soft delete)
                            .lang("painless")
                            .params(Map.of("isDeleted", JsonData.of(value)))  // Sử dụng JsonData.of để chuyển đổi value thành kiểu JsonData
                    );

            // Thực hiện update
            elasticsearchClient.updateByQuery(updateRequestBuilder.build());

            log.info("Successfully marked documents as deleted or restored");

        } catch (IOException e) {
            log.error("Error marking documents as deleted: {}", e.getMessage(), e);
            throw new CustomIOException("Failed to mark documents as deleted");
        }
    }
}
