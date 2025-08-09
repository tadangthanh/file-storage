package vn.thanh.storageservice.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.thanh.storageservice.dto.ResponseData;
import vn.thanh.storageservice.dto.VersionInitRequest;
import vn.thanh.storageservice.service.IVersionService;

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
public class VersionRest {
    private final IVersionService versionService;

    @PostMapping("/init")
    ResponseData<String> initVersion(@RequestBody VersionInitRequest versionInitRequest){
        return new ResponseData<>(200,"ok", versionService.initVersion(versionInitRequest));
    }

}
