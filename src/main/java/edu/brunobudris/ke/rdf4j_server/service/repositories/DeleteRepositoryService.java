package edu.brunobudris.ke.rdf4j_server.service.repositories;

import edu.brunobudris.ke.rdf4j_server.exception.CommonControllerException;
import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.stereotype.Service;

import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.CONFLICT;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.INTERNAL_ERROR;
import static edu.brunobudris.ke.rdf4j_server.exception.CommonControllerExceptionType.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeleteRepositoryService {

    private final RepositoryManager repositoryManager;

    public void deleteRepository(String repositoryID) {
        boolean hasConfig = repositoryManager.hasRepositoryConfig(repositoryID);
        if (!hasConfig) {
            throw new CommonControllerException(NOT_FOUND, "Repository not found");
        }

        boolean isSafeToRemove = repositoryManager.isSafeToRemove(repositoryID);
        if (!isSafeToRemove) {
            throw new CommonControllerException(CONFLICT, "The repository is not safe to remove");
        }

        boolean isRemoved = repositoryManager.removeRepository(repositoryID);
        if (!isRemoved) {
            throw new CommonControllerException(INTERNAL_ERROR, "The server was unable to remove the repository");
        }
    }

}
