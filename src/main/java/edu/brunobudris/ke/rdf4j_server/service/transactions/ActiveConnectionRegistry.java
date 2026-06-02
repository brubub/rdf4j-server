package edu.brunobudris.ke.rdf4j_server.service.transactions;

import jakarta.annotation.Nullable;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ActiveConnectionRegistry {

    private final Map<String, RepositoryConnection> activeTransactions = new HashMap<>();

    public String registerConnection(RepositoryConnection connection) {
        UUID transactionId = UUID.randomUUID();
        activeTransactions.put(transactionId.toString(), connection);
        return transactionId.toString();
    }

    public Optional<RepositoryConnection> findConnection(String connectionId) {
        return Optional.ofNullable(activeTransactions.get(connectionId));
    }

    @Nullable
    public RepositoryConnection unregisterConnection(String connectionId) {
        return activeTransactions.remove(connectionId);
    }
}
