package vn.thanh.storageservice.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.security.access.AccessDeniedException;
import vn.thanh.storageservice.exception.ErrorResponse;
import vn.thanh.storageservice.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(body);
                String message = node.has("message") ? node.get("message").asText() : "Lỗi không xác định";

                if (response.status() == 404) {
                    return new ResourceNotFoundException(message);
                }
                if (response.status() == 403) {
                    return new AccessDeniedException(message);
                }
            }
        } catch (IOException ex) {
            // Nếu parse lỗi thì trả fallback message
            if (response.status() == 404) {
                return new ResourceNotFoundException("Không tìm thấy tài nguyên");
            }
            if (response.status() == 403) {
                return new AccessDeniedException("Access denied from remote API");
            }
        }

        // Các lỗi khác để mặc định xử lý
        return defaultDecoder.decode(methodKey, response);
    }

}
