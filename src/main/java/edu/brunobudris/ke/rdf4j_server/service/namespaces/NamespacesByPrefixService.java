package edu.brunobudris.ke.rdf4j_server.service.namespaces;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class NamespacesByPrefixService {

    private final RepositoryManager repositoryManager;

    public String getNamespaceByPrefix(
            String repositoryID,
            String namespacesPrefix
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            return connection.getNamespace(namespacesPrefix);
        }
    }

    public void setNamespaceForPrefix(
            String repositoryID,
            String prefix,
            String namespace
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.setNamespace(prefix, namespace);
        }
    }

    public void removeNamespaceForPrefix(
            String repositoryID,
            @Nullable String prefix
    ) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        try (RepositoryConnection connection = repository.getConnection()) {
            connection.removeNamespace(prefix == null ? "" : prefix);
        }
    }
}
