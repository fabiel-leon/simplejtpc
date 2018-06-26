/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.service;

import com.fabiel.jtpc2.utils.HtmlToPlain;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;

/**
 *
 * @author administrador
 */
public class NewMailReceiver extends HttpServlet {

    HtmlToPlain htpt = new HtmlToPlain();
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);
    byte[] bs = new byte[204800];
    int read = 0;
    InternetAddress[] myEmails;
    private static final Logger LOG = Logger.getLogger(NewMailReceiver.class.getName());
    InternetAddress address;
    Calendar calend = Calendar.getInstance();

    public NewMailReceiver() {
        try {
            this.myEmails = new InternetAddress[]{
                //                new InternetAddress("fleon@estudiantes.uci.cu", "Fabiel Leon"),
                new InternetAddress("no-reply-2014@latinmail.com", "Fabiel Leon"),
                new InternetAddress("fleon90@nauta.cu", "Fabiel Leon"),
                new InternetAddress("fabiel@emsume.sld.cu", "Fabiel Leon"),};
            address = new InternetAddress("fleon@estudiantes.uci.cu");
        } catch (UnsupportedEncodingException | AddressException ex) {
            Logger.getLogger(NewMailReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String s = "";
        String trim = "";
        try {
            MimeMessage message = new MimeMessage(session, req.getInputStream());

            InternetAddress[] from = (InternetAddress[]) message.getFrom();
            address = from[0];

            String contentType = message.getContentType();

            Object o = message.getContent();
            s = message.getSubject().toLowerCase().trim();
//            System.out.println("metodo = " + s);
            if (contentType.toLowerCase().contains("text/plain")
                    || contentType.toLowerCase().startsWith("multipart")) {
                if (contentType.toLowerCase().startsWith("multipart")) {
                    Multipart m = (Multipart) o;
                    OUTER:
                    for (int i = 0; i < m.getCount(); i++) {
                        BodyPart bodyPart = m.getBodyPart(i);
                        if (null != bodyPart.getContentType()) {
                            switch (bodyPart.getContentType()) {
                                case "text/plain":
                                    o = bodyPart.getContent();
                                    break OUTER;
                                case "text/html":
                                    o = htpt.getPlainText(Jsoup.parse((String) bodyPart.getContent()), false);
                                    break OUTER;
                            }
                        }
                    }
                }

                if ("px".equals(s) || "mail".equals(s) || "amp".equals(s) || "get".equals(s) || "plain".equals(s) || "zip-plain".equals(s) || "zip-mail".equals(s) || "zip".equals(s) || "gzip-plain".equals(s) || "gzip".equals(s) || "gzip-mail".equals(s)
                        || Integer.parseInt(s) > 0) {
                    MimeMessage respuesta = new MimeMessage(session);
                    respuesta.addRecipient(Message.RecipientType.TO, address);
                    respuesta.setFrom(new InternetAddress("app@simplejtpc.appspotmail.com", "App"));
                    respuesta.setSubject(s);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ZipOutputStream zos = new ZipOutputStream(baos);
                    String contenido = ((String) o);
                    Multipart mpResp = new MimeMultipart();
                    BodyPart texto = new MimeBodyPart();
                    texto.setText(
                            contenido
                            + "\nadjunto le envio la respuesta del servidor\n"
                            + (address.getAddress().endsWith("uci.cu") ? "" : "Copyright Fabiel Leon 2014-" + calend.get(Calendar.YEAR))
                    );
                    mpResp.addBodyPart(texto);
                    String[] split = ((String) o).split("\n");
                    for (String string : split) {
                        trim = URLDecoder.decode(string.trim(), "UTF-8");
                        if (!"".equals(trim)) {
                            if (null != s) {
                                switch (s) {
                                    case "get": {
                                        MimeBodyPart adjunto = new MimeBodyPart();
                                        URLConnection urlc = getURLConnection(trim);
                                        String filename = trim.substring(trim.lastIndexOf("/") + 1);
                                        filename = !"".equals(filename) ? filename : "archivo";
                                        String type = urlc.getContentType() != null ? urlc.getContentType() : "application/octet-stream";
                                        ByteArrayDataSource bads = new ByteArrayDataSource(urlc.getInputStream(), type);
                                        adjunto.setDataHandler(new DataHandler(bads));
                                        adjunto.setFileName(filename);
                                        mpResp.addBodyPart(adjunto);
                                        break;
                                    }
                                    case "px": {
                                        String hu = message.getHeader("HU", null);
                                        String hm = message.getHeader("HM", null);
                                        String max = message.getHeader("MM", null);
                                        Queue queue = QueueFactory.getDefaultQueue();
                                        TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.GET);
                                        String[] headers = message.getHeader("HH");
                                        for (String header : headers) {
                                            int indexOf = header.indexOf(":");
                                            String key = header.substring(0, indexOf);
                                            String value = header.substring(indexOf + 1);
                                            as.param(key, value);
                                        }
                                        as.param("content", contenido.getBytes());
                                        as.header("mm", max);
                                        as.header("hu", hu);
                                        as.header("hm", hm);
                                        as.header("address", address.getAddress());
                                        as.url("/split");
                                        queue.add(as);
                                        break;
                                    }
                                    case "amp": {
                                        MimeBodyPart adjunto = new MimeBodyPart();
                                        ByteArrayDataSource bads = new ByteArrayDataSource(htpt.getMailText(Jsoup.connect(trim).timeout(0).get(), "amp"), "text/html; charset=utf-8");
                                        adjunto.setDataHandler(new DataHandler(bads));
                                        adjunto.setFileName("pagina.html");
                                        mpResp.addBodyPart(adjunto);
                                        break;
                                    }
                                    case "mail": {
                                        MimeBodyPart adjunto = new MimeBodyPart();
                                        ByteArrayDataSource bads = new ByteArrayDataSource(htpt.getMailText(Jsoup.connect(trim).timeout(0).get(), "mail"), "text/html; charset=utf-8");
                                        adjunto.setDataHandler(new DataHandler(bads));
                                        mpResp.addBodyPart(adjunto);
                                        break;
                                    }
                                    case "plain":
                                        MimeBodyPart adjunto = new MimeBodyPart();
                                        ByteArrayDataSource bads = new ByteArrayDataSource(htpt.getPlainText(Jsoup.connect(trim).timeout(0).get(), false), "text/plain; charset=utf-8");
                                        adjunto.setDataHandler(new DataHandler(bads));
                                        adjunto.setFileName("pagina.txt");
                                        mpResp.addBodyPart(adjunto);
//                                        texto.setText(htpt.getPlainText(Jsoup.connect(trim).timeout(0).get(), false));
                                        break;
                                    case "zip":
                                    case "gzip": {
                                        URLConnection urlc = getURLConnection(trim);
                                        String filename = trim.substring(trim.lastIndexOf("/") + 1);
                                        String type = urlc.getContentType() != null ? urlc.getContentType() : "application/octet-stream";
                                        filename = (!"".equals(filename) ? filename : "archivo") + (type.toLowerCase().contains("text/") ? ".html" : "");

                                        ZipEntry zipEntry = new ZipEntry(filename);
                                        zos.putNextEntry(zipEntry);
                                        BufferedInputStream bis = new BufferedInputStream(urlc.getInputStream());
                                        while ((read = bis.read(bs)) != -1) {
                                            zos.write(bs, 0, read);
                                        }
                                        zos.flush();
                                        zos.closeEntry();
                                        break;
                                    }
                                    case "zip-plain":
                                    case "gzip-plain": {
                                        URLConnection urlc = getURLConnection(trim);
                                        String filename = trim.substring(trim.lastIndexOf("/") + 1);
                                        String type = urlc.getContentType() != null ? urlc.getContentType() : "application/octet-stream";
                                        filename = (!"".equals(filename) ? filename : "archivo") + (type.toLowerCase().contains("text/") ? ".txt" : "");

                                        ZipEntry zipEntry = new ZipEntry(filename);
                                        zos.putNextEntry(zipEntry);
                                        ByteArrayInputStream bais = new ByteArrayInputStream(htpt.getPlainText(Jsoup.connect(trim).timeout(0).get(), false).getBytes());
                                        while ((read = bais.read(bs)) != -1) {
                                            zos.write(bs, 0, read);
                                        }
                                        zos.flush();
                                        zos.closeEntry();
                                        break;
                                    }
                                    case "zip-mail":
                                    case "gzip-mail": {
                                        URLConnection urlc = getURLConnection(trim);
                                        String filename = trim.substring(trim.lastIndexOf("/") + 1);
                                        String type = urlc.getContentType() != null ? urlc.getContentType() : "application/octet-stream";
                                        filename = (!"".equals(filename) ? filename : "archivo") + (type.toLowerCase().contains("text/") ? ".html" : "");

                                        ZipEntry zipEntry = new ZipEntry(filename);
                                        zos.putNextEntry(zipEntry);
                                        ByteArrayInputStream bais = new ByteArrayInputStream(htpt.getMailText(Jsoup.connect(trim).timeout(0).get(), "mail").getBytes());
                                        while ((read = bais.read(bs)) != -1) {
                                            zos.write(bs, 0, read);
                                        }
                                        zos.flush();
                                        zos.closeEntry();
                                        break;
                                    }
//                                    default:
//                                        int parseInt = Integer.parseInt(s);
//                                        String plainText = htpt.getPlainText(Jsoup.connect(trim).timeout(0).get(), false);
//                                        int length = plainText.length(),
//                                         cnt = 0;
//                                        //FIXME subir foto pal facebook
//                                        /**
//                                         * Upload a photo to an album. {
//                                         *
//                                         * @sample.xml
//                                         * ../../../doc/mule-module-facebook.xml.sample
//                                         * facebook:publishPhoto}
//                                         * @param albumId the id of the album
//                                         * object
//                                         * @param caption Caption of the photo
//                                         * @param photo File containing the
//                                         * photo
//                                         */
////@Processor @OAuthProtected public void publishPhoto(String albumId,String caption,File photo){
////  URI uri=UriBuilder.fromPath(FACEBOOK_URI).path("{albumId}/photos").build(albumId);
////  WebResource resource=this.newWebResource(uri,accessToken);
////  FormDataMultiPart multiPart=new FormDataMultiPart();
////  multiPart.bodyPart(new BodyPart(photo,MediaType.APPLICATION_OCTET_STREAM_TYPE));
////  multiPart.field("message",caption);
////  resource.type(MediaType.MULTIPART_FORM_DATA).post(multiPart);
////}
//
//                                        //FIXME
////                                        while (cnt < length) {
////                                            MimeMessage temp = new MimeMessage(session);
////                                            temp.addRecipient(Message.RecipientType.TO, address);
////                                            temp.setFrom(new InternetAddress("app@simplejtpc.appspotmail.com", "App"));
////                                            temp.setSubject(s);
////                                            temp.setText(plainText.substring(cnt, ((cnt + parseInt) <= length ? cnt + parseInt : length)), "utf-8");
////                                            Transport.send(temp);
////                                            cnt = cnt + parseInt;
//                                        TimeUnit.SECONDS.sleep(4);
////                                        }
//                                        break;
                                }
                            }
                        }
                    }
                    if ("zip".equals(s) || "zip-plain".equals(s) || "zip-mail".equals(s)) {
                        zos.flush();
                        zos.close();
                        MimeBodyPart adjunto = new MimeBodyPart();
                        ByteArrayDataSource bads = new ByteArrayDataSource(baos.toByteArray(), "application/zip");
                        adjunto.setDataHandler(new DataHandler(bads));
                        adjunto.setFileName("paginas.zip");
                        mpResp.addBodyPart(adjunto);

                    }
                    if ("gzip".equals(s) || "gzip-plain".equals(s) || "gzip-mail".equals(s)) {
                        try (ByteArrayOutputStream gzip = new ByteArrayOutputStream(); GZIPOutputStream gzipos = new GZIPOutputStream(gzip)) {
                            zos.flush();
                            zos.close();
                            gzipos.write(baos.toByteArray());
                            gzipos.flush();
                            gzipos.close();
                            MimeBodyPart adjunto = new MimeBodyPart();
                            ByteArrayDataSource bads = new ByteArrayDataSource(gzip.toByteArray(), "application/x-gzip");
                            adjunto.setDataHandler(new DataHandler(bads));
                            adjunto.setFileName("paginas.gz");
                            mpResp.addBodyPart(adjunto);
                        }
                    }
                    respuesta.setContent(mpResp);
                    Transport.send(respuesta);
                } else {
                    throw new NullPointerException(
                            "envie un correo de la siguiente manera\n" + ""
                            + "en asunto escriba: mail"
                            + "en el cuerpo del correo escriba: http://www.revolico.com/"
                            + "\n"
                            + "otros posibles asuntos son: get"
                            + ", mail"
                            + ", plain"
                            + ", zip"
                            + ", zip-plain"
                            + ", zip-mail"
                            + ", gzip"
                            + ", gzip-plain"
                            + ", gzip-mail"
                            + ", amp"
                    //                            + "asunto='get'~ resultado: las pag adjuntas\n"
                    //                            + "asunto='mail'~ resultado: las pag adjuntas\n"
                    //                            + "asunto='plain'~ resultado: las pag en texto plano \n"
                    //                            + "asunto='zip'~ resultado: las pag adjuntas comprimidas en un zip\n"
                    //                            + "asunto='zip-plain'~ resultado: las pag adjuntas en texto plano comprimidas en un zip  \n"
                    //                            + "asunto='gzip'~ resultado: las pag adjuntas comprimidas en un gzip\n"
                    //                            + "asunto='gzip-plain'~ resultado: las pag adjuntas en texto plano comprimidas en un gzip  \n"
                    );
                }

            }

        } catch (Exception ex) {
            try {
                MimeMessage respuesta = new MimeMessage(session);
                respuesta.setFrom(new InternetAddress("app@simplejtpc.appspotmail.com", "App"));
//                respuesta.addRecipients(Message.RecipientType.TO, myEmails);
                respuesta.addRecipient(Message.RecipientType.TO, address);
                respuesta.setSubject("Error " + s);
//                StringBuilder sb = new StringBuilder();
//                StackTraceElement[] stackTrace = ex.getStackTrace();
//                for (StackTraceElement stackTraceElement : stackTrace) {
//                    sb.append(stackTraceElement.toString()).append("\n");
//                }
                respuesta.setText(ex.toString() + trim);
                Transport.send(respuesta);
                LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            } catch (MessagingException | UnsupportedEncodingException ex1) {
                System.out.println(ex1.getLocalizedMessage());
                LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex1);
            }
        }
    }

    private InputStream getStream(String trim) throws MalformedURLException, IOException {
        URLConnection urlc = new URL(trim).openConnection();
        urlc.setConnectTimeout(0);
        urlc.setReadTimeout(0);
        return urlc.getInputStream();
    }

    private URLConnection getURLConnection(String trim) throws MalformedURLException, IOException {
        URLConnection urlc = new URL(trim).openConnection();
        urlc.setConnectTimeout(0);
        urlc.setReadTimeout(0);
        return urlc;
    }

}
