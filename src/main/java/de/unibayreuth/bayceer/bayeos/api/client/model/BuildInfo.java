package de.unibayreuth.bayceer.bayeos.api.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Build information returned by the public {@code GET /version} endpoint.
 *
 * <p>The API marshals these fields with capitalised JSON keys.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class BuildInfo {

    @JsonProperty("Version")
    private String version;

    @JsonProperty("GitCommit")
    private String gitCommit;

    @JsonProperty("BuildDate")
    private String buildDate;

    BuildInfo() {
    }

    public String getVersion() {
        return version;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public String getBuildDate() {
        return buildDate;
    }

    @Override
    public String toString() {
        return "BuildInfo{version='" + version + "', gitCommit='" + gitCommit
                + "', buildDate='" + buildDate + "'}";
    }
}
