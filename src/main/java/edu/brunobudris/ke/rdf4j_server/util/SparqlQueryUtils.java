package edu.brunobudris.ke.rdf4j_server.util;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.explanation.Explanation;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.BAD_REQUEST;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_ACCEPTABLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_LD_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_BOOLEAN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_TURTLE;
import static edu.brunobudris.ke.rdf4j_server.util.RdfInOutUtils.getAcceptAndQueryResultWriterOrDefaultIfAcceptable;
import static edu.brunobudris.ke.rdf4j_server.util.RdfInOutUtils.getAcceptAndRdfHandlerOrDefaultIfAcceptable;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.containsAcceptableType;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.containsAllTypeOrThrow;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.eclipse.rdf4j.query.QueryResults.distinctResults;
import static org.eclipse.rdf4j.query.QueryResults.limitResults;
import static org.eclipse.rdf4j.query.QueryResults.report;
import static org.springframework.http.MediaType.ALL_VALUE;

@UtilityClass
public class SparqlQueryUtils {

    public static Query prepareQuery(
            RepositoryConnection connection,
            String query
    ) {
        return prepareQuery(connection, null, query);
    }

    public static Query prepareQuery(
            RepositoryConnection connection,
            @Nullable String queryLn,
            String query
    ) {
        if (isBlank(queryLn)) {
            return connection.prepareQuery(query);
        }

        QueryLanguage queryLanguage = QueryLanguage.valueOf(queryLn);
        if (queryLanguage == null) {
            throw new CommonControllerException(BAD_REQUEST, "Query language not supported");
        }

        return connection.prepareQuery(queryLanguage, query);
    }

    public static RDFHandler getRdfHandlerAndSetContentType(
            HttpServletResponse servletResponse,
            List<String> acceptHeaders
    ) throws IOException {
        ServletOutputStream outputStream = servletResponse.getOutputStream();
        Pair<String, RDFHandler> acceptAndHandler = getAcceptAndRdfHandlerOrDefaultIfAcceptable(acceptHeaders, outputStream, TEXT_TURTLE);
        servletResponse.setContentType(acceptAndHandler.getLeft());
        return acceptAndHandler.getRight();
    }

    public static QueryResultWriter getResultWriterAndSetContentType(
            HttpServletResponse servletResponse,
            List<String> acceptHeaders
    ) throws IOException {
        ServletOutputStream outputStream = servletResponse.getOutputStream();
        Pair<String, QueryResultWriter> acceptAndWriter = getAcceptAndQueryResultWriterOrDefaultIfAcceptable(acceptHeaders, outputStream, APPLICATION_SPARQL_RESULTS_JSON);
        servletResponse.setContentType(acceptAndWriter.getLeft());
        return acceptAndWriter.getRight();
    }

    public static void prepareExplanationResponse(
            HttpServletResponse servletResponse,
            List<String> acceptHeaders,
            Explanation explanation
    ) {
        Optional<String> acceptableTypeOpt = containsAcceptableType(acceptHeaders, List.of(APPLICATION_LD_JSON, ALL_VALUE));
        if (acceptableTypeOpt.isEmpty()) {
            throw new CommonControllerException(NOT_ACCEPTABLE, "MediaType not supported");
        }

        try {
            String explanationJson = explanation.toJson();
            byte[] jsonBytes = explanationJson.getBytes();

            servletResponse.setContentType(APPLICATION_LD_JSON);
            servletResponse.setContentLength(jsonBytes.length);

            OutputStream outputStream = servletResponse.getOutputStream();
            outputStream.write(jsonBytes);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void prepareBooleanResponse(
            HttpServletResponse servletResponse,
            List<String> acceptHeaders,
            boolean evaluationResult
    ) {
        Optional<String> acceptOpt = containsAcceptableType(acceptHeaders, List.of(TEXT_BOOLEAN, TEXT_PLAIN));
        if (acceptOpt.isEmpty()) {
            containsAllTypeOrThrow(acceptHeaders, () -> new CommonControllerException(NOT_ACCEPTABLE, "MediaType not supported"));
            acceptOpt = Optional.of(TEXT_PLAIN);
        }

        String accept = acceptOpt.get();
        try {
            String resultStr = String.valueOf(evaluationResult);
            byte[] resultBytes = resultStr.getBytes();

            servletResponse.setContentType(accept);
            servletResponse.setContentLength(resultBytes.length);

            OutputStream outputStream = servletResponse.getOutputStream();
            outputStream.write(resultBytes);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void reportGraphResult(
            RDFHandler rdfHandler,
            GraphQueryResult queryResult,
            boolean distinct,
            @Nullable Integer limit,
            @Nullable Integer offset
    ) {
        GraphQueryResult resultToSerialize = queryResult;
        if (distinct) {
            resultToSerialize = distinctResults(queryResult);
        }
        if (offset != null || limit != null) {
            resultToSerialize = limitResults(
                    resultToSerialize, limit == null ? MAX_VALUE : limit, offset == null ? 0 : offset);
        }

        report(resultToSerialize, rdfHandler);
    }

    public static void reportTupleResult(
            QueryResultWriter resultWriter,
            TupleQueryResult queryResult,
            boolean distinct,
            @Nullable Integer limit,
            @Nullable Integer offset
    ) {
        TupleQueryResult resultToSerialize = queryResult;
        if (distinct) {
            resultToSerialize = distinctResults(queryResult);
        }
        if (offset != null || limit != null) {
            resultToSerialize = limitResults(
                    resultToSerialize, limit == null ? MAX_VALUE : limit, offset == null ? 0 : offset);
        }

        report(resultToSerialize, resultWriter);
    }
}
