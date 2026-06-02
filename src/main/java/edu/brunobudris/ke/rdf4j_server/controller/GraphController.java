package edu.brunobudris.ke.rdf4j_server.controller;

import edu.brunobudris.ke.rdf4j_server.service.graph.AddGraphStatementsService;
import edu.brunobudris.ke.rdf4j_server.service.graph.ClearGraphStatementsService;
import edu.brunobudris.ke.rdf4j_server.service.graph.FetchGraphStatementsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_LD_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_TRIX;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_TRIG;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.DEFAULT_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.GRAPH_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.REPOSITORY_ID_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_RDF_N3;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_TURTLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_X_NQUADS;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "/repositories")
@RequiredArgsConstructor
public class GraphController {

    private final FetchGraphStatementsService fetchStatementsService;
    private final AddGraphStatementsService addStatementsService;
    private final ClearGraphStatementsService clearStatementsService;

    @GetMapping(path = "/{repositoryID}/rdf-graphs/{graph}", produces = {
            APPLICATION_RDF_XML,
            TEXT_PLAIN,
            TEXT_TURTLE,
            TEXT_RDF_N3,
            TEXT_X_NQUADS,
            APPLICATION_LD_JSON,
            APPLICATION_RDF_JSON,
            APPLICATION_TRIX,
            APPLICATION_X_TRIG,
            APPLICATION_X_BINARY_RDF
    })
    public void getAllStatements(
            HttpServletResponse servletResponse,
            @RequestHeader(ACCEPT) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(GRAPH_PARAM) String graph
    ) throws Exception {
        try {
            fetchStatementsService.getAllStatements(servletResponse, acceptHeaders, repositoryID, graph, false);
            servletResponse.setStatus(OK.value());
            servletResponse.flushBuffer();
        } catch (Exception ex) {
            servletResponse.reset();
            throw ex;
        }
    }

    @PostMapping(path = "/{repositoryID}/rdf-graphs/{graph}", consumes = {
            APPLICATION_RDF_XML,
            TEXT_PLAIN,
            TEXT_TURTLE,
            TEXT_RDF_N3,
            TEXT_X_NQUADS,
            APPLICATION_LD_JSON,
            APPLICATION_RDF_JSON,
            APPLICATION_TRIX,
            APPLICATION_X_TRIG,
            APPLICATION_X_BINARY_RDF
    })
    @ResponseStatus(NO_CONTENT)
    public void addStatements(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(GRAPH_PARAM) String graph,
            @RequestHeader(CONTENT_TYPE) String contentType,
            InputStream requestBody
    ) {
        addStatementsService.addStatements(contentType, repositoryID, graph, requestBody);
    }

    @DeleteMapping(path = "/{repositoryID}/rdf-graphs/{graph}")
    @ResponseStatus(NO_CONTENT)
    public void clearGraph(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(GRAPH_PARAM) String graph
    ) {
        clearStatementsService.clearGraph(repositoryID, graph);
    }

    @GetMapping(path = "/{repositoryID}/rdf-graphs", produces = {
            APPLICATION_RDF_XML,
            TEXT_PLAIN,
            TEXT_TURTLE,
            TEXT_RDF_N3,
            TEXT_X_NQUADS,
            APPLICATION_LD_JSON,
            APPLICATION_RDF_JSON,
            APPLICATION_TRIX,
            APPLICATION_X_TRIG,
            APPLICATION_X_BINARY_RDF
    })
    public void getAllStatements(
            HttpServletResponse servletResponse,
            @RequestHeader(ACCEPT) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(value = GRAPH_PARAM, required = false) String namedGraph,
            @RequestParam(value = DEFAULT_PARAM, required = false) boolean defaultGraph
    ) throws Exception {
        try {
            fetchStatementsService.getAllStatements(servletResponse, acceptHeaders, repositoryID, namedGraph, defaultGraph);
            servletResponse.setStatus(OK.value());
            servletResponse.flushBuffer();
        } catch (Exception ex) {
            servletResponse.reset();
            throw ex;
        }
    }
}
