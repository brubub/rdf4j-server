package edu.brunobudris.ke.rdf4j_server.service.repositories;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleContext;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleResource;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleURI;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleValue;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class RemoveStatementsService {

    private final RepositoryManager repositoryManager;

    public void removeStatements(
            String repositoryID,
            @Nullable String subject,
            @Nullable String predicate,
            @Nullable String object,
            @Nullable List<String> contexts
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        Resource subjectIRI = isBlank(subject) ? null : parseNTripleResource(subject);
        IRI predicateIRI = isBlank(predicate) ? null : parseNTripleURI(predicate);
        Value objectValue = isBlank(object) ? null : parseNTripleValue(object);
        Resource[] contextIRIs = parseNTripleContext(contexts);

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.remove(subjectIRI, predicateIRI, objectValue, contextIRIs);
        }
    }
}
