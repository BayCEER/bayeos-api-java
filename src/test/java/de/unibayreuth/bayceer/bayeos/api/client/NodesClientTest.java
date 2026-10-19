package de.unibayreuth.bayceer.bayeos.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import de.unibayreuth.bayceer.bayeos.api.client.http.Json;
import de.unibayreuth.bayceer.bayeos.api.client.model.Node;
import de.unibayreuth.bayceer.bayeos.api.client.model.NodeKind;
import de.unibayreuth.bayceer.bayeos.api.client.model.NodeSearchResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodesClientTest {

    private static final String CREATED_NODE =
            "{\"id\":42,\"display_name\":\"AirTemp\",\"kind\":\"Folder\",\"parent_id\":2,"
                    + "\"node_path\":\"/Climate/AirTemp\",\"permission\":7}";

    @Test
    void createPathSendsNodePathAndDerivedDisplayName() throws Exception {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(201, CREATED_NODE);

            try (BayeosClient client = simpleClient(stub)) {
                Node node = client.nodes().createPath("~/Climate/AirTemp").execute();
                assertEquals(42, node.getId());
            }

            StubServer.Recorded request = stub.lastRequest();
            assertEquals("POST", request.method);
            assertEquals("/nodes", request.path);
            JsonNode body = Json.mapper().readTree(request.body);
            assertEquals("~/Climate/AirTemp", body.get("node_path").asText());
            assertEquals("AirTemp", body.get("display_name").asText());
            assertEquals("Folder", body.get("kind").asText());
            assertFalse(body.has("parent_id"));
        }
    }

    @Test
    void createPathWithExplicitKind() throws Exception {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(201, CREATED_NODE);

            try (BayeosClient client = simpleClient(stub)) {
                client.nodes().createPath("~/Climate/AirTemp", NodeKind.TIMESERIES).execute();
            }

            JsonNode body = Json.mapper().readTree(stub.lastRequest().body);
            assertEquals("Timeseries", body.get("kind").asText());
        }
    }

    @Test
    void createFolderSendsParentId() throws Exception {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(201, CREATED_NODE);

            try (BayeosClient client = simpleClient(stub)) {
                client.nodes().createFolder("Climate").parentId(2).execute();
            }

            JsonNode body = Json.mapper().readTree(stub.lastRequest().body);
            assertEquals(2, body.get("parent_id").asInt());
            assertEquals("Climate", body.get("display_name").asText());
            assertFalse(body.has("node_path"));
        }
    }

    @Test
    void settingBothParentIdAndPathFailsFast() {
        try (StubServer stub = new StubServer()) {
            try (BayeosClient client = simpleClient(stub)) {
                IllegalStateException error = assertThrows(IllegalStateException.class,
                        () -> client.nodes().createFolder("x").parentId(1).path("~/x").execute());
                assertEquals("Set either parentId or path, not both", error.getMessage());
            }
            assertEquals(0, stub.requestCount());
        }
    }

    @Test
    void searchSendsPagingParamsAndParsesResult() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"total\":1,\"hits\":[{\"id\":5,\"display_name\":\"A\","
                    + "\"kind\":\"Timeseries\",\"node_path\":\"~/A\"}]}");

            try (BayeosClient client = simpleClient(stub)) {
                NodeSearchResult result = client.nodes().search()
                        .pattern("~/**")
                        .kind(NodeKind.TIMESERIES)
                        .skip(5)
                        .limit(20)
                        .execute();

                assertEquals(1, result.getTotal());
                assertEquals(1, result.getHits().size());
                assertEquals(5, result.getHits().get(0).getId());
            }

            StubServer.Recorded request = stub.lastRequest();
            assertEquals("/nodes/search", request.path);
            assertEquals("~/**", request.queryParam("pattern"));
            assertEquals("Timeseries", request.queryParam("kind"));
            assertEquals("5", request.queryParam("skip"));
            assertEquals("20", request.queryParam("limit"));
        }
    }

    private static BayeosClient simpleClient(StubServer stub) {
        return BayeosClient.builder().baseUrl(stub.baseUrl()).token("t").build();
    }
}
