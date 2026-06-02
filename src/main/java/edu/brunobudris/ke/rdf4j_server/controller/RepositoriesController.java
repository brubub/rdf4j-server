package edu.brunobudris.ke.rdf4j_server.controller;

import edu.brunobudris.ke.rdf4j_server.service.repositories.CreateRepositoryService;
import edu.brunobudris.ke.rdf4j_server.service.repositories.DeleteRepositoryService;
import edu.brunobudris.ke.rdf4j_server.service.repositories.RemoveStatementsService;
import edu.brunobudris.ke.rdf4j_server.service.repositories.FetchStatementsService;
import edu.brunobudris.ke.rdf4j_server.service.repositories.RepositoryContextsService;
import edu.brunobudris.ke.rdf4j_server.service.repositories.RetrieveRepositoriesService;
import edu.brunobudris.ke.rdf4j_server.service.repositories.RepositorySizeService;
import edu.brunobudris.ke.rdf4j_server.service.repositories.ReplaceStatementsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_TRIX;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF_RESULTS_TABLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_TRIG;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.BASE_URI_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.CONTEXT_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.INFER_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.OBJ_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.PRED_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.REPOSITORY_ID_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.SUBJ_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_RDF_N3;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_TURTLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_X_NQUADS;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/repositories")
public class RepositoriesController {

    private final RetrieveRepositoriesService retrieveRepositoriesService;
    private final DeleteRepositoryService deleteRepositoryService;
    private final CreateRepositoryService createRepositoryService;
    private final FetchStatementsService fetchStatementsService;
    private final RemoveStatementsService removeStatementsService;
    private final ReplaceStatementsService replaceStatementsService;
    private final RepositorySizeService repositorySizeService;
    private final RepositoryContextsService repositoryContextsService;

    @GetMapping(produces = {
            APPLICATION_SPARQL_RESULTS_XML,
            APPLICATION_SPARQL_RESULTS_JSON,
            APPLICATION_X_BINARY_RDF_RESULTS_TABLE
    })
    public void getRepositories(
            HttpServletResponse servletResponse,
            @RequestHeader(ACCEPT) List<String> acceptHeaders
    ) throws Exception {
        try {
            retrieveRepositoriesService.getRepositories(servletResponse, acceptHeaders);
            servletResponse.setStatus(OK.value());
            servletResponse.flushBuffer();
        } catch (Exception ex) {
            servletResponse.reset();
            throw ex;
        }
    }

    @DeleteMapping(path = "/{repositoryID}")
    @ResponseStatus(NO_CONTENT)
    public void deleteRepository(@PathVariable(REPOSITORY_ID_PARAM) String repositoryID) {
        deleteRepositoryService.deleteRepository(repositoryID);
    }

    @PutMapping(path = "/{repositoryID}", consumes = {
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
    public void createRepository(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestHeader(CONTENT_TYPE) String contentType,
            InputStream requestBody
    ) {
        createRepositoryService.createRepository(contentType, repositoryID, requestBody);
    }

    @GetMapping(path = "/{repositoryID}/statements", produces = {
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
    public void getStatements(
            HttpServletResponse servletResponse,
            @RequestHeader(ACCEPT) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(name = SUBJ_PARAM, required = false) String subject,
            @RequestParam(name = PRED_PARAM, required = false) String predicate,
            @RequestParam(name = OBJ_PARAM, required = false) String object,
            @RequestParam(name = CONTEXT_PARAM, required = false) List<String> contexts,
            @RequestParam(name = INFER_PARAM, required = false, defaultValue = "true") String inferred
    ) throws Exception {
        try {
            fetchStatementsService.fetchStatements(servletResponse, acceptHeaders, repositoryID, subject, predicate, object, contexts, inferred);
            servletResponse.setStatus(OK.value());
            servletResponse.flushBuffer();
        } catch (Exception ex) {
            servletResponse.reset();
            throw ex;
        }
    }

    @DeleteMapping(path = "/{repositoryID}/statements")
    @ResponseStatus(NO_CONTENT)
    public void deleteStatements(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(name = SUBJ_PARAM, required = false) String subject,
            @RequestParam(name = PRED_PARAM, required = false) String predicate,
            @RequestParam(name = OBJ_PARAM, required = false) String object,
            @RequestParam(name = CONTEXT_PARAM, required = false) List<String> contexts
    ) {
        removeStatementsService.removeStatements(repositoryID, subject, predicate, object, contexts);
    }

    @PutMapping(path = "/{repositoryID}/statements", consumes = {
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
    public void replaceStatements(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(name = CONTEXT_PARAM, required = false) List<String> contexts,
            @RequestParam(name = BASE_URI_PARAM, required = false) String baseUri,
            @RequestHeader(CONTENT_TYPE) String contentType,
            InputStream requestBody
    ) {
        replaceStatementsService.replaceStatements(contentType, repositoryID, contexts, baseUri, requestBody);
    }

    @GetMapping(path = "/{repositoryID}/size", produces = TEXT_PLAIN)
    public String getSize(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @RequestParam(name = CONTEXT_PARAM, required = false) List<String> contexts
    ) {
        return repositorySizeService.getSize(repositoryID, contexts);
    }

    @GetMapping(path = "/{repositoryID}/contexts", produces = {
            APPLICATION_SPARQL_RESULTS_XML,
            APPLICATION_SPARQL_RESULTS_JSON,
            APPLICATION_X_BINARY_RDF_RESULTS_TABLE
    })
    public void getContexts(
            HttpServletResponse servletResponse,
            @RequestHeader(ACCEPT) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID
    ) throws Exception {
        try {
            repositoryContextsService.getContexts(servletResponse, repositoryID, acceptHeaders);
            servletResponse.setStatus(OK.value());
            servletResponse.flushBuffer();
        } catch (Exception ex) {
            servletResponse.reset();
            throw ex;
        }
    }
}
