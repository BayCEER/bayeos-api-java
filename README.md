# bayeos-api-java

Fluent Java client for the [BayEOS](https://github.com/BayCEER/bayeos-api) REST API.

- Java 11+, no HTTP dependency beyond the JDK (`java.net.http`), Jackson for JSON.
- Fluent request builders for nodes and timeseries.
- Column-oriented **batched** timeseries writes across several series, with automatic chunking.
- User/JWT and node-token authentication, optional automatic token refresh.

Package root: `de.unibayreuth.bayceer.bayeos.api.client`.

## Quick start

```java
import de.unibayreuth.bayceer.bayeos.api.client.BayeosClient;
import de.unibayreuth.bayceer.bayeos.api.client.model.*;
import java.time.Duration;
import java.time.Instant;

try (BayeosClient client = BayeosClient.builder()
        .baseUrl("http://localhost:5532/bayeos-api")   // includes the API context
        .credentials("<username>", "<password>")                 // lazy sign-in on first request
        .autoRefresh(true)                             // refresh on expiry / 401
        .build()) {

    // Nodes: mkdir -p style path creation (intermediate folders are created)
    Node folder = client.nodes().createPath("~/Climate/Stations").execute();

    // Search by Ant path
    NodeSearchResult hits = client.nodes().search()
            .pattern("~/**").kind(NodeKind.TIMESERIES).limit(20).execute();

    // Create a timeseries under a parent id (or use .path("~/Climate/Air_Temperature"))
    Node airTemp = client.timeseries().create("Air_Temperature")
            .parentId(folder.getId())
            .unit("°C")
            .resolution(Duration.ofMinutes(10))
            .execute();

    // Single-series write (API upserts on (id, ts))
    client.timeseries().write(airTemp.getId())
            .point(Instant.parse("2026-01-01T00:00:00Z"), 12.4)
            .point(Instant.parse("2026-01-01T00:10:00Z"), 12.9, Status.VALID)
            .ignoreInvalidTimestamps(true)
            .send();

    // Batched write across several series (column-oriented, chunked automatically)
    client.timeseries().writeBatch()
            .series(airTemp.getId())
                .point(Instant.parse("2026-01-01T00:20:00Z"), 13.1)
                .point(Instant.parse("2026-01-01T00:30:00Z"), 13.4)
            .series(7)                                  // another series by id
                .points(rows)                           // Collection<DataPoint>
            .chunkSize(5000)
            .ignoreInvalidTimestamps(true)
            .send();

    // Batched read
    BatchData data = client.timeseries().readBatch()
            .ids(airTemp.getId(), 7)
            .from(Instant.parse("2026-01-01T00:00:00Z"))
            .to(Instant.parse("2026-01-02T00:00:00Z"))
            .sampleInterval(SampleInterval.HOURLY)
            .sampleFunc(SampleFunc.AVG)
            .statusFilter(Status.NOT_SET, Status.VALID)
            .execute();

    for (SeriesData series : data.getSeries()) {
        System.out.println(series.getId() + " -> " + series.getValues());
    }
}
```

## Authentication

Three modes, mutually exclusive:

| Mode       | Builder                                     | Header sent                   |
| ---------- | ------------------------------------------- | ----------------------------- |
| User / JWT | `.credentials(user, pass)` or `.token(jwt)` | `Authorization: Bearer <jwt>` |
| Node token | `.nodeToken(uuid)`                          | `Node-Token: <uuid>`          |

- With `.credentials(...)` the sign-in is performed **lazily** on the first secured request.
- `.autoRefresh(true)` refreshes the token before expiry (when the JWT carries an `exp`) and
  retries once after a 401.
- `client.signIn(user, pass)`, `client.refreshToken()` and `client.signOut()` are also available.
- The `~` path prefix and node search resolve against the caller's home node and therefore require
  user/JWT authentication — a node token has no home node.

## API surface (v1)

| Area         | Methods                                                                          |
| ------------ | -------------------------------------------------------------------------------- |
| Version      | `client.version()`                                                               |
| Auth         | `client.signIn()`, `client.refreshToken()`, `client.signOut()`                   |
| Nodes        | `nodes().get/list/children/parents/search/createFolder/createPath/update/delete` |
| Timeseries   | `timeseries().create/get/list/update/delete`                                     |
| Data write   | `write(id)` (single series), `writeBatch()` (multi-series, chunked)              |
| Data read    | `read(id)` (single series), `readBatch()` (multi-series)                         |
| Stats / gaps | `stats(id)`, `gaps(id)`                                                          |

## Behaviour notes

- **Timestamps** are sent as RFC3339 strings and parsed from either RFC3339 strings or epoch
  microseconds, so both `ts_type` encodings work.
- **Batch writes** split points into chunks of `chunkSize` (default 5000) per series per request.
  If some points in a chunk carry a status and others do not, missing statuses are filled with
  `Status.NOT_SET` (0), which is the API default.
- **Errors** are thrown as unchecked exceptions: `ApiException` (non-2xx, carries status code and
  the parsed `ErrorResponse`), `AuthenticationException` (401), `TransportException` (I/O).
- The client is `AutoCloseable` and safe to share across threads.

## Building

```bash
mvn test          # compile + unit tests (offline, embedded HTTP stub)
mvn package       # build the jar
```
