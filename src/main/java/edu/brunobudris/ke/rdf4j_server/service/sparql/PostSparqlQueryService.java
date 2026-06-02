package edu.brunobudris.ke.rdf4j_server.service.sparql;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.BAD_REQUEST;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.getRdfHandlerAndSetContentType;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.getResultWriterAndSetContentType;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.prepareQuery;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.reportTupleResult;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostSparqlQueryService {

    private final RepositoryManager repositoryManager;

    public void executeQuery(
            HttpServletResponse servletResponse,
            List<String> acceptHeaders,
            String repositoryID,
            String queryStr
    ) {
        if (isBlank(queryStr)) {
            throw new CommonControllerException(BAD_REQUEST, "The SPARQL query to execute is null");
        }

        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            Query query = prepareQuery(connection, queryStr);

            if (query instanceof BooleanQuery booleanQuery) {
                boolean evaluationResult = booleanQuery.evaluate();
                SparqlQueryUtils.prepareBooleanResponse(servletResponse, acceptHeaders, evaluationResult);
            } else if (query instanceof GraphQuery graphQuery) {
                try (GraphQueryResult queryResult = graphQuery.evaluate()) {
                    RDFHandler rdfHandler = getRdfHandlerAndSetContentType(servletResponse, acceptHeaders);
                    SparqlQueryUtils.reportGraphResult(rdfHandler, queryResult, false, null, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (query instanceof TupleQuery tupleQuery) {
                try (TupleQueryResult queryResult = tupleQuery.evaluate()) {
                    QueryResultWriter resultWriter = getResultWriterAndSetContentType(servletResponse, acceptHeaders);
                    reportTupleResult(resultWriter, queryResult, false, null, null);
                }
            } else {
                throw new CommonControllerException(INTERNAL_ERROR, "Query type non supported");
            }
        } catch (IOException ex) {
            log.error("Unexpected exception", ex);
            throw new RuntimeException(ex);
        }
    }
}
