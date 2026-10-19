package de.unibayreuth.bayceer.bayeos.api.client;

import de.unibayreuth.bayceer.bayeos.api.client.model.BuildInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VersionTest {

    @Test
    void versionIsPublicAndParsesCapitalisedKeys() {
        try (StubServer stub = new StubServer()) {
            stub.enqueue(200, "{\"Version\":\"0.1.0\",\"GitCommit\":\"abc123\",\"BuildDate\":\"2026-01-01\"}");

            try (BayeosClient client = BayeosClient.builder().baseUrl(stub.baseUrl()).build()) {
                BuildInfo info = client.version();
                assertEquals("0.1.0", info.getVersion());
                assertEquals("abc123", info.getGitCommit());
                assertEquals("2026-01-01", info.getBuildDate());
            }

            StubServer.Recorded request = stub.lastRequest();
            assertEquals("/version", request.path);
            assertNull(request.authorization);
        }
    }
}
