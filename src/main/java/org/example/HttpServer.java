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
 * Esta clase  implementa un servidor HTTP básico sin frameworks.
 * Permite manejar solicitudes GET y POST, servir archivos estáticos y gestionar una API.
 */
public class HttpServer {
    private static final List<Component> components = new ArrayList<>();

    private static final Map<String, BiFunction<Request, Response, String>> getRoutes = new HashMap<>();
    private static final Map<String, BiFunction<Request, Response, String>> postRoutes = new HashMap<>();
    private static String staticDirectory = "src/main/webapp";

    /**
     * Metodo principal que inicia el servidor, configura rutas y define el puerto.
     */
    public static void main(String[] args) {
        staticfiles("src/main/webapp");

        get("/hello", (req, res) -> "Hello " + req.getValues("name"));
        get("/pi", (req, res) -> String.valueOf(Math.PI));

        get("/api/components", (req, res) -> {
            if (components.isEmpty()) {
                return "[]";
            }
            return toJson(components);
        });

        post("/api/components", (req, res) -> {
            String body = req.getBody();
            Map<String, String> data = parseJson(body);

            if (data.containsKey("name") && data.containsKey("type") && data.containsKey("price")) {
                try {
                    double price = Double.parseDouble(data.get("price"));
                    components.add(new Component(data.get("name"), data.get("type"), price));
                    return "{\"message\": \"Component added successfully\"}";
                } catch (NumberFormatException e) {
                    return "{\"error\": \"Invalid price format\"}";
                }
            }
            return "{\"error\": \"Missing fields\"}";

        });

        start(35000);
    }

    /**
     * Define una ruta POST y su manejador.
     *
     * @param path La ruta de la solicitud POST.
     * @param handler El manejador de la solicitud, que recibe un objeto Request y Response.
     */
    public static void get(String path, BiFunction<Request, Response, String> handler) {
        getRoutes.put(path, handler);
    }

    /**
     * Define una ruta POST y su manejador.
     */
    public static void post(String path, BiFunction<Request, Response, String> handler) {
        postRoutes.put(path, handler);
    }

    /**
     * Configura el directorio donde se encuentran los archivos estáticos.
     *
     * @param path La ruta del directorio que contiene los archivos estáticos.
     */
    public static void staticfiles(String path) {
        staticDirectory = path;
        System.out.println("Archivos estáticos servidos desde: " + new File(staticDirectory).getAbsolutePath());
    }

    /**
     * Inicia el servidor en el puerto especificado.
     *
     * @param port El puerto en el que el servidor escuchará las solicitudes.
     */
    public static void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor en ejecución en el puerto " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }
    }

    /**
     * Maneja las solicitudes entrantes y genera las respuestas.
     *
     * @param clientSocket El socket del cliente que realiza la solicitud.
     */
    private static void handleRequest(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return;

            System.out.println("Solicitud: " + requestLine);
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) return;

            String method = requestParts[0];
            String fullPath = requestParts[1];
            String path = fullPath.split("\\?")[0];
            Map<String, String> queryParams = getQueryParams(fullPath);

            Request req = new Request(method, path, queryParams);
            Response res = new Response();

            // 🔹 Si la ruta está definida en el hashmap `routes`
            if (method.equals("GET") && getRoutes.containsKey(path)) {
                String responseBody = getRoutes.get(path).apply(req, res);
                sendResponse(out, 200, "application/json", responseBody);
            } else if (method.equals("POST") && postRoutes.containsKey(path)) {
                String body = readRequestBody(in);
                req.setBody(body);
                String responseBody = postRoutes.get(path).apply(req, res);
                sendResponse(out, 201, "application/json", responseBody);
            }
            else {
                System.out.println("Buscando archivo estático en: " + staticDirectory + path);
                serveStaticFile(path, out);
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee el cuerpo de la solicitud HTTP.
     *
     * @param in El BufferedReader que lee la solicitud.
     * @return El cuerpo de la solicitud como una cadena de texto.
     */
    private static String readRequestBody(BufferedReader in) throws IOException {
        StringBuilder body = new StringBuilder();
        int contentLength = 0;

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.substring(15).trim());
            }
        }

        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);
            body.append(buffer);
        }

        String jsonBody = body.toString();
        System.out.println("JSON Recibido: " + jsonBody);
        return jsonBody;
    }


    /**
     * Extrae parámetros de la consulta de la URL.
     *
     * @param fullPath La ruta completa de la URL, incluyendo los parámetros de consulta.
     * @return Un mapa con los parámetros de consulta.
     */
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

    /**
     * Sirve un archivo estático en respuesta a una solicitud HTTP.
     *
     * @param path la ruta solicitada del archivo.
     * @param out el OutputStream para enviar la respuesta.
     */
    private static void serveStaticFile(String path, OutputStream out) throws IOException {
        if (path.equals("/")) path = "/index.html";

        // Busca el archivo estático en la carpeta "src/main/webapp"
        File file = new File("src/main/webapp" + path);
        if (file.exists() && !file.isDirectory()) {
            // Determina el tipo de contenido según la extensión del archivo
            String contentType = getContentType(path);
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\n\r\n").getBytes());
            out.write(fileBytes);
        } else {
            // Si el archivo no existe, responde con 404.
            out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        }
    }

    /**
     * Envía una respuesta HTTP al cliente.
     *
     * @param out El OutputStream para enviar la respuesta.
     * @param statusCode El código de estado HTTP.
     * @param contentType El tipo de contenido de la respuesta.
     * @param body El cuerpo de la respuesta.
     */
    private static void sendResponse(OutputStream out, int statusCode, String contentType, String body) throws IOException {
        String statusLine = "HTTP/1.1 " + statusCode + " " + getStatusMessage(statusCode) + "\r\n";
        String headers = "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "Connection: close\r\n\r\n";

        out.write((statusLine + headers + body).getBytes());
        out.flush();
    }

    /**
     * Devuelve un mensaje de estado HTTP basado en el código de estado.
     *
     * @param statusCode El código de estado HTTP.
     * @return El mensaje de estado correspondiente.
     */
    private static String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Unknown Status";
        };
    }

    /**
     * Determina el tipo de contenido (MIME type) basado en la extensión del archivo.
     *
     * @param path La ruta del archivo.
     * @return El tipo de contenido correspondiente.
     */
    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpeg") || path.endsWith(".jpg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    /**
     * Parsea una cadena JSON en un mapa de clave-valor.
     *
     * @param json La cadena JSON a parsear.
     * @return Un mapa con los datos parseados.
     */
    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();

        if (json == null || json.isEmpty()) {
            return map; // Devuelve un mapa vacío si el JSON está vacío
        }

        json = json.trim();

        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1); // Elimina llaves solo si existen
        } else {
            return map; // Si no es un JSON válido, devuelve un mapa vacío
        }

        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }


    /**
     * Convierte la lista de componentes a un formato JSON manualmente.
     *
     * @param components La lista de componentes a convertir.
     * @return Una cadena JSON que representa la lista de componentes.
     */
    private static String toJson(List<Component> components) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < components.size(); i++) {
            Component component = components.get(i);
            json.append("{")
                    .append("\"name\": \"").append(component.getName()).append("\", ")
                    .append("\"type\": \"").append(component.getType()).append("\", ")
                    .append("\"price\": ").append(component.getPrice())
                    .append("}");
            if (i < components.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

}