package edu.brunobudris.ke.rdf4j_server.controller;

import edu.brunobudris.ke.rdf4j_server.config.PropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;

@RestController
@RequestMapping("/protocol")
@RequiredArgsConstructor
public class ProtocolController {

    private final PropertiesConfig propertiesConfig;

    @GetMapping(produces = TEXT_PLAIN)
    public String getProtocolVersion() {
        return propertiesConfig.getProtocolVersion();
    }
}
