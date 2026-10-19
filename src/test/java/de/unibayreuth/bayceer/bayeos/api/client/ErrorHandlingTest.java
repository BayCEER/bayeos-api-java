package de.unibayreuth.bayceer.bayeos.api.client;

import de.unibayreuth.bayceer.bayeos.api.client.exception.ApiException;
import de.unibayreuth.bayceer.bayeos.api.client.exception.AuthenticationException;
import de.unibayreuth.bayceer.bayeos.api.client.exception.BayeosException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorHandlingTest {

    @Test
    void apiErrorBodyIsMapped() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(400, "{\"error\":\"InvalidRequest\",\"message\":\"The request parameters are invalid.\","
                    + "\"details\":{\"parameter\":\"ids\"},\"timestamp\":\"2026-01-01T00:00:00Z\","
                    + "\"traceId\":\"abc123\"}");

            try (BayeosClient client = simpleClient(stub)) {
                ApiException error = assertThrows(ApiException.class,
                        () -> client.nodes().get(999));

                assertEquals(400, error.getStatusCode());
                assertEquals("InvalidRequest", error.getError());
                assertEquals("The request parameters are invalid.", error.getServerMessage());
                assertEquals("abc123", error.getTraceId());
                assertEquals("ids", error.getDetails().get("parameter"));
            }
        }
    }

    @Test
    void unauthorizedIsMappedToAuthenticationException() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(401, "{\"error\":\"Unauthorized\",\"message\":\"invalid token\","
                    + "\"timestamp\":\"2026-01-01T00:00:00Z\"}");

            try (BayeosClient client = simpleClient(stub)) {
                assertThrows(AuthenticationException.class, () -> client.nodes().get(6));
            }
        }
    }

    @Test
    void nonJsonErrorBodyStillProducesException() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(500, "boom");

            try (BayeosClient client = simpleClient(stub)) {
                BayeosException error = assertThrows(BayeosException.class,
                        () -> client.nodes().get(6));
                assertEquals("HTTP 500: boom", error.getMessage());
            }
        }
    }

    private static BayeosClient simpleClient(StubServer stub) {
        return BayeosClient.builder().baseUrl(stub.baseUrl()).token("t").build();
    }
}
