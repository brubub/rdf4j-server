package edu.brunobudris.ke.rdf4j_server.controller;

import edu.brunobudris.ke.rdf4j_server.service.namespaces.DeleteNamespacesService;
import edu.brunobudris.ke.rdf4j_server.service.namespaces.FetchNamespacesService;
import edu.brunobudris.ke.rdf4j_server.service.namespaces.NamespacesByPrefixService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF_RESULTS_TABLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.NAMESPACES_PREFIX_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.REPOSITORY_ID_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
public class NamespacesController {

    private final FetchNamespacesService fetchNamespacesService;
    private final DeleteNamespacesService deleteNamespacesService;
    private final NamespacesByPrefixService namespacesByPrefixService;

    @GetMapping(path = "/{repositoryID}/namespaces", produces = {
            APPLICATION_SPARQL_RESULTS_XML,
            APPLICATION_SPARQL_RESULTS_JSON,
            APPLICATION_X_BINARY_RDF_RESULTS_TABLE
    })
    public void getNamespaces(
            HttpServletResponse servletResponse,
            @RequestHeader(value = ACCEPT, required = false) List<String> acceptHeaders,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID
    ) throws Exception {
        try {
            fetchNamespacesService.getNamespaces(servletResponse, acceptHeaders, repositoryID);
            servletResponse.setStatus(OK.value());
            servletResponse.flushBuffer();
        } catch (Exception ex) {
            servletResponse.reset();
            throw ex;
        }
    }

    @DeleteMapping(path = "/{repositoryID}/namespaces")
    @ResponseStatus(NO_CONTENT)
    public void deleteNamespaces(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID
    ) {
        deleteNamespacesService.deleteNamespaces(repositoryID);
    }

    @GetMapping(path = "/{repositoryID}/namespaces/{namespacesPrefix}", produces = TEXT_PLAIN)
    public String getNamespaceByPrefix(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(NAMESPACES_PREFIX_PARAM) String namespacesPrefix
    ) {
        return namespacesByPrefixService.getNamespaceByPrefix(repositoryID, namespacesPrefix);
    }

    @PutMapping(path = "/{repositoryID}/namespaces/{namespacesPrefix}", consumes = TEXT_PLAIN)
    @ResponseStatus(NO_CONTENT)
    public void setNamespaceForPrefix(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(NAMESPACES_PREFIX_PARAM) String namespacesPrefix,
            @RequestBody String newNamespace
    ) {
        namespacesByPrefixService.setNamespaceForPrefix(repositoryID, namespacesPrefix, newNamespace);
    }

    @DeleteMapping(path = {"/{repositoryID}/namespaces/", "/{repositoryID}/namespaces/{namespacesPrefix}"})
    @ResponseStatus(NO_CONTENT)
    public void removeNamespaceForPrefix(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(value = NAMESPACES_PREFIX_PARAM, required = false) String namespacesPrefix
    ) {
        namespacesByPrefixService.removeNamespaceForPrefix(repositoryID, namespacesPrefix);
    }
}
