/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fabiel.jtpc2.utils;

/**
 *
 * @author administrador
 */

 
import java.io.File;
import java.io.IOException;
import java.util.List;
 
/**
 * This program demonstrates a usage of the MultipartUtility class.
 * @author www.codejava.net
 *
 */
public class Trigger {
 
    public static void main(String[] args) {
        String charset = "UTF-8";
        File uploadFile1 = new File("e:/Test/PIC1.JPG");
        File uploadFile2 = new File("e:/Test/PIC2.JPG");
        String requestURL = "http://localhost:8080/FileUploadSpringMVC/uploadFile.do";
 
        try {
            Multipart multipart = new Multipart(requestURL, charset);
             
            multipart.addHeaderField("User-Agent", "CodeJava");
            multipart.addHeaderField("Test-Header", "Header-Value");
             
            multipart.addFormField("description", "Cool Pictures");
            multipart.addFormField("keywords", "Java,upload,Spring");
             
            multipart.addFilePart("fileUpload", uploadFile1);
            multipart.addFilePart("fileUpload", uploadFile2);
 
            List<String> response = multipart.finish();
             
            System.out.println("SERVER REPLIED:");
             
            for (String line : response) {
                System.out.println(line);
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
}
