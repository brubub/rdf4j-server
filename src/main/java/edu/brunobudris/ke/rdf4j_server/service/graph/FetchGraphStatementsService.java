package edu.brunobudris.ke.rdf4j_server.service.graph;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
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

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.BAD_REQUEST;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.getRDFFormatOrDefaultIfAcceptable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.eclipse.rdf4j.rio.Rio.createWriter;

@Service
@RequiredArgsConstructor
public class FetchGraphStatementsService {

    private final RepositoryManager repositoryManager;

    public void getAllStatements(
            HttpServletResponse servletResponse,
            List<String> acceptHeader,
            String repositoryID,
            @Nullable String namedGraph,
            boolean defaultGraph
    ) {
        RDFFormat rdfFormat = getRDFFormatOrDefaultIfAcceptable(acceptHeader, TURTLE);
        servletResponse.setContentType(rdfFormat.getDefaultMIMEType());

        if (isNotBlank(namedGraph) && defaultGraph) {
            throw new CommonControllerException(BAD_REQUEST, "Both named graph and default graph specified in the request");
        } else if (isBlank(namedGraph) && !defaultGraph) {
            throw new CommonControllerException(BAD_REQUEST, "Neither named graph nor default graph specified in the request");
        }

        IRI context = defaultGraph ? null : iri(namedGraph);

        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            RepositoryResult<Statement> statements = connection.getStatements(null, null, null, context);
            try (OutputStream os = servletResponse.getOutputStream()) {
                RDFWriter writer = createWriter(rdfFormat, os);
                Rio.write(statements, writer);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
