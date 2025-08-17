package vn.thanh.metadataservice.annotation;



import vn.thanh.metadataservice.dto.ResourceType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    ResourceType resourceType();   // DOCUMENT, CATEGORY, ...
    String resourceParam();        // tên @PathVariable hoặc @RequestParam chứa resourceId
    int permissionBit();           // bit cần check (READ, WRITE, DELETE...)
}