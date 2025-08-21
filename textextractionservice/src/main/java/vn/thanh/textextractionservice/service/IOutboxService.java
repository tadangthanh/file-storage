package vn.thanh.textextractionservice.service;


import vn.thanh.textextractionservice.dto.DocumentIndexMessage;

public interface IOutboxService {
    void addEventTextExtracted(DocumentIndexMessage message);
}
