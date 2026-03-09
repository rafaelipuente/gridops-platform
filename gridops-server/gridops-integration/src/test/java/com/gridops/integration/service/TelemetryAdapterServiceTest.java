package com.gridops.integration.service;

import com.gridops.integration.client.SoapTelemetryClient;
import com.gridops.integration.dto.TelemetryDto;
import com.gridops.integration.mapper.TelemetryMapper;
import com.gridops.telemetry.schema.GetTelemetryResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ws.client.WebServiceIOException;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryAdapterServiceTest {

    @Mock
    private SoapTelemetryClient soapClient;

    @Mock
    private TelemetryMapper mapper;

    private TelemetryAdapterService service;

    @BeforeEach
    void setUp() {
        service = new TelemetryAdapterService(soapClient, mapper);
    }

    @Test
    void getTelemetry_success_returnsMappedDto() throws Exception {
        GetTelemetryResponse soapResponse = new GetTelemetryResponse();
        soapResponse.setAssetTag("SUB-PDX-001");
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(GregorianCalendar.from(
                        ZonedDateTime.now(ZoneOffset.UTC)));
        soapResponse.setTimestamp(xmlCal);
        soapResponse.setTemperatureCelsius(new BigDecimal("65.3"));
        soapResponse.setLoadPercent(new BigDecimal("72.1"));
        soapResponse.setVoltageKv(new BigDecimal("121.50"));
        soapResponse.setPowerOutputMw(new BigDecimal("45.20"));
        soapResponse.setStatus("NORMAL");

        TelemetryDto expectedDto = new TelemetryDto(
                "SUB-PDX-001", Instant.now(),
                new BigDecimal("65.3"), new BigDecimal("72.1"),
                new BigDecimal("121.50"), new BigDecimal("45.20"), "NORMAL");

        when(soapClient.getTelemetry("SUB-PDX-001")).thenReturn(soapResponse);
        when(mapper.toDto(soapResponse)).thenReturn(expectedDto);

        TelemetryDto result = service.getTelemetry("SUB-PDX-001");

        assertNotNull(result);
        assertEquals("SUB-PDX-001", result.getAssetTag());
        assertEquals("NORMAL", result.getStatus());
    }

    @Test
    void getTelemetry_soapFailure_throwsTelemetryUnavailable() {
        when(soapClient.getTelemetry("SUB-PDX-001"))
                .thenThrow(new WebServiceIOException("Connection refused"));

        TelemetryAdapterService.TelemetryUnavailableException ex =
                assertThrows(TelemetryAdapterService.TelemetryUnavailableException.class,
                        () -> service.getTelemetry("SUB-PDX-001"));

        assertEquals("Telemetry service is currently unavailable", ex.getMessage());
    }
}
