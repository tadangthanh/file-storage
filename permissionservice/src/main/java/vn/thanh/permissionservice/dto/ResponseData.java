package vn.thanh.permissionservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // cho Jackson deserialize
@AllArgsConstructor // nếu muốn xài constructor full
public class ResponseData<T> {
    private int status;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // put,patch,delete
    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }

}

