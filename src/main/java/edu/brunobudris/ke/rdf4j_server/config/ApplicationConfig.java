package edu.brunobudris.ke.rdf4j_server.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.tomcat.util.buf.EncodedSolidusHandling.DECODE;

@Configuration
public class ApplicationConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory ->
                factory.addConnectorCustomizers(connector ->
                        connector.setEncodedSolidusHandling(DECODE.getValue()));
    }

}
