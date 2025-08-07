package vn.thanh.metadataservice.utils;


import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "FILE_UTIL")
public class FileUtil {

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int idx = fileName.lastIndexOf('.');
        return (idx == -1) ? "" : fileName.substring(idx + 1).toLowerCase();
    }


    public static String generateFileName(String blobName) {
        int idx = blobName.lastIndexOf('_');
        return (idx == -1) ? blobName : blobName.substring(idx + 1);
    }

    public static String getOriginalFileName(String blobName) {
        int first = blobName.indexOf('_');
        int last = blobName.lastIndexOf('_');
        if (first < 0 || last <= first) return blobName;
        return blobName.substring(first + 1, last);
    }
}