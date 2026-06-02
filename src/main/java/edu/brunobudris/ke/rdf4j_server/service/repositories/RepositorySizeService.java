package edu.brunobudris.ke.rdf4j_server.service.repositories;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import java.util.List;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;
import static edu.brunobudris.ke.rdf4j_server.util.RdfMediaUtils.parseNTripleContext;
import static java.lang.String.valueOf;

@Service
@RequiredArgsConstructor
public class RepositorySizeService {

    private final RepositoryManager repositoryManager;

    public String getSize(String repositoryID, List<String> contexts) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        Resource[] contextIRIs = parseNTripleContext(contexts);

        try (RepositoryConnection connection = repository.getConnection()) {
            return valueOf(connection.size(contextIRIs));
        }
    }
}