package vn.thanh.storageservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.security.access.AccessDeniedException;
import vn.thanh.storageservice.exception.ErrorResponse;
import vn.thanh.storageservice.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule()); // <-- quan trọng
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                ErrorResponse obj = mapper.readValue(body, ErrorResponse.class);
                if (response.status() == 404) {
                    return new ResourceNotFoundException(obj.getMessage());
                }
                if (response.status() == 403) {
                    return new AccessDeniedException(obj.getMessage());
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
