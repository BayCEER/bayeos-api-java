package de.unibayreuth.bayceer.bayeos.api.client;

import de.unibayreuth.bayceer.bayeos.api.client.model.Node;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthFlowTest {

    private static final String ERROR_401 =
            "{\"error\":\"Unauthorized\",\"message\":\"expired\",\"timestamp\":\"2026-01-01T00:00:00Z\"}";

    @Test
    void credentialsSignInLazilyAndAttachBearer() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"token\":\"TOKEN\"}");
            stub.enqueue(200, "[]");

            try (BayeosClient client = BayeosClient.builder()
                    .baseUrl(stub.baseUrl())
                    .credentials("root", "bayeos")
                    .build()) {
                List<Node> nodes = client.nodes().list();
                assertTrue(nodes.isEmpty());
            }

            assertEquals(2, stub.requestCount());
            StubServer.Recorded signIn = stub.request(0);
            assertEquals("POST", signIn.method);
            assertEquals("/auth/signin", signIn.path);
            assertTrue(signIn.body.contains("\"name\":\"root\""));
            assertTrue(signIn.body.contains("\"password\":\"bayeos\""));
            assertNull(signIn.authorization);

            StubServer.Recorded list = stub.request(1);
            assertEquals("/nodes", list.path);
            assertEquals("Bearer TOKEN", list.authorization);
        }
    }

    @Test
    void nodeTokenModeUsesNodeTokenHeader() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "[]");

            try (BayeosClient client = BayeosClient.builder()
                    .baseUrl(stub.baseUrl())
                    .nodeToken("11111111-2222-3333-4444-555555555555")
                    .build()) {
                client.nodes().list();
            }

            assertEquals(1, stub.requestCount());
            StubServer.Recorded list = stub.request(0);
            assertEquals("11111111-2222-3333-4444-555555555555", list.nodeToken);
            assertNull(list.authorization);
        }
    }

    @Test
    void explicitTokenIsUsedDirectly() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "[]");

            try (BayeosClient client = BayeosClient.builder()
                    .baseUrl(stub.baseUrl())
                    .token("PRESET")
                    .build()) {
                client.nodes().list();
            }

            assertEquals(1, stub.requestCount());
            assertEquals("Bearer PRESET", stub.request(0).authorization);
        }
    }

    @Test
    void autoRefreshRetriesOnceAfterUnauthorized() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"token\":\"A\"}");
            stub.enqueue(401, ERROR_401);
            stub.enqueue(200, "{\"token\":\"B\"}");
            stub.enqueue(200, "[]");

            try (BayeosClient client = BayeosClient.builder()
                    .baseUrl(stub.baseUrl())
                    .credentials("root", "bayeos")
                    .autoRefresh(true)
                    .build()) {
                client.nodes().list();
            }

            assertEquals(4, stub.requestCount());
            assertEquals("/auth/signin", stub.request(0).path);
            assertEquals("Bearer A", stub.request(1).authorization);
            assertEquals("PUT", stub.request(2).method);
            assertEquals("/auth/refresh", stub.request(2).path);
            assertEquals("Bearer B", stub.request(3).authorization);
        }
    }
}
