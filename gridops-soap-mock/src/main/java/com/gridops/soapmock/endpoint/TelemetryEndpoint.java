package com.gridops.soapmock.endpoint;

import com.gridops.telemetry.schema.GetTelemetryRequest;
import com.gridops.telemetry.schema.GetTelemetryResponse;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

@Endpoint
public class TelemetryEndpoint {

    private static final String NAMESPACE_URI = "http://gridops.com/telemetry";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetTelemetryRequest")
    @ResponsePayload
    public GetTelemetryResponse getTelemetry(@RequestPayload GetTelemetryRequest request) {
        String assetTag = request.getAssetTag();
        int seed = Math.abs(assetTag.hashCode());

        long minuteBucket = Instant.now().getEpochSecond() / 60;
        int variation = (int) ((seed + minuteBucket) % 20) - 10;

        BigDecimal temperature = BigDecimal.valueOf(55 + (seed % 30) + variation)
                .setScale(1, RoundingMode.HALF_UP);
        BigDecimal load = BigDecimal.valueOf(40 + (seed % 45) + (variation / 2))
                .setScale(1, RoundingMode.HALF_UP);
        BigDecimal voltage = BigDecimal.valueOf(110 + (seed % 25) + (variation * 0.3))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal power = BigDecimal.valueOf(20 + (seed % 60) + (variation * 0.5))
                .setScale(2, RoundingMode.HALF_UP);

        load = load.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
        power = power.max(BigDecimal.ZERO);

        String status;
        if (temperature.compareTo(BigDecimal.valueOf(80)) > 0) {
            status = "CRITICAL";
        } else if (temperature.compareTo(BigDecimal.valueOf(70)) > 0) {
            status = "WARNING";
        } else {
            status = "NORMAL";
        }

        GetTelemetryResponse response = new GetTelemetryResponse();
        response.setAssetTag(assetTag);
        response.setTimestamp(toXmlCalendar());
        response.setTemperatureCelsius(temperature);
        response.setLoadPercent(load);
        response.setVoltageKv(voltage);
        response.setPowerOutputMw(power);
        response.setStatus(status);
        return response;
    }

    private XMLGregorianCalendar toXmlCalendar() {
        try {
            GregorianCalendar cal = GregorianCalendar.from(
                    ZonedDateTime.now(ZoneOffset.UTC));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Failed to create XMLGregorianCalendar", e);
        }
    }
}
