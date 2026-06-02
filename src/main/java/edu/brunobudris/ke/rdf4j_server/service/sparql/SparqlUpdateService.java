package edu.brunobudris.ke.rdf4j_server.service.sparql;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.UNSUPPORTED_MEDIA_TYPE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_SPARQL_UPDATE;
import static edu.brunobudris.ke.rdf4j_server.util.ControllerUtils.APPLICATION_X_RDFTRANSACTION;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.findRDFFormat;
import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Service
@RequiredArgsConstructor
public class SparqlUpdateService {

    private final RepositoryManager repositoryManager;

    public void update(
            String contentType,
            String repositoryID,
            @Nullable String baseUri,
            InputStream requestBody
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            if (APPLICATION_SPARQL_UPDATE.equals(contentType) || APPLICATION_FORM_URLENCODED_VALUE.equals(contentType)) {
                executeSparqlUpdate(connection, baseUri, requestBody);
                return;
            } else if (APPLICATION_X_RDFTRANSACTION.equals(contentType)) {
                executeRdfTransaction(connection, baseUri, requestBody);
                return;
            }

            Optional<RDFFormat> rdfFormatOpt = findRDFFormat(contentType);
            if (rdfFormatOpt.isPresent()) {
                upsertRdfStatements(connection, baseUri, requestBody, rdfFormatOpt.get());
                return;
            }
            throw new CommonControllerException(UNSUPPORTED_MEDIA_TYPE, "Request data not understood");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void executeSparqlUpdate(
            RepositoryConnection connection,
            @Nullable String baseUri,
            InputStream requestBody
    ) throws IOException {
        String updateStr = new String(requestBody.readAllBytes());
        Update update = connection.prepareUpdate(SPARQL, updateStr, baseUri);
        update.execute();
    }

    private void upsertRdfStatements(
            RepositoryConnection connection,
            @Nullable String baseUri,
            InputStream requestBody,
            RDFFormat rdfFormat
    ) throws IOException {
        connection.add(requestBody, baseUri, rdfFormat);
    }

    private void executeRdfTransaction(
            RepositoryConnection connection,
            @Nullable String baseUri,
            InputStream requestBody
    ) {
        // TODO
        throw new CommonControllerException(INTERNAL_ERROR, "Transactions not implemented yet");
    }
}
