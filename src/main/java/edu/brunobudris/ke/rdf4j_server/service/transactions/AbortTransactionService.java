package edu.brunobudris.ke.rdf4j_server.service.transactions;

import lombok.RequiredArgsConstructor;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbortTransactionService {

    private final ActiveConnectionRegistry activeConnectionRegistry;

    public void abortTransaction(String transactionID) {
        RepositoryConnection connection = activeConnectionRegistry.unregisterConnection(transactionID);
        if (connection == null) {
            return;
        }
        connection.rollback();
        connection.close();
    }
}
