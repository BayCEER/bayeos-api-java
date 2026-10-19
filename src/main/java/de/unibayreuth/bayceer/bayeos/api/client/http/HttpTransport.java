package de.unibayreuth.bayceer.bayeos.api.client.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unibayreuth.bayceer.bayeos.api.client.auth.AuthSession;
import de.unibayreuth.bayceer.bayeos.api.client.exception.ApiException;
import de.unibayreuth.bayceer.bayeos.api.client.exception.AuthenticationException;
import de.unibayreuth.bayceer.bayeos.api.client.exception.TransportException;
import de.unibayreuth.bayceer.bayeos.api.client.model.ErrorResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around {@link HttpClient} that serialises request bodies,
 * attaches the auth headers, decodes responses and maps API errors to
 * exceptions.
 */
public final class HttpTransport {

    private final String baseUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final Duration requestTimeout;
    private volatile AuthSession auth;

    public HttpTransport(String baseUrl, HttpClient http, Duration requestTimeout) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.http = http;
        this.mapper = Json.mapper();
        this.requestTimeout = requestTimeout;
    }

    public void setAuth(AuthSession auth) {
        this.auth = auth;
    }

    public <T> T execute(Request req, Class<T> type) {
        String body = send(req);
        return parse(body, type);
    }

    public <T> T execute(Request req, TypeReference<T> type) {
        String body = send(req);
        return parse(body, type);
    }

    public <T> List<T> executeList(Request req, Class<T> elementType) {
        String body = send(req);
        if (body == null || body.isEmpty()) {
            return List.of();
        }
        try {
            return mapper.readValue(body,
                    mapper.getTypeFactory().constructCollectionType(List.class, elementType));
        } catch (IOException e) {
            throw new TransportException("Failed to parse response from " + req.path, e);
        }
    }

    public void execute(Request req) {
        send(req);
    }

    private String send(Request req) {
        AuthSession session = this.auth;
        if (req.auth && session != null) {
            session.ensureToken();
        }
        try {
            return sendOnce(req, session);
        } catch (AuthenticationException e) {
            if (req.auth && !req.noAuthRetry && session != null && session.canRefresh()) {
                session.refresh();
                return sendOnce(req, session);
            }
            throw e;
        }
    }

    private String sendOnce(Request req, AuthSession session) {
        URI uri = buildUri(req);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)
                .header("Accept", "application/json");
        if (req.body != null) {
            builder.header("Content-Type", "application/json");
        }
        if (req.auth && session != null) {
            session.applyHeaders(builder);
        }

        HttpRequest.BodyPublisher publisher = req.body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(serialize(req.body), StandardCharsets.UTF_8);
        builder.method(req.method, publisher);

        HttpResponse<String> response;
        try {
            response = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new TransportException("HTTP " + req.method + " " + uri + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransportException("HTTP " + req.method + " " + uri + " interrupted", e);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return response.body();
        }
        throw mapError(status, response.body());
    }

    private ApiException mapError(int status, String body) {
        ErrorResponse parsed = null;
        if (body != null && !body.isEmpty()) {
            try {
                parsed = mapper.readValue(body, ErrorResponse.class);
            } catch (IOException ignored) {
                // body was not the expected ErrorResponse JSON
            }
        }
        if (status == 401) {
            return new AuthenticationException(status, parsed, body);
        }
        return new ApiException(status, parsed, body);
    }

    private <T> T parse(String body, Class<T> type) {
        if (body == null || body.isEmpty() || type == null || type == Void.class) {
            return null;
        }
        try {
            return mapper.readValue(body, type);
        } catch (IOException e) {
            throw new TransportException("Failed to parse response as " + type.getSimpleName(), e);
        }
    }

    private <T> T parse(String body, TypeReference<T> type) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            return mapper.readValue(body, type);
        } catch (IOException e) {
            throw new TransportException("Failed to parse response", e);
        }
    }

    private String serialize(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new TransportException("Failed to serialise request body", e);
        }
    }

    private URI buildUri(Request req) {
        StringBuilder sb = new StringBuilder(baseUrl).append(req.path);
        if (!req.query.isEmpty()) {
            char separator = '?';
            for (Map.Entry<String, String> entry : req.query.entrySet()) {
                sb.append(separator);
                separator = '&';
                sb.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
            }
        }
        return URI.create(sb.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String stripTrailingSlash(String url) {
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
