package com.gridops.integration.client;

import com.gridops.telemetry.schema.GetTelemetryRequest;
import com.gridops.telemetry.schema.GetTelemetryResponse;

import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.stereotype.Component;

@Component
public class SoapTelemetryClient {

    private final WebServiceTemplate webServiceTemplate;

    public SoapTelemetryClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public GetTelemetryResponse getTelemetry(String assetTag) {
        GetTelemetryRequest request = new GetTelemetryRequest();
        request.setAssetTag(assetTag);

        return (GetTelemetryResponse) webServiceTemplate
                .marshalSendAndReceive(request);
    }
}
