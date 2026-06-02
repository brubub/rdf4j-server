package edu.brunobudris.ke.rdf4j_server.service.repositories;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.getRDFFormatOrDefaultIfAcceptable;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleContext;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleResource;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleValue;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleURI;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.eclipse.rdf4j.rio.Rio.createWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class FetchStatementsService {

    private final RepositoryManager repositoryManager;

    public void fetchStatements(
            HttpServletResponse servletResponse,
            List<String> acceptHeader,
            String repositoryID,
            @Nullable String subject,
            @Nullable String predicate,
            @Nullable String object,
            @Nullable List<String> context,
            @Nullable String inferred
    ) {
        RDFFormat rdfFormat = getRDFFormatOrDefaultIfAcceptable(acceptHeader, TURTLE);
        servletResponse.setContentType(rdfFormat.getDefaultMIMEType());

        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        Resource subjectIRI = isBlank(subject) ? null : parseNTripleResource(subject);
        IRI predicateIRI = isBlank(predicate) ? null : parseNTripleURI(predicate);
        Value objectValue = isBlank(object) ? null : parseNTripleValue(object);
        boolean inferredBoolean = isBlank(inferred) || inferred.equalsIgnoreCase("true");
        Resource[] contextIRIs = parseNTripleContext(context);

        try (RepositoryConnection connection = repository.getConnection()) {
            RepositoryResult<Statement> result = connection.getStatements(subjectIRI, predicateIRI, objectValue, inferredBoolean, contextIRIs);
            try (OutputStream os = servletResponse.getOutputStream()) {
                RDFWriter writer = createWriter(rdfFormat, os);
                Rio.write(result, writer);
            } catch (IOException ex) {
                log.error("IO error writing result", ex);
                throw new CommonControllerException(INTERNAL_ERROR, "Statements fetch error");
            }
        }
    }
}
