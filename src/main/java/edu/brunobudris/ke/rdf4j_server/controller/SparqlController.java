package edu.brunobudris.ke.rdf4j_server.controller;

import edu.brunobudris.ke.rdf4j_server.service.sparql.GetSparqlQueryService;
import edu.brunobudris.ke.rdf4j_server.service.sparql.PostSparqlQueryService;
import edu.brunobudris.ke.rdf4j_server.service.sparql.SparqlUpdateService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.query.explanation.Explanation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_LD_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_QUERY;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_UPDATE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_TRIX;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF_RESULTS_TABLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_RDFTRANSACTION;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_TRIG;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.BASE_URI_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.DISTINCT_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.EXPLAIN_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.INFER_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.LIMIT_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.OFFSET_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.QUERY_LN_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.QUERY_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.REPOSITORY_ID_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_BOOLEAN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_RDF_N3;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_TURTLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_X_NQUADS;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TIMEOUT_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.UPDATE_PARAM;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/repositories")
public class SparqlController {

    private final GetSparqlQueryService getSparqlQueryService;
    private final PostSparqlQueryService postSparqlQueryService;
    private final SparqlUpdateService sparqlUpdateService;

    @GetMapping(path = "/{repositoryID}", produces = {
            APPLICATION_SPARQL_RESULTS_XML,
            APPLICATION_SPARQL_RESULTS_JSON,
            APPLICATION_X_BINARY_RDF_RESULTS_TABLE,
            APPLICATION_RDF_XML,
            TEXT_PLAIN,
            TEXT_TURTLE,
            TEXT_RDF_N3,
            TEXT_BOOLEAN,
            TEXT_X_NQUADS,
            APPLICATION_LD_JSON,
            APPLICATION_RDF_JSON,
            APPLICATION_TRIX,
            APPLICATION_X_TRIG,
            APPLICATION_X_BINARY_RDF
    })
    public void executeGetQuery(
            HttpServletResponse httpServletResponse,
            @RequestHeader(name = ACCEPT, required = false) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(name = QUERY_PARAM) String query,
            @RequestParam(name = QUERY_LN_PARAM, required = false) String queryLn,
            @RequestParam(name = INFER_PARAM, required = false, defaultValue = "true") boolean inferred,
            @RequestParam(name = TIMEOUT_PARAM, required = false) Integer timeout,
            @RequestParam(name = DISTINCT_PARAM, required = false) boolean distinct,
            @RequestParam(name = LIMIT_PARAM, required = false) Integer limit,
            @RequestParam(name = OFFSET_PARAM, required = false) Integer offset,
            @RequestParam(name = EXPLAIN_PARAM, required = false) Explanation.Level level,
            @RequestParam(required = false) Map<String,String> variables
    ) throws Exception {
        try {
            getSparqlQueryService.executeQuery(httpServletResponse, acceptHeaders, repositoryID, query, queryLn,
                    inferred, timeout, distinct, limit, offset, level, variables);
            httpServletResponse.setStatus(OK.value());
            httpServletResponse.flushBuffer();
        } catch (Exception ex) {
            httpServletResponse.reset();
            throw ex;
        }
    }

    @PostMapping(path = "/{repositoryID}",
            consumes = {
                APPLICATION_SPARQL_QUERY
            },
            produces = {
                APPLICATION_SPARQL_RESULTS_XML,
                APPLICATION_SPARQL_RESULTS_JSON,
                APPLICATION_X_BINARY_RDF_RESULTS_TABLE,
                APPLICATION_RDF_XML,
                TEXT_PLAIN,
                TEXT_TURTLE,
                TEXT_RDF_N3,
                TEXT_BOOLEAN,
                TEXT_X_NQUADS,
                APPLICATION_LD_JSON,
                APPLICATION_RDF_JSON,
                APPLICATION_TRIX,
                APPLICATION_X_TRIG,
                APPLICATION_X_BINARY_RDF
            }
    )
    public void executePostQuery(
            HttpServletResponse httpServletResponse,
            @RequestHeader(name = ACCEPT, required = false) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestBody String query
    ) throws Exception {
        try {
            postSparqlQueryService.executeQuery(httpServletResponse, acceptHeaders, repositoryID, query);
            httpServletResponse.setStatus(OK.value());
            httpServletResponse.flushBuffer();
        } catch (Exception ex) {
            httpServletResponse.reset();
            throw ex;
        }
    }

    @PostMapping(path = "/{repositoryID}",
            consumes = {
                    APPLICATION_FORM_URLENCODED_VALUE
            },
            produces = {
                    APPLICATION_SPARQL_RESULTS_XML,
                    APPLICATION_SPARQL_RESULTS_JSON,
                    APPLICATION_X_BINARY_RDF_RESULTS_TABLE,
                    APPLICATION_RDF_XML,
                    TEXT_PLAIN,
                    TEXT_TURTLE,
                    TEXT_RDF_N3,
                    TEXT_BOOLEAN,
                    TEXT_X_NQUADS,
                    APPLICATION_LD_JSON,
                    APPLICATION_RDF_JSON,
                    APPLICATION_TRIX,
                    APPLICATION_X_TRIG,
                    APPLICATION_X_BINARY_RDF
            }
    )
    public void executeUrlEncodedQuery(
            HttpServletResponse httpServletResponse,
            @RequestHeader(name = ACCEPT, required = false) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(QUERY_PARAM) String query
    ) throws Exception {
        try {
            postSparqlQueryService.executeQuery(httpServletResponse, acceptHeaders, repositoryID, query);
            httpServletResponse.setStatus(OK.value());
            httpServletResponse.flushBuffer();
        } catch (Exception ex) {
            httpServletResponse.reset();
            throw ex;
        }
    }

    @PostMapping(path = "/{repositoryID}/statements",
            consumes = {
                    APPLICATION_SPARQL_UPDATE,
                    APPLICATION_RDF_XML,
                    TEXT_PLAIN,
                    TEXT_TURTLE,
                    TEXT_RDF_N3,
                    TEXT_X_NQUADS,
                    APPLICATION_LD_JSON,
                    APPLICATION_RDF_JSON,
                    APPLICATION_TRIX,
                    APPLICATION_X_TRIG,
                    APPLICATION_X_BINARY_RDF,
                    APPLICATION_X_RDFTRANSACTION
            }
    )
    @ResponseStatus(NO_CONTENT)
    public void executePostUpdate(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(value = BASE_URI_PARAM, required = false) String baseUri,
            @RequestHeader(CONTENT_TYPE) String contentType,
            InputStream requestBody
    ) {
        sparqlUpdateService.update(contentType, repositoryID, baseUri, requestBody);
    }

    @PostMapping(path = "/{repositoryID}/statements", consumes = APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(NO_CONTENT)
    public void executeUrlEncodedUpdate(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(value = BASE_URI_PARAM, required = false) String baseUri,
            @RequestParam(UPDATE_PARAM) String updateStr
    ) {
        ByteArrayInputStream updateAsIS = new ByteArrayInputStream(updateStr.getBytes());
        sparqlUpdateService.update(APPLICATION_FORM_URLENCODED_VALUE, repositoryID, baseUri, updateAsIS);
    }
}
