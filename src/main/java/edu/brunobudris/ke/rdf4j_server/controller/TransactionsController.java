package edu.brunobudris.ke.rdf4j_server.controller;

import edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction;
import edu.brunobudris.ke.rdf4j_server.service.transactions.AbortTransactionService;
import edu.brunobudris.ke.rdf4j_server.service.transactions.ExecuteTransactionService;
import edu.brunobudris.ke.rdf4j_server.service.transactions.StartTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.ACTION_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_LD_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_TRIX;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_TRIG;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.BASE_URI_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.INFER_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.INSERT_GRAPH_URI_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.LOCATION_HEADER;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.OBJ_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.PRED_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.QUERY_LN_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.QUERY_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.REMOVE_GRAPH_URI_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.REPOSITORY_ID_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.SUBJ_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_RDF_N3;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_TURTLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_X_NQUADS;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TRANSACTION_ID_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.UPDATE_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.USING_GRAPH_URI_PARAM;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.USING_NAMED_GRAPH_URI_PARAM;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
public class TransactionsController {

    private final StartTransactionService startTransactionService;
    private final AbortTransactionService abortTransactionService;
    private final ExecuteTransactionService executeTransactionService;

    @PostMapping("/{repositoryID}/transactions")
    public void startTransaction(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID
    ) throws IOException {
        String transactionId = startTransactionService.startTransaction(repositoryID);

        String requestURI = request.getRequestURL().toString();
        String location = requestURI + "/" + transactionId;

        response.setStatus(CREATED.value());
        response.addHeader(LOCATION_HEADER, location);
        response.flushBuffer();
    }

    @PutMapping(value = "/{repositoryID}/transactions/{transactionID}",
            consumes = {
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
            }
    )
    public void executeTransaction(
            HttpServletResponse response,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(TRANSACTION_ID_PARAM) String transactionID,
            @RequestParam(ACTION_PARAM) TransactionProtocolAction action,
            @RequestParam(value = SUBJ_PARAM, required = false) String subject,
            @RequestParam(value = PRED_PARAM, required = false) String predicate,
            @RequestParam(value = OBJ_PARAM, required = false) String object,
            @RequestParam(value = QUERY_PARAM, required = false) String query,
            @RequestParam(value = QUERY_LN_PARAM, required = false) String queryLanguage,
            @RequestParam(value = INFER_PARAM, required = false, defaultValue = "true") boolean infer,
            @RequestParam(value = UPDATE_PARAM, required = false) String update,
            @RequestParam(value = BASE_URI_PARAM, required = false) String baseURI,
            @RequestParam(value = USING_GRAPH_URI_PARAM, required = false) List<String> usingGraphUri,
            @RequestParam(value = USING_NAMED_GRAPH_URI_PARAM, required = false) List<String> usingNamedGraphUri,
            @RequestParam(value = REMOVE_GRAPH_URI_PARAM, required = false) List<String> removeGraphUri,
            @RequestParam(value = INSERT_GRAPH_URI_PARAM, required = false) List<String> insertGraphUri,
            @RequestHeader(value = ACCEPT, required = false) List<String> acceptHeaders,
            @RequestHeader(value = CONTENT_TYPE) String contentType,
            InputStream requestBody
    ) {
        try {
            executeTransactionService.executeTransaction(
                    response, repositoryID, transactionID, action, subject, predicate, object, query, queryLanguage, infer,
                    update, baseURI, usingGraphUri, usingNamedGraphUri, removeGraphUri, insertGraphUri, acceptHeaders,
                    contentType, requestBody
            );
        } catch (RuntimeException ex) {
            response.reset();
            throw ex;
        }
    }

    @PutMapping(value = "/{repositoryID}/transactions/{transactionID}", headers = "!Content-type")
    public void executeTransactionWithoutBody(
            HttpServletResponse response,
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(TRANSACTION_ID_PARAM) String transactionID,
            @RequestParam(ACTION_PARAM) TransactionProtocolAction action,
            @RequestParam(value = SUBJ_PARAM, required = false) String subject,
            @RequestParam(value = PRED_PARAM, required = false) String predicate,
            @RequestParam(value = OBJ_PARAM, required = false) String object,
            @RequestParam(value = QUERY_PARAM, required = false) String query,
            @RequestParam(value = QUERY_LN_PARAM, required = false) String queryLanguage,
            @RequestParam(value = INFER_PARAM, required = false, defaultValue = "true") boolean infer,
            @RequestParam(value = UPDATE_PARAM, required = false) String update,
            @RequestParam(value = BASE_URI_PARAM, required = false) String baseURI,
            @RequestParam(value = USING_GRAPH_URI_PARAM, required = false) List<String> usingGraphUri,
            @RequestParam(value = USING_NAMED_GRAPH_URI_PARAM, required = false) List<String> usingNamedGraphUri,
            @RequestParam(value = REMOVE_GRAPH_URI_PARAM, required = false) List<String> removeGraphUri,
            @RequestParam(value = INSERT_GRAPH_URI_PARAM, required = false) List<String> insertGraphUri,
            @RequestHeader(value = ACCEPT, required = false) List<String> acceptHeaders
    ) {
        try {
            executeTransactionService.executeTransaction(
                    response, repositoryID, transactionID, action, subject, predicate, object, query, queryLanguage, infer,
                    update, baseURI, usingGraphUri, usingNamedGraphUri, removeGraphUri, insertGraphUri, acceptHeaders,
                    null, null
            );
        } catch (RuntimeException ex) {
            response.reset();
            throw ex;
        }
    }

    @DeleteMapping("/{repositoryID}/transactions/{transactionID}")
    @ResponseStatus(NO_CONTENT)
    public void abortTransaction(
            @PathVariable(REPOSITORY_ID_PARAM) String repositoryID,
            @PathVariable(TRANSACTION_ID_PARAM) String transactionID
    ) {
        abortTransactionService.abortTransaction(transactionID);
    }
}
