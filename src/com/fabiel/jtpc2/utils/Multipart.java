/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author administrador
 */
public class Multipart {

    /**
     * This utility class provides an abstraction layer for sending multipart
     * HTTP POST requests to a web server.
     *
     * @author www.codejava.net
     *
     */
    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private final HttpURLConnection httpConn;
    private final String charset;
    private final OutputStream outputStream;
    private final PrintWriter writer;

    /**
     * This constructor initializes a new HTTP POST request with content type is
     * set to multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public Multipart(String requestURL, String charset) throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);	// indicates POST method
        httpConn.setRequestMethod("POST");
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
//        httpConn.setRequestProperty("User-Agent", "CodeJava Agent");
//        httpConn.setRequestProperty("Test", "Bonjour");
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    /**
     * This constructor initializes a new HTTP POST request with content type is
     * set to multipart/form-data
     *
     * @param urlc
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public Multipart(URLConnection urlc, String charset) throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";

        httpConn = (HttpURLConnection) urlc;
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);	// indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    /**
     * Adds a form field to the request
     *
     * @param name field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @param name
     * @param contentType
     * @throws IOException
     */
    public void addFilePart(String fieldName, InputStream uploadFile, String name, String contentType)
            throws IOException {
//        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                + contentType)
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

//        try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = uploadFile.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        uploadFile.close();
        outputStream.flush();
//        }

        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @param name
     * @param contentType
     * @throws IOException
     */
    public void addFilePart(String fieldName, byte[] uploadFile, String name, String contentType)
            throws IOException {
        addFilePart(fieldName, new ByteArrayInputStream(uploadFile), name, contentType);
    }

    public void addFilePart(String fieldName, byte[] uploadFile, String name)
            throws IOException {
        addFilePart(fieldName, new ByteArrayInputStream(uploadFile), name, URLConnection.guessContentTypeFromName(name));
    }

    public void addFilePart(String fieldName, InputStream is, String name)
            throws IOException {
        addFilePart(fieldName, is, name, URLConnection.guessContentTypeFromName(name));
    }

    public void addFilePart(String fieldName, File file, String contentType)
            throws IOException {
        addFilePart(fieldName, new BufferedInputStream(new FileInputStream(file)), file.getName(), contentType);
    }

    public void addFilePart(String fieldName, File file)
            throws IOException {
        addFilePart(fieldName, new BufferedInputStream(new FileInputStream(file)), file.getName(), URLConnection.guessContentTypeFromName(file.getName()));
    }

    /**
     * Adds a header field to the request.
     *
     * @param name - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned status
     * OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public List<String> finish() throws IOException {
        List<String> response = new ArrayList<String>();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();
        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }

        return response;
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned status
     * OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();
        // checks server's status code first
//        int status = httpConn.getResponseCode();
        return httpConn.getInputStream();
    }

}
