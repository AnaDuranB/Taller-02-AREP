package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {

    @BeforeAll
    static void startServer() {
        new Thread(() -> HttpServer.main(new String[]{})).start();
        try {
            Thread.sleep(1000); // Esperar a que el servidor inicie
        } catch (InterruptedException ignored) {}
    }

    private String sendHttpRequest(String request) throws IOException {
        try (Socket socket = new Socket("localhost", 35000);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            out.write(request.getBytes());
            out.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                if (line.isEmpty()) break; // Fin de encabezados
            }
            return response.toString();
        }
    }

    @Test
    void testGetHello() throws IOException {
        String response = sendHttpRequest("GET /hello?name=John HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertTrue(response.contains("200 OK"));
    }

    @Test
    void testGetPi() throws IOException {
        String response = sendHttpRequest("GET /pi HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertTrue(response.contains("200 OK"));
    }

    @Test
    void testPostComponent() throws IOException {
        String requestBody = "{\"name\": \"CPU\", \"type\": \"Processor\", \"price\": \"299.99\"}";
        String request = "POST /api/components HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + requestBody.length() + "\r\n\r\n" +
                requestBody;

        String response = sendHttpRequest(request);
        assertTrue(response.contains("201 Created"));
    }

    @Test
    void testInvalidRoute() throws IOException {
        String response = sendHttpRequest("GET /invalid HTTP/1.1\r\nHost: localhost\r\n\r\n");
        assertTrue(response.contains("404 Not Found"));
    }
}
