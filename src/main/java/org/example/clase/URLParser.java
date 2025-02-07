package org.example.clase;

import java.net.URL;

public class URLParser {
    public static void main(String[] args) throws Exception {
        URL myurl = new URL("http://is.ldbn.escuelaing.edu.co:80/index.html?val=90&t=56#events");

        System.out.println("Protocol: " + myurl.getProtocol());
        System.out.println("Authority: " + myurl.getAuthority());
        System.out.println("Host: " + myurl.getHost());
        System.out.println("Port: " + myurl.getPort());
        System.out.println("Path: " + myurl.getPath());
        System.out.println("Query: " + myurl.getQuery());
        System.out.println("File: " + myurl.getFile());
        System.out.println("Ref: " + myurl.getRef());
    }
}

