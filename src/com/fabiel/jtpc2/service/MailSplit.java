/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.service;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author administrador
 */
public class MailSplit extends HttpServlet {

    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//       http://www.youtube.com/watch?v=dXJvsjkNP2c
        Queue queue = QueueFactory.getDefaultQueue();
        TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.POST);
        as.param("max", "");
        as.param("range", "");
        as.url("/split");
        queue.add(as);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {

            String max = request.getHeader("max");
            String hu = request.getHeader("hu");
            String hm = request.getHeader("hm");
            String range = request.getHeader("range");

            HttpURLConnection hurlc = (HttpURLConnection) getURLConnection(hu);
            hurlc.setRequestMethod(hm);
            if (range == null) {
                hurlc.addRequestProperty("Range", "bytes=0-" + max);
            } else {
                hurlc.addRequestProperty("Range", "bytes=" + range + "-" + (range + max));
            }
            String content = "";
            Map<String, Object> param = new HashMap(request.getParameterMap());
            Set<Entry<String, Object>> entrySet = param.entrySet();
            for (Iterator<Entry<String, Object>> it = entrySet.iterator(); it.hasNext();) {
                Entry<String, Object> entry = it.next();
                String val = request.getParameter(entry.getKey());
                if ("content".equals(entry.getKey())) {
                    content = val;
                    it.remove();
                    continue;
                }
                hurlc.addRequestProperty(entry.getKey(), val);
            }

            hurlc.setDoOutput(true);

            try (OutputStream outputStream = hurlc.getOutputStream()) {
                outputStream.write(content.getBytes());
            }

//        String filename = trim.substring(trim.lastIndexOf("/") + 1);
//        filename = !"".equals(filename) ? filename : "archivo";
            String type = hurlc.getContentType() != null ? hurlc.getContentType() : "application/octet-stream";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipos = new GZIPOutputStream(baos);
            copy(hurlc.getInputStream(), gzipos);

            MimeMessage respuesta = new MimeMessage(session);
            respuesta.addRecipient(Message.RecipientType.TO, new InternetAddress(request.getHeader("address")));
            respuesta.setFrom(new InternetAddress("app@simplejtpc.appspotmail.com", "App"));
            respuesta.setSubject("");

            ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), type);

            MimeBodyPart adjunto = new MimeBodyPart();
            Map<String, List<String>> headerFields = hurlc.getHeaderFields();
            for (Entry<String, List<String>> entry : headerFields.entrySet()) {
                String key = entry.getKey();
                List<String> list = entry.getValue();
                for (String val : list) {
                    adjunto.addHeader(key, val);
                }
            }
            adjunto.setDataHandler(new DataHandler(bads));

            Multipart mpResp = new MimeMultipart();
            mpResp.addBodyPart(adjunto);
            respuesta.setContent(mpResp);
            Transport.send(respuesta);
            //        Content-Range	Where in a full body message this partial message belongs
            //                Content-Range: bytes 21010-47021/47022

            //        MimeBodyPart adjunto = new MimeBodyPart();
            //        String[] headers = message.getHeader("HH");
            //
            //        HttpURLConnection hurlc = (HttpURLConnection) getURLConnection(message.getHeader("HU", null));
            //        for (String header : headers) {
            //            int indexOf = header.indexOf(":");
            //            String key = header.substring(0, indexOf);
            //            String value = header.substring(indexOf + 1);
            //            hurlc.addRequestProperty(key, value);
            //        }
            //        hurlc.setRequestMethod(message.getHeader("HM", null));
            //        hurlc.setDoOutput(true);
            //
            //        try (OutputStream outputStream = hurlc.getOutputStream()) {
            //            outputStream.write(contenido.getBytes());
            //        }
            //
            //        String filename = trim.substring(trim.lastIndexOf("/") + 1);
            //        filename = !"".equals(filename) ? filename : "archivo";
            //        String type = hurlc.getContentType() != null ? hurlc.getContentType() : "application/octet-stream";
            //
            //        ByteArrayDataSource bads = new ByteArrayDataSource(hurlc.getInputStream(), type);
            //        adjunto.setDataHandler(new DataHandler(bads));
            //        adjunto.setFileName(filename);
            //        mpResp.addBodyPart(adjunto);
            ;
        } catch (MessagingException ex) {
            Logger.getLogger(MailSplit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private URLConnection getURLConnection(String trim) throws MalformedURLException, IOException {
        URLConnection urlc = new URL(trim).openConnection();
        urlc.setConnectTimeout(0);
        urlc.setReadTimeout(0);
        return urlc;
    }

    private void copy(InputStream is, OutputStream out) throws IOException {
        byte[] bs = new byte[300000];
        int cont;
        while ((cont = is.read(bs)) != -1) {
            out.write(bs, 0, cont);
        }
        out.flush();
        out.close();
    }

}
