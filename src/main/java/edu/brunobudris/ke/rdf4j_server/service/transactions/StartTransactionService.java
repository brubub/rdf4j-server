package edu.brunobudris.ke.rdf4j_server.service.transactions;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StartTransactionService {

    private final RepositoryManager repositoryManager;
    private final ActiveConnectionRegistry activeConnectionRegistry;

    public String startTransaction(String repositoryID) {
        Repository repository = repositoryManager.getRepository(repositoryID);
        if (repository == null) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        RepositoryConnection connection = repository.getConnection();
        connection.begin();
        return activeConnectionRegistry.registerConnection(connection);
    }
}
