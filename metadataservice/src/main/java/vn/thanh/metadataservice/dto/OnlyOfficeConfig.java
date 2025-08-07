package vn.thanh.metadataservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OnlyOfficeConfig {
    private Long fileId;
    private String documentKey;
    private String documentTitle;
    private String fileType;
    private String documentType;
    private String documentUrl;
    private String callbackUrl;

    // Thông tin quyền của người dùng
    private Permissions permissions;

    // Thông tin người dùng
    private User user;

    // Nested class để lưu quyền
    @Getter
    @Setter
    public static class Permissions {
        private boolean edit;
        private boolean comment;
        private boolean download;
    }

    // Nested class để lưu thông tin người dùng
    @Getter
    @Setter
    public static class User {
        private UUID id;
        private String name;
    }
}
