package com.gridops.integration.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class TelemetryDto {

    private String assetTag;
    private Instant timestamp;
    private BigDecimal temperatureCelsius;
    private BigDecimal loadPercent;
    private BigDecimal voltageKv;
    private BigDecimal powerOutputMw;
    private String status;

    public TelemetryDto() {
    }

    public TelemetryDto(String assetTag, Instant timestamp, BigDecimal temperatureCelsius,
                        BigDecimal loadPercent, BigDecimal voltageKv,
                        BigDecimal powerOutputMw, String status) {
        this.assetTag = assetTag;
        this.timestamp = timestamp;
        this.temperatureCelsius = temperatureCelsius;
        this.loadPercent = loadPercent;
        this.voltageKv = voltageKv;
        this.powerOutputMw = powerOutputMw;
        this.status = status;
    }

    public String getAssetTag() {
        return assetTag;
    }

    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(BigDecimal temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public BigDecimal getLoadPercent() {
        return loadPercent;
    }

    public void setLoadPercent(BigDecimal loadPercent) {
        this.loadPercent = loadPercent;
    }

    public BigDecimal getVoltageKv() {
        return voltageKv;
    }

    public void setVoltageKv(BigDecimal voltageKv) {
        this.voltageKv = voltageKv;
    }

    public BigDecimal getPowerOutputMw() {
        return powerOutputMw;
    }

    public void setPowerOutputMw(BigDecimal powerOutputMw) {
        this.powerOutputMw = powerOutputMw;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
