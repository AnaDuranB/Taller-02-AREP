package org.example;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Esta clase implementa un servidor HTTP básico que maneja solicitudes GET y POST para una API,
 * así como solicitudes para servir archivos estáticos (HTML, CSS, JS, imágenes) desde el servidor.
 * Escucha en el puerto 35000 y gestiona componentes que se almacenan en una lista.
 * No usa frameworks, solo bibliotecas estándar de Java.
 */

public class HttpServer {

    private static final Map<String, BiFunction<Request, Response, String>> routes = new HashMap<>();
    private static String staticFolder = "src/main/webapp"; // Directorio por defecto

    public static void main(String[] args) {
        staticfiles("webroot"); // Define la carpeta de archivos estáticos

        get("/hello", (req, res) -> "Hello " + req.getValues("name"));
        get("/pi", (req, res) -> String.valueOf(Math.PI));

        start(35000);
    }

    // Método para definir rutas REST con GET
    public static void get(String path, BiFunction<Request, Response, String> handler) {
        routes.put(path, handler);
    }

    // Método para definir el directorio de archivos estáticos
    public static void staticfiles(String folder) {
        staticFolder = "target/classes/" + folder;
    }

    // Inicia el servidor en el puerto especificado
    public static void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor en ejecución en el puerto " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleRequest(clientSocket)).start(); // Manejo concurrente de solicitudes
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("Solicitud: " + requestLine);
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String fullPath = requestParts[1];

            String path = fullPath.split("\\?")[0]; // Separa la URL base de los parámetros
            Map<String, String> queryParams = getQueryParams(fullPath);

            Request req = new Request(method, path, queryParams);
            Response res = new Response();

            if (routes.containsKey(path)) {
                // Ejecuta el handler correspondiente a la ruta REST
                String responseBody = routes.get(path).apply(req, res);
                sendResponse(out, 200, "text/plain", responseBody);
            } else {
                // Servir archivos estáticos si la ruta no es REST
                serveStaticFile(path, out);
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Extrae parámetros de la consulta de la URL (?name=Pedro&age=30)
    private static Map<String, String> getQueryParams(String fullPath) {
        Map<String, String> queryParams = new HashMap<>();
        if (fullPath.contains("?")) {
            String queryString = fullPath.split("\\?")[1];
            String[] params = queryString.split("&");

            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return queryParams;
    }

    // Sirve archivos estáticos
    private static void serveStaticFile(String path, OutputStream out) throws IOException {
        if (path.equals("/")) path = "/index.html";

        File file = new File(staticFolder + path);
        if (file.exists() && !file.isDirectory()) {
            String contentType = getContentType(path);
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            sendResponse(out, 200, contentType, new String(fileBytes));
        } else {
            sendResponse(out, 404, "text/plain", "404 Not Found");
        }
    }

    // Envia una respuesta HTTP
    private static void sendResponse(OutputStream out, int statusCode, String contentType, String body) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length() + "\r\n"
                + "\r\n"
                + body;
        out.write(response.getBytes());
        out.flush();
    }

    // Determina el tipo de contenido (MIME type)
    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg")) return "image/jpeg";
        return "text/plain";
    }
}