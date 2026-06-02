package edu.brunobudris.ke.rdf4j_server.util;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.query.resultio.binary.BinaryQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLStarResultsTSVWriter;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.binary.BinaryRDFWriter;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter;
import org.eclipse.rdf4j.rio.n3.N3Writer;
import org.eclipse.rdf4j.rio.nquads.NQuadsWriter;
import org.eclipse.rdf4j.rio.rdfjson.RDFJSONWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.trig.TriGWriter;
import org.eclipse.rdf4j.rio.trix.TriXWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_ACCEPTABLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_LD_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_RESULTS_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_TRIX;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF_RESULTS_TABLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_TRIG;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_RDF_N3;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_TURTLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_X_NQUADS;
import static org.eclipse.rdf4j.rio.RDFFormat.RDFJSON;
import static org.eclipse.rdf4j.rio.Rio.createParser;
import static org.springframework.http.MediaType.ALL_VALUE;

@UtilityClass
public class RdfInOutUtils {

    public static Pair<String, QueryResultWriter> getAcceptAndQueryResultWriterOrDefaultIfAcceptable(
            List<String> acceptHeaders,
            OutputStream outputStream,
            String fallback
    ) {
        return findAcceptAndQueryResultWriter(acceptHeaders, outputStream)
                .or(() -> {
                    if (acceptHeaders.contains(ALL_VALUE)) {
                        return findAcceptAndQueryResultWriter(List.of(fallback), outputStream);
                    }
                    return Optional.empty();
                })
                .orElseThrow(() -> new CommonControllerException(NOT_ACCEPTABLE, "MediaType not implemented"));
    }

    public static Optional<Pair<String, QueryResultWriter>> findAcceptAndQueryResultWriter(
            List<String> acceptHeaders,
            OutputStream outputStream
    ) {
        for (String accept : acceptHeaders) {
            QueryResultWriter writer = initQueryResultWriter(accept, outputStream);
            if (writer != null) {
                return Optional.of(Pair.of(accept, writer));
            }
        }
        return Optional.empty();
    }

    @Nullable
    public static QueryResultWriter initQueryResultWriter(String acceptHeader, OutputStream outputStream) {
        return switch (acceptHeader) {
            case APPLICATION_SPARQL_RESULTS_XML: yield new SPARQLResultsXMLWriter(outputStream);
            case APPLICATION_SPARQL_RESULTS_JSON: yield new SPARQLResultsJSONWriter(outputStream);
            case APPLICATION_X_BINARY_RDF_RESULTS_TABLE: yield new BinaryQueryResultWriter(outputStream);
            case TEXT_PLAIN: yield new SPARQLStarResultsTSVWriter(outputStream);
            default: yield null;
        };
    }

    public static Pair<String, RDFHandler> getAcceptAndRdfHandlerOrDefaultIfAcceptable(
            List<String> acceptHeaders,
            OutputStream outputStream,
            String fallback
    ) {
        return findAcceptAndRdfHandler(acceptHeaders, outputStream)
                .or(() -> {
                    if (acceptHeaders.contains(ALL_VALUE)) {
                        return findAcceptAndRdfHandler(List.of(fallback), outputStream);
                    }
                    return Optional.empty();
                })
                .orElseThrow(() -> new CommonControllerException(NOT_ACCEPTABLE, "MediaType not implemented"));
    }

    public static Optional<Pair<String, RDFHandler>> findAcceptAndRdfHandler(
            List<String> acceptHeaders,
            OutputStream outputStream
    ) {
        for (String accept : acceptHeaders) {
            RDFHandler handler = initRdfHandler(accept, outputStream);
            if (handler != null) {
                return Optional.of(Pair.of(accept, handler));
            }
        }
        return Optional.empty();
    }

    @Nullable
    public static RDFHandler initRdfHandler(String acceptHeader, OutputStream outputStream) {
        return switch (acceptHeader) {
            case APPLICATION_RDF_XML: yield new RDFXMLWriter(outputStream);
            case TEXT_TURTLE: yield new TurtleWriter(outputStream);
            case TEXT_RDF_N3: yield new N3Writer(outputStream);
            case TEXT_X_NQUADS: yield new NQuadsWriter(outputStream);
            case APPLICATION_LD_JSON: yield new JSONLDWriter(outputStream);
            case APPLICATION_RDF_JSON: yield new RDFJSONWriter(outputStream, RDFJSON);
            case APPLICATION_TRIX: yield new TriXWriter(outputStream);
            case APPLICATION_X_TRIG: yield new TriGWriter(outputStream);
            case APPLICATION_X_BINARY_RDF: yield new BinaryRDFWriter(outputStream);
            default: yield null;
        };
    }

    public static RDFParser initParser(RDFFormat rdfFormat) {
        return createParser(rdfFormat);
    }
}
