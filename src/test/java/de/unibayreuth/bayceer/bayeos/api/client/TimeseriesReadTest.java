package de.unibayreuth.bayceer.bayeos.api.client;

import de.unibayreuth.bayceer.bayeos.api.client.model.BatchData;
import de.unibayreuth.bayceer.bayeos.api.client.model.SampleFunc;
import de.unibayreuth.bayceer.bayeos.api.client.model.SampleInterval;
import de.unibayreuth.bayceer.bayeos.api.client.model.SeriesData;
import de.unibayreuth.bayceer.bayeos.api.client.model.Status;
import de.unibayreuth.bayceer.bayeos.api.client.model.TsType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeseriesReadTest {

    private static final Instant FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TO = Instant.parse("2026-01-02T00:00:00Z");

    @Test
    void batchReadSendsQueryParamsAndParsesIsoTimestamps() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"ts_type\":\"iso\",\"sample_interval\":\"1h\",\"sample_func\":\"avg\","
                    + "\"series\":[{\"id\":6,\"ts\":[\"2026-01-01T00:00:00Z\"],\"values\":[1.5]}]}");

            try (BayeosClient client = simpleClient(stub)) {
                BatchData data = client.timeseries().readBatch()
                        .ids(6, 7)
                        .from(FROM)
                        .to(TO)
                        .sampleInterval(SampleInterval.HOURLY)
                        .sampleFunc(SampleFunc.AVG)
                        .statusFilter(Status.NOT_SET, Status.VALID)
                        .execute();

                assertEquals(1, data.getSeries().size());
                assertEquals("1h", data.getSampleInterval());
                SeriesData series = data.first();
                assertEquals(6, series.getId());
                assertEquals(FROM, series.getTs().get(0));
                assertEquals(1.5, series.getValues().get(0));
            }

            StubServer.Recorded request = stub.lastRequest();
            assertEquals("/timeseries/data", request.path);
            assertEquals("6,7", request.queryParam("ids"));
            assertEquals("2026-01-01T00:00:00Z", request.queryParam("from"));
            assertEquals("2026-01-02T00:00:00Z", request.queryParam("to"));
            assertEquals("1h", request.queryParam("sample_interval"));
            assertEquals("avg", request.queryParam("sample_func"));
            assertEquals("0,1", request.queryParam("status_filter"));
        }
    }

    @Test
    void batchReadParsesEpochMicrosecondTimestamps() {
        long micros = FROM.getEpochSecond() * 1_000_000L;
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"ts_type\":\"epoch\",\"series\":[{\"id\":6,\"ts\":[" + micros
                    + "],\"values\":[2.0]}]}");

            try (BayeosClient client = simpleClient(stub)) {
                BatchData data = client.timeseries().readBatch()
                        .ids(6)
                        .tsType(TsType.EPOCH)
                        .execute();
                assertEquals(FROM, data.first().getTs().get(0));
            }

            assertEquals("epoch", stub.lastRequest().queryParam("ts_type"));
        }
    }

    @Test
    void singleReadUsesLimit() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"series\":[]}");

            try (BayeosClient client = simpleClient(stub)) {
                client.timeseries().read(6).limit(10).execute();
            }

            StubServer.Recorded request = stub.lastRequest();
            assertEquals("/timeseries/6/data", request.path);
            assertEquals("10", request.queryParam("limit"));
        }
    }

    @Test
    void batchReadRequiresAtLeastOneId() {
        try (StubServer stub = new StubServer()) {
            try (BayeosClient client = simpleClient(stub)) {
                assertThrows(IllegalStateException.class,
                        () -> client.timeseries().readBatch().execute());
            }
            assertEquals(0, stub.requestCount());
        }
    }

    @Test
    void statsAndGapsParseResponses() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"count\":2,\"first_ts\":\"2026-01-01T00:00:00Z\","
                    + "\"last_ts\":\"2026-01-01T00:10:00Z\",\"min\":1.0,\"max\":2.0,\"mean\":1.5}");

            try (BayeosClient client = simpleClient(stub)) {
                assertEquals(2, client.timeseries().stats(6).execute().getCount());
            }

            stub.enqueue(200, "[{\"start\":\"2026-01-01T00:00:00Z\",\"end\":\"2026-01-01T00:30:00Z\","
                    + "\"duration_minutes\":30}]");
            try (BayeosClient client = simpleClient(stub)) {
                assertEquals(30, client.timeseries().gaps(6).thresholdMinutes(10).execute().get(0)
                        .getDurationMinutes());
            }
        }
    }

    @Test
    void batchReadWithoutStatusFilterOmitsParameter() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"series\":[]}");

            try (BayeosClient client = simpleClient(stub)) {
                client.timeseries().readBatch().ids(6).execute();
            }

            assertTrue(stub.lastRequest().queryParam("status_filter") == null);
        }
    }

    private static BayeosClient simpleClient(StubServer stub) {
        return BayeosClient.builder().baseUrl(stub.baseUrl()).token("t").build();
    }
}
