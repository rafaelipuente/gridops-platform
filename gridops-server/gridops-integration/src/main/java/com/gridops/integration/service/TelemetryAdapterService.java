package com.gridops.integration.service;

import com.gridops.integration.client.SoapTelemetryClient;
import com.gridops.integration.dto.TelemetryDto;
import com.gridops.integration.mapper.TelemetryMapper;
import com.gridops.telemetry.schema.GetTelemetryResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceIOException;

@Service
public class TelemetryAdapterService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryAdapterService.class);

    private final SoapTelemetryClient soapClient;
    private final TelemetryMapper mapper;

    public TelemetryAdapterService(SoapTelemetryClient soapClient, TelemetryMapper mapper) {
        this.soapClient = soapClient;
        this.mapper = mapper;
    }

    public TelemetryDto getTelemetry(String assetTag) {
        try {
            GetTelemetryResponse response = soapClient.getTelemetry(assetTag);
            return mapper.toDto(response);
        } catch (WebServiceIOException e) {
            log.error("Telemetry service unavailable for asset {}: {}", assetTag, e.getMessage());
            throw new TelemetryUnavailableException(
                    "Telemetry service is currently unavailable", e);
        }
    }

    public static class TelemetryUnavailableException extends RuntimeException {
        public TelemetryUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
