package com.gridops.integration.mapper;

import com.gridops.integration.dto.TelemetryDto;
import com.gridops.telemetry.schema.GetTelemetryResponse;

import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TelemetryMapperTest {

    private final TelemetryMapper mapper = new TelemetryMapper();

    @Test
    void toDto_mapsAllFields() throws Exception {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(now));

        GetTelemetryResponse response = new GetTelemetryResponse();
        response.setAssetTag("SUB-PDX-001");
        response.setTimestamp(xmlCal);
        response.setTemperatureCelsius(new BigDecimal("65.3"));
        response.setLoadPercent(new BigDecimal("72.1"));
        response.setVoltageKv(new BigDecimal("121.50"));
        response.setPowerOutputMw(new BigDecimal("45.20"));
        response.setStatus("NORMAL");

        TelemetryDto dto = mapper.toDto(response);

        assertEquals("SUB-PDX-001", dto.getAssetTag());
        assertNotNull(dto.getTimestamp());
        assertEquals(new BigDecimal("65.3"), dto.getTemperatureCelsius());
        assertEquals(new BigDecimal("72.1"), dto.getLoadPercent());
        assertEquals(new BigDecimal("121.50"), dto.getVoltageKv());
        assertEquals(new BigDecimal("45.20"), dto.getPowerOutputMw());
        assertEquals("NORMAL", dto.getStatus());
    }

    @Test
    void toDto_preservesWarningStatus() throws Exception {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(now));

        GetTelemetryResponse response = new GetTelemetryResponse();
        response.setAssetTag("TRF-PDX-010");
        response.setTimestamp(xmlCal);
        response.setTemperatureCelsius(new BigDecimal("75.0"));
        response.setLoadPercent(new BigDecimal("88.5"));
        response.setVoltageKv(new BigDecimal("115.20"));
        response.setPowerOutputMw(new BigDecimal("30.00"));
        response.setStatus("WARNING");

        TelemetryDto dto = mapper.toDto(response);

        assertEquals("TRF-PDX-010", dto.getAssetTag());
        assertEquals("WARNING", dto.getStatus());
    }
}
