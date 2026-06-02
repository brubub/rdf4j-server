package edu.brunobudris.ke.rdf4j_server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PropertiesConfig {

    @Value("${rdf4j.protocol.version}")
    private String protocolVersion;

}
