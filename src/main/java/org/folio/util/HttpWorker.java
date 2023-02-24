package org.folio.util;

import lombok.Setter;
import lombok.SneakyThrows;
import org.folio.model.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.folio.FolioUpdateAuthoritiesApp.exitWithError;

@Setter
public class HttpWorker {
    private final Configuration configuration;
    private String okapiToken;

    public HttpWorker(Configuration configuration) {
        this.configuration = configuration;
    }

    public HttpRequest constructGETRequest(String uri) {
        return constructRequest(uri).GET().build();
    }

    public HttpRequest constructDELETERequest(String uri) {
        return constructRequest(uri).DELETE().build();
    }

    @SneakyThrows
    public HttpRequest constructPOSTRequest(String uri, String body) {
        return constructRequest(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    @SneakyThrows
    public HttpRequest constructFilePOSTRequest(String uri, String fileBody) {
        return constructRequest(uri)
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBody.getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    @SneakyThrows
    public HttpRequest constructPUTRequest(String uri, String body) {
        return constructRequest(uri)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    public HttpRequest.Builder constructRequest(String uri) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(configuration.getOkapiUrl() + uri))
                .header("x-okapi-tenant", configuration.getTenant());

        if (okapiToken != null) {
            builder.header("x-okapi-token", okapiToken);
        }
        return builder;
    }

    public HttpRequest constructExternalRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
    }

    @SneakyThrows
    public HttpResponse<String> sendRequest(HttpRequest request) {
        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public void verifyStatus(HttpResponse<?> response, int expectedStatus, String errorMessage) {
        if (response.statusCode() != expectedStatus) {
            exitWithError(errorMessage + "\nResponse: " + response.body());
        }
    }
}
