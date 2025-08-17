package vn.thanh.permissionservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.thanh.permissionservice.dto.PermissionAddRequest;
import vn.thanh.permissionservice.dto.PermissionDto;
import vn.thanh.permissionservice.dto.PermissionRequest;
import vn.thanh.permissionservice.dto.ResponseData;
import vn.thanh.permissionservice.service.IPermissionService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/permissions")
@Validated
public class PermissionRest {
    private final IPermissionService permissionService;

    @PostMapping("/assign")
    public ResponseData<PermissionDto> assignPer(@Valid @RequestBody PermissionRequest request) {
        return new ResponseData<>(201, "Thành công", permissionService.assignPermission(request));
    }

    @PostMapping("/add/{permissionId}")
    public ResponseData<PermissionDto> addPer(@PathVariable Long permissionId, @Valid @RequestBody PermissionAddRequest request) {
        return new ResponseData<>(201, "Thành công", permissionService.addPermission(permissionId, request));
    }

    @DeleteMapping("/{permissionId}")
    public ResponseData<Void> delPer(@PathVariable Long permissionId) {
        permissionService.deletePermissionById(permissionId);
        return new ResponseData<>(204, "Thành công", null);
    }
}
