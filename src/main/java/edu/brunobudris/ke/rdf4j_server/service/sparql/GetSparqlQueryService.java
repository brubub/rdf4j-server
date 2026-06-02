package edu.brunobudris.ke.rdf4j_server.service.sparql;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.explanation.Explanation;
import org.eclipse.rdf4j.query.explanation.Explanation.Level;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleValue;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.getRdfHandlerAndSetContentType;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.getResultWriterAndSetContentType;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.prepareExplanationResponse;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.prepareQuery;
import static edu.brunobudris.ke.rdf4j_server.util.SparqlQueryUtils.reportTupleResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSparqlQueryService {

    private final RepositoryManager repositoryManager;

    public void executeQuery(
            HttpServletResponse servletResponse,
            List<String> acceptHeaders,
            String repositoryID,
            String queryStr,
            @Nullable String queryLn,
            boolean inferred,
            @Nullable Integer timeout,
            boolean distinct,
            @Nullable Integer limit,
            @Nullable Integer offset,
            @Nullable Level level,
            Map<String,String> variables
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            Query query = prepareQuery(connection, queryLn, queryStr);
            setCommonParams(query, inferred, timeout, variables);

            if (level != null) {
                Explanation explanation = query.explain(level);
                prepareExplanationResponse(servletResponse, acceptHeaders, explanation);
            } else if (query instanceof BooleanQuery booleanQuery) {
                boolean evaluationResult = booleanQuery.evaluate();
                SparqlQueryUtils.prepareBooleanResponse(servletResponse, acceptHeaders, evaluationResult);
            } else if (query instanceof GraphQuery graphQuery) {
                try (GraphQueryResult queryResult = graphQuery.evaluate()) {
                    RDFHandler rdfHandler = getRdfHandlerAndSetContentType(servletResponse, acceptHeaders);
                    SparqlQueryUtils.reportGraphResult(rdfHandler, queryResult, distinct, limit, offset);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (query instanceof TupleQuery tupleQuery) {
                try (TupleQueryResult queryResult = tupleQuery.evaluate()) {
                    QueryResultWriter resultWriter = getResultWriterAndSetContentType(servletResponse, acceptHeaders);
                    reportTupleResult(resultWriter, queryResult, distinct, limit, offset);
                }
            } else {
                throw new CommonControllerException(INTERNAL_ERROR, "Query type non supported");
            }
        } catch (IOException ex) {
            log.error("Unexpected exception", ex);
            throw new RuntimeException(ex);
        }
    }

    private static void setCommonParams(
            Query query,
            boolean inferred,
            Integer timeout,
            Map<String,String> variables
    ) {
        query.setIncludeInferred(inferred);
        if (timeout != null) {
            query.setMaxExecutionTime(timeout);
        }
        for (Map.Entry<String,String> entry : variables.entrySet()) {
            Value value = parseNTripleValue(entry.getValue());
            query.setBinding(entry.getKey(), value);
        }
    }
}
