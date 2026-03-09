package com.gridops.integration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class SoapClientConfig {

    @Value("${gridops.telemetry.soap-url}")
    private String soapUrl;

    @Bean
    public Jaxb2Marshaller telemetryMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.gridops.telemetry.schema");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate telemetryWebServiceTemplate(Jaxb2Marshaller telemetryMarshaller) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(telemetryMarshaller);
        template.setUnmarshaller(telemetryMarshaller);
        template.setDefaultUri(soapUrl);
        return template;
    }
}
