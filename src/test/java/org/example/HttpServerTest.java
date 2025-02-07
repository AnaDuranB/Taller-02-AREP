package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

import java.nio.charset.StandardCharsets;

public class HttpServerTest {

    @Test
    public void testHandleApiRequestGet() throws IOException {
        HttpServer server = new HttpServer();
        String request = "GET /api/components HTTP/1.1\r\n\r\n"; // simulamos una solicitud GET
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedReader in = new BufferedReader(new StringReader(request)); // leemos la solicitud
        OutputStream out = new BufferedOutputStream(outputStream); // BufferedOutputStream para capturar la salida

        server.handleApiRequest("GET", "/api/components", in, out); // manejamos la solicitud

        // convertimos la respuesta a una cadena legible
        String response = outputStream.toString(StandardCharsets.UTF_8.name());
        System.out.println(response);

        assertTrue(response.contains("200 OK"), "La respuesta debe contener '200 OK'");
    }


    @Test
    public void testHandleApiRequestPost() throws IOException {
        HttpServer server = new HttpServer();
        String request = "POST /api/components HTTP/1.1\r\nContent-Length: 52\r\n\r\n{\"name\":\"RAM\", \"type\":\"Memoria RAM\", \"price\":100.0}";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedReader in = new BufferedReader(new StringReader(request));
        OutputStream out = new BufferedOutputStream(outputStream);

        server.handleApiRequest("POST", "/api/components", in, out);

        String response = outputStream.toString(StandardCharsets.UTF_8.name());
        System.out.println(response);
        assertTrue(response.contains("201 Created"));
    }
}
