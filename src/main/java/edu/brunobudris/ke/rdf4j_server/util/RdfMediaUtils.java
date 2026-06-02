package edu.brunobudris.ke.rdf4j_server.util;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_ACCEPTABLE;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.UNSUPPORTED_MEDIA_TYPE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_LD_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_JSON;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_RDF_XML;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_TRIX;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_BINARY_RDF;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_TRIG;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_PLAIN;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_RDF_N3;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_TURTLE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.TEXT_X_NQUADS;
import static org.eclipse.rdf4j.rio.RDFFormat.BINARY;
import static org.eclipse.rdf4j.rio.RDFFormat.JSONLD;
import static org.eclipse.rdf4j.rio.RDFFormat.N3;
import static org.eclipse.rdf4j.rio.RDFFormat.NQUADS;
import static org.eclipse.rdf4j.rio.RDFFormat.NTRIPLES;
import static org.eclipse.rdf4j.rio.RDFFormat.RDFJSON;
import static org.eclipse.rdf4j.rio.RDFFormat.RDFXML;
import static org.eclipse.rdf4j.rio.RDFFormat.TRIG;
import static org.eclipse.rdf4j.rio.RDFFormat.TRIX;
import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.eclipse.rdf4j.rio.helpers.NTriplesUtil.parseResource;
import static org.eclipse.rdf4j.rio.helpers.NTriplesUtil.parseURI;
import static org.eclipse.rdf4j.rio.helpers.NTriplesUtil.parseValue;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.util.CollectionUtils.isEmpty;

@UtilityClass
public class RdfMediaUtils {

    private static final SimpleValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

    public static <X extends Throwable> void containsAllTypeOrThrow(
            List<String> acceptHeaders,
            Supplier<? extends X> exceptionSupplier
    ) throws X {
        if (!acceptHeaders.contains(ALL_VALUE)) {
            throw exceptionSupplier.get();
        }
    }

    public static Optional<String> containsAcceptableType(
            List<String> acceptHeaders,
            List<String> acceptable
    ) {
        if (isEmpty(acceptHeaders)) {
            return Optional.empty();
        }

        for (String acceptableType : acceptable) {
            boolean contains = acceptHeaders.contains(acceptableType);
            if (contains) {
                return Optional.of(acceptableType);
            }
        }

        return Optional.empty();
    }

    public static Resource parseNTripleResource(String value) {
        return parseResource(value, VALUE_FACTORY);
    }

    public static IRI parseNTripleURI(String value) {
        return parseURI(value, VALUE_FACTORY);
    }

    public static Value parseNTripleValue(String value) {
        return parseValue(value, VALUE_FACTORY);
    }

    public static Resource[] parseNTripleContext(List<String> context) {
        if (context == null || context.isEmpty()) {
            return new IRI[]{};
        }

        return context.stream()
                .filter(Objects::nonNull)
                .map(v -> parseResource(v, VALUE_FACTORY))
                .toArray(Resource[]::new);
    }

    public static RDFFormat getRDFFormatByContentType(String contentType) {
        return findRDFFormat(contentType)
                .orElseThrow(() -> new CommonControllerException(UNSUPPORTED_MEDIA_TYPE, "MediaType not supported"));
    }

    public static RDFFormat getRDFFormatOrDefaultIfAcceptable(
            List<String> accept,
            RDFFormat fallback
    ) {
        return findRDFFormat(accept)
                .orElseGet(() -> {
                    if (accept.contains(ALL_VALUE)) {
                        return fallback;
                    } else {
                        throw new CommonControllerException(NOT_ACCEPTABLE, "MediaType not supported");
                    }
                });
    }

    public static Optional<RDFFormat> findRDFFormat(String contentType) {
        return Optional.ofNullable(mapContentTypeToRDFFormat(contentType));
    }

    public static Optional<RDFFormat> findRDFFormat(List<String> accept) {
        return accept.stream()
                .filter(StringUtils::isNotBlank)
                .filter(c -> !ALL_VALUE.equals(c))
                .map(RdfMediaUtils::mapContentTypeToRDFFormat)
                .filter(Objects::nonNull)
                .findFirst();
    }

    @Nullable
    public static RDFFormat mapContentTypeToRDFFormat(String contentType) {
        return switch (contentType) {
            case APPLICATION_RDF_XML: yield RDFXML;
            case TEXT_PLAIN: yield NTRIPLES;
            case TEXT_TURTLE: yield TURTLE;
            case TEXT_RDF_N3: yield N3;
            case TEXT_X_NQUADS: yield NQUADS;
            case APPLICATION_LD_JSON: yield JSONLD;
            case APPLICATION_RDF_JSON: yield RDFJSON;
            case APPLICATION_TRIX: yield TRIX;
            case APPLICATION_X_TRIG: yield TRIG;
            case APPLICATION_X_BINARY_RDF: yield BINARY;
            default: yield null;
        };
    }
}