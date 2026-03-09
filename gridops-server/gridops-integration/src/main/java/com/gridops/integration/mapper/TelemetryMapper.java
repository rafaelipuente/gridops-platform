package com.gridops.integration.mapper;

import com.gridops.integration.dto.TelemetryDto;
import com.gridops.telemetry.schema.GetTelemetryResponse;

import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Instant;

@Component
public class TelemetryMapper {

    public TelemetryDto toDto(GetTelemetryResponse response) {
        return new TelemetryDto(
                response.getAssetTag(),
                toInstant(response.getTimestamp()),
                response.getTemperatureCelsius(),
                response.getLoadPercent(),
                response.getVoltageKv(),
                response.getPowerOutputMw(),
                response.getStatus()
        );
    }

    private Instant toInstant(XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar().toInstant();
    }
}
