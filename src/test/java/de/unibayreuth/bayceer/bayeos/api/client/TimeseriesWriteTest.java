package de.unibayreuth.bayceer.bayeos.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import de.unibayreuth.bayceer.bayeos.api.client.http.Json;
import de.unibayreuth.bayceer.bayeos.api.client.model.Status;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeseriesWriteTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2026-01-01T00:10:00Z");

    @Test
    void singleSeriesWriteSendsArrayWithStatusAndQueryFlag() throws Exception {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(201, "");

            try (BayeosClient client = simpleClient(stub)) {
                client.timeseries().write(6)
                        .point(T1, 1.5, Status.VALID)
                        .ignoreInvalidTimestamps(true)
                        .send();
            }

            StubServer.Recorded request = stub.lastRequest();
            assertEquals("POST", request.method);
            assertEquals("/timeseries/6/data", request.path);
            assertEquals("true", request.queryParam("ignore_invalid_timestamps"));

            JsonNode body = Json.mapper().readTree(request.body);
            assertTrue(body.isArray());
            assertEquals(1, body.size());
            assertEquals("2026-01-01T00:00:00Z", body.get(0).get("ts").asText());
            assertEquals(1.5, body.get(0).get("value").asDouble());
            assertEquals(1, body.get(0).get("status").asInt());
        }
    }

    @Test
    void batchWriteIsColumnOrientedAcrossSeries() throws Exception {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(201, "");

            try (BayeosClient client = simpleClient(stub)) {
                client.timeseries().writeBatch()
                        .series(6)
                        .point(T1, 1.0)
                        .point(T2, 2.0, Status.VALID_WITH_REMARKS)
                        .series(7)
                        .point(T1, 3.0)
                        .send();
            }

            assertEquals(1, stub.requestCount());
            StubServer.Recorded request = stub.lastRequest();
            assertEquals("/timeseries/data", request.path);

            JsonNode series = Json.mapper().readTree(request.body).get("series");
            assertEquals(2, series.size());

            JsonNode first = series.get(0);
            assertEquals(6, first.get("id").asInt());
            assertEquals(2, first.get("ts").size());
            assertEquals("2026-01-01T00:00:00Z", first.get("ts").get(0).asText());
            assertEquals("2026-01-01T00:10:00Z", first.get("ts").get(1).asText());
            assertEquals(1.0, first.get("values").get(0).asDouble());
            assertEquals(2.0, first.get("values").get(1).asDouble());
            assertEquals(2, first.get("status").get(1).asInt());

            JsonNode second = series.get(1);
            assertEquals(7, second.get("id").asInt());
            assertEquals(1, second.get("ts").size());
            assertEquals(3.0, second.get("values").get(0).asDouble());
        }
    }

    @Test
    void batchWriteChunksLargeSeries() throws Exception {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(201, "");
            stub.enqueue(201, "");
            stub.enqueue(201, "");

            try (BayeosClient client = simpleClient(stub)) {
                de.unibayreuth.bayceer.bayeos.api.client.timeseries.DataBatchBuilder.SeriesScope scope =
                        client.timeseries().writeBatch().series(6).chunkSize(2);
                for (int i = 0; i < 5; i++) {
                    scope.point(T1.plusSeconds(60L * i), i);
                }
                scope.send();
            }

            assertEquals(3, stub.requestCount());
            assertEquals(2, chunkSize(stub.request(0), 0));
            assertEquals(2, chunkSize(stub.request(1), 0));
            assertEquals(1, chunkSize(stub.request(2), 0));
        }
    }

    @Test
    void mixedStatusFillsMissingWithNotSet() throws Exception {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(201, "");

            try (BayeosClient client = simpleClient(stub)) {
                client.timeseries().writeBatch()
                        .series(6)
                        .point(T1, 1.0)
                        .point(T2, 2.0, Status.VALID)
                        .send();
            }

            JsonNode series = Json.mapper().readTree(stub.lastRequest().body).get("series").get(0);
            assertEquals(0, series.get("status").get(0).asInt());
            assertEquals(1, series.get("status").get(1).asInt());
        }
    }

    private static int chunkSize(StubServer.Recorded request, int seriesIndex) throws Exception {
        return Json.mapper().readTree(request.body).get("series").get(seriesIndex).get("ts").size();
    }

    private static BayeosClient simpleClient(StubServer stub) {
        return BayeosClient.builder().baseUrl(stub.baseUrl()).token("t").build();
    }
}
