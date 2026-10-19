package de.unibayreuth.bayceer.bayeos.api.client;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal embedded HTTP server used by the unit tests. It records incoming
 * requests and answers with queued responses (defaulting to {@code 200} with an
 * empty body).
 */
final class StubServer implements AutoCloseable {

    static final class Recorded {
        final String method;
        final String path;
        final Map<String, String> query;
        final String authorization;
        final String nodeToken;
        final String contentType;
        final String body;

        Recorded(String method, String path, Map<String, String> query, String authorization,
                 String nodeToken, String contentType, String body) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.authorization = authorization;
            this.nodeToken = nodeToken;
            this.contentType = contentType;
            this.body = body;
        }

        String queryParam(String name) {
            return query.get(name);
        }
    }

    private static final class StubResponse {
        final int status;
        final String body;

        StubResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

    private final HttpServer server;
    private final List<Recorded> requests = Collections.synchronizedList(new ArrayList<>());
    private final Deque<StubResponse> responses = new ArrayDeque<>();
    private final String baseUrl;

    StubServer() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.add(new Recorded(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    parseQuery(exchange.getRequestURI().getRawQuery()),
                    exchange.getRequestHeaders().getFirst("Authorization"),
                    exchange.getRequestHeaders().getFirst("Node-Token"),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    body));
            StubResponse response = responses.poll();
            if (response == null) {
                response = new StubResponse(200, "");
            }
            byte[] out = response.body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            try {
                exchange.sendResponseHeaders(response.status, out.length == 0 ? -1 : out.length);
                if (out.length > 0) {
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(out);
                    }
                }
            } finally {
                exchange.close();
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    String baseUrl() {
        return baseUrl;
    }

    void enqueue(int status, String body) {
        responses.add(new StubResponse(status, body));
    }

    List<Recorded> requests() {
        return new ArrayList<>(requests);
    }

    Recorded request(int index) {
        return requests().get(index);
    }

    Recorded lastRequest() {
        List<Recorded> all = requests();
        return all.get(all.size() - 1);
    }

    int requestCount() {
        return requests.size();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> result = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return result;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                result.put(decode(pair), "");
            } else {
                result.put(decode(pair.substring(0, eq)), decode(pair.substring(eq + 1)));
            }
        }
        return result;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
