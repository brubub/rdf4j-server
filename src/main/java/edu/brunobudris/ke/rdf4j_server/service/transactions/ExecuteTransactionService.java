package edu.brunobudris.ke.rdf4j_server.service.transactions;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction;
import edu.brunobudris.ke.rdf4j_server.service.remover.SesameTolerantRDFRemover;
import jakarta.annotation.Nullable;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.BAD_REQUEST;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.CONFLICT;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.ADD;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.COMMIT;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.DELETE;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.GET;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.PING;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.QUERY;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.SIZE;
import static edu.brunobudris.ke.rdf4j_server.model.TransactionProtocolAction.UPDATE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.RdfInOutUtils.initParser;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.getRDFFormatByContentType;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.getRDFFormatOrDefaultIfAcceptable;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleResource;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleURI;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleValue;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.getRdfHandlerAndSetContentType;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.getResultWriterAndSetContentType;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.prepareBooleanResponse;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.prepareQuery;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.reportGraphResult;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.reportTupleResult;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;
import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.eclipse.rdf4j.rio.Rio.createWriter;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
public class ExecuteTransactionService {

    private final RepositoryManager repositoryManager;
    private final ActiveConnectionRegistry activeConnectionRegistry;

    public void executeTransaction(
            HttpServletResponse response,
            String repositoryID,
            String transactionID,
            TransactionProtocolAction action,
            @Nullable String subject,
            @Nullable String predicate,
            @Nullable String object,
            @Nullable String query,
            @Nullable String queryLanguage,
            boolean infer,
            @Nullable String update,
            @Nullable String baseURI,
            @Nullable List<String> usingGraphUri,
            @Nullable List<String> usingNamedGraphUri,
            @Nullable List<String> removeGraphUri,
            @Nullable List<String> insertGraphUri,
            @Nullable List<String> acceptHeaders,
            @Nullable String contentType,
            @Nullable InputStream requestBody
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        RepositoryConnection connection = activeConnectionRegistry.findConnection(transactionID)
                .orElseThrow(() -> new CommonControllerException(NOT_FOUND, "Transaction not found"));

        if (ADD == action) {
            executeAddOperation(connection, baseURI, contentType, requestBody);
            response.setStatus(NO_CONTENT.value());
        } else if (DELETE == action) {
            executeDeleteOperation(connection, contentType, requestBody);
            response.setStatus(NO_CONTENT.value());
        } else if (GET == action) {
            executeGetOperation(response, acceptHeaders, connection, subject, predicate, object, infer);
            response.setStatus(OK.value());
        } else if (QUERY == action) {
            executeQueryOperation(response, acceptHeaders, connection, query, queryLanguage, infer);
            response.setStatus(OK.value());
        } else if (UPDATE == action) {
            executeUpdateOperation(connection, update, baseURI);
            response.setStatus(NO_CONTENT.value());
        } else if (SIZE == action) {
            executeSizeOperation(response, connection);
            response.setStatus(OK.value());
        } else if (COMMIT == action) {
            executeCommitOperation(connection, transactionID);
            response.setStatus(OK.value());
        }  else if (PING == action) {
            response.setStatus(NO_CONTENT.value());
        } else {
            throw new CommonControllerException(INTERNAL_ERROR, "Action not recognized");
        }
    }

    private void executeAddOperation(
            RepositoryConnection connection,
            @Nullable String baseURI,
            @Nullable String contentType,
            @Nullable InputStream requestBody
    ) {
        checkContentTypeAndBody(contentType, requestBody);

        RDFFormat rdfFormat = getRDFFormatByContentType(contentType);
        try {
            connection.add(requestBody, baseURI, rdfFormat);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void executeDeleteOperation(
            RepositoryConnection connection,
            @Nullable String contentType,
            @Nullable InputStream requestBody
    ) {
        checkContentTypeAndBody(contentType, requestBody);

        RDFFormat rdfFormat = getRDFFormatByContentType(contentType);
        RDFParser rdfParser = initParser(rdfFormat);
        SesameTolerantRDFRemover rdfRemover = new SesameTolerantRDFRemover(connection);
        rdfParser.setRDFHandler(rdfRemover);

        try {
            rdfParser.parse(requestBody);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void executeGetOperation(
            HttpServletResponse response,
            List<String> acceptHeaders,
            RepositoryConnection connection,
            @Nullable String subject,
            @Nullable String predicate,
            @Nullable String object,
            boolean infer
    ) {
        RDFFormat rdfFormat = getRDFFormatOrDefaultIfAcceptable(acceptHeaders, TURTLE);
        response.setContentType(rdfFormat.getDefaultMIMEType());

        Resource subjectIRI = isBlank(subject) ? null : parseNTripleResource(subject);
        IRI predicateIRI = isBlank(predicate) ? null : parseNTripleURI(predicate);
        Value objectValue = isBlank(object) ? null : parseNTripleValue(object);

        RepositoryResult<Statement> result = connection.getStatements(subjectIRI, predicateIRI, objectValue, infer);
        try (OutputStream os = response.getOutputStream()) {
            RDFWriter writer = createWriter(rdfFormat, os);
            Rio.write(result, writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void executeQueryOperation(
            HttpServletResponse response,
            List<String> acceptHeaders,
            RepositoryConnection connection,
            String queryStr,
            String queryLanguage,
            boolean infer
    ) {
        if (isBlank(queryStr)) {
            throw new CommonControllerException(BAD_REQUEST, "Query string is blank");
        }

        Query query = prepareQuery(connection, queryLanguage, queryStr);
        query.setIncludeInferred(infer);

        try {
            if (query instanceof BooleanQuery booleanQuery) {
                boolean evaluationResult = booleanQuery.evaluate();
                prepareBooleanResponse(response, acceptHeaders, evaluationResult);
            } else if (query instanceof GraphQuery graphQuery) {
                try (GraphQueryResult queryResult = graphQuery.evaluate()) {
                    RDFHandler rdfHandler = getRdfHandlerAndSetContentType(response, acceptHeaders);
                    reportGraphResult(rdfHandler, queryResult, false, null, null);
                }
            } else if (query instanceof TupleQuery tupleQuery) {
                try (TupleQueryResult queryResult = tupleQuery.evaluate()) {
                    QueryResultWriter resultWriter = getResultWriterAndSetContentType(response, acceptHeaders);
                    reportTupleResult(resultWriter, queryResult, false, null, null);
                }
            } else {
                throw new CommonControllerException(INTERNAL_ERROR, "Query type non supported");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void executeUpdateOperation(
            RepositoryConnection connection,
            String updateStr,
            String baseURI
    ) {
        if (isBlank(updateStr)) {
            throw new CommonControllerException(BAD_REQUEST, "Update string is blank");
        }

        Update update = connection.prepareUpdate(SPARQL, updateStr, baseURI);
        update.execute();
    }

    private void executeSizeOperation(
            HttpServletResponse response,
            RepositoryConnection connection
    ) {
        long size = connection.size();
        String sizeAsString = String.valueOf(size);

        try {
            ServletOutputStream os = response.getOutputStream();
            os.write(sizeAsString.getBytes());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        response.setContentType(TEXT_PLAIN);
    }

    private void executeCommitOperation(
            RepositoryConnection connection,
            String transactionID
    ) {
        if (!connection.isActive()) {
            throw new CommonControllerException(CONFLICT, "Transaction is not active");
        }
        connection.commit();
        activeConnectionRegistry.unregisterConnection(transactionID);
    }

    private static void checkContentTypeAndBody(
            @Nullable String contentType,
            @Nullable InputStream requestBody
    ) {
        if (isBlank(contentType)) {
            throw new CommonControllerException(BAD_REQUEST, "Content type header is empty");
        }
        if (requestBody == null) {
            throw new CommonControllerException(BAD_REQUEST, "Request body is missing");
        }
    }
}
