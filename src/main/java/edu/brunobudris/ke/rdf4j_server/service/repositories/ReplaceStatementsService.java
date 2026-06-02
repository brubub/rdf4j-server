package edu.brunobudris.ke.rdf4j_server.service.repositories;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.getRDFFormatByContentType;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleContext;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplaceStatementsService {

    private final RepositoryManager repositoryManager;

    public void replaceStatements(
            String contentType,
            String repositoryID,
            @Nullable List<String> contexts,
            String baseUri,
            InputStream requestBody
    ) {
        RDFFormat rdfFormat = getRDFFormatByContentType(contentType);

        Resource[] contextIRIs = parseNTripleContext(contexts);

        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.add(requestBody, baseUri, rdfFormat, contextIRIs);
        } catch (IOException ex) {
            log.error("Error during statements replacement", ex);
            throw new CommonControllerException(INTERNAL_ERROR, ex.getMessage());
        }
    }
}
