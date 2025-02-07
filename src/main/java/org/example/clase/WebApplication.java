package org.example.clase;


import static org.example.clase.HttpServer.staticfiles;
import static org.example.clase.HttpServer.get;
import java.io.IOException;
import java.net.URISyntaxException;

public class WebApplication {
    public static void main(String[] args) throws IOException, URISyntaxException {
        staticfiles("/webroot");
        //get("/hello", (req, resp) -> "Hello " + req.getValues("name"));
        get("/pi", (req, res) -> {
            return String.valueOf(Math.PI);
        });
        get("/e", (req, res) -> {
            return String.valueOf(Math.E);
        });
//        get("/hello", (req, resp) -> "Hello " + req.getValues("name"));
        HttpServer.start(args);
    }
}