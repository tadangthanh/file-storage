package vn.thanh.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserResponse extends BaseDto implements Serializable {
    private Long id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private int totalDocuments;
}
