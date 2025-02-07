package org.example;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
public class HttpServerIntegrationTest {
    private static Process serverProcess;

    @BeforeAll
    public static void setUp() throws IOException {
        serverProcess = new ProcessBuilder("java", "-cp", "target/classes", "org.example.HttpServer")
                .start();
    }

    @AfterAll
    public static void tearDown() throws InterruptedException {
        if (serverProcess != null) {
            serverProcess.destroy();
            serverProcess.waitFor();
        }
    }

    @Test
    public void testServerResponse() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:35000/api/components"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }
}
