package com.fabiel.jtpc2.servlets.managers;

import com.fabiel.jtpc2.controllers.PostController;
import com.fabiel.jtpc2.controllers.ProxyController;
import com.fabiel.jtpc2.controllers.StatController;
import com.fabiel.jtpc2.controllers.UserController;
import com.fabiel.jtpc2.entities.Post;
import com.fabiel.jtpc2.entities.User;
import com.fabiel.jtpc2.utils.HtmlToPlain;
import com.fabiel.jtpc2.utils.Utils;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.List;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;

public class Manager extends HttpServlet {

    /**
     * usar https stricto en java
     *
     */
// Use HTTP Strict Transport Security to force client to use secure connections only
//boolean use_sts = true;
//
//if(use_sts) {
//    if(request.getScheme().equals("https")) {
//        // Send HSTS header
//        response.setHeader("Strict-Transport-Security", "max-age=10886400; includeSubDomains; preload");
//    } else {
//        // Redirect to HTTPS
//        response.setStatus(301);
//        String url = "https://" + request.getServerName();
//        if(request.getPathInfo() != null) {
//            url = url + "/" + request.getPathInfo();
//        }
//        if(request.getQueryString() != null && request.getQueryString().length() > 0) {
//            url = url + "?" + request.getQueryString();
//        }
//        response.setHeader("Location", url);
//    }
//}
    public Manager() {

    }

    public ObjectMapper getMapper() {
        return Utils.getInstance().getMapper();
    }

    public UserController getUC() {
        return Utils.getInstance().getUc();
    }

    public PostController getPC() {
        return Utils.getInstance().getPc();
    }

    public ProxyController getPxC() {
        return Utils.getInstance().getPxc();
    }

    public StatController getSc() {
        return Utils.getInstance().getSc();
    }

    public Session getS() {
        return Utils.getInstance().getS();
    }

    public InternetAddress[] getAdmins() {
        return Utils.getInstance().getAdmins();
    }

    private String getOSURL() {
        return Utils.getInstance().getOstesturl();
    }

    private void setOSURL(String s) {
        Utils.getInstance().setOstesturl(s);
    }
//    public void setPromo(String promo) {
//        Utils.getInstance().setTextoPromo(promo);
//    }
    Session session = getS(); 
    Calendar calend = Calendar.getInstance();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            MimeMessage message = new MimeMessage(getS(), request.getInputStream());
//            Message reply = message.reply(true);
//            reply.writeTo(System.out);
            String from = ((InternetAddress) message.getFrom()[0]).getAddress();
            String to = ((InternetAddress) message.getRecipients(Message.RecipientType.TO)[0]).getAddress().split("@")[0];
//            System.out.println("to = " + to);

//            System.out.println("subject = " + message.getSubject());
            if ((getUC().existUserEmail(from) || "fleon90@nauta.cu".equals(from)) && !"user".equals(to)) {
//                System.out.println("ejecutando www por correo ");

                InternetAddress address = (InternetAddress) message.getFrom()[0];
                int read;
                byte[] bs = new byte[204800];

                HtmlToPlain htpt = new HtmlToPlain();

                String trim;
                String contentType = message.getContentType();

                Object o = message.getContent();
                String s = message.getSubject().toLowerCase().trim();
//            System.out.println("metodo = " + s);
                if (contentType.toLowerCase().contains("text/plain")
                        || contentType.toLowerCase().startsWith("multipart")) {
                    if (contentType.toLowerCase().startsWith("multipart")) {
                        Multipart m = (Multipart) o;
                        OUTER:
                        for (int i = 0; i < m.getCount(); i++) {
                            BodyPart bodyPart = m.getBodyPart(i);
                            if (null != bodyPart.getContentType()) {
                                String partContentType = bodyPart.getContentType();
                                if (partContentType.contains("text/plain")) {
                                    o = bodyPart.getContent();
                                    break;
                                } else if (partContentType.contains("text/html")) {
                                    o = htpt.getPlainText(Jsoup.parse((String) bodyPart.getContent()), false);
                                }
                            }
                        }
                    }
//                    System.out.println("o = " + o);
                    if ("px".equals(s) || "mail".equals(s) || "amp".equals(s) || "get".equals(s) || "plain".equals(s) || "zip-plain".equals(s) || "zip-mail".equals(s) || "zip".equals(s) || "gzip-plain".equals(s) || "gzip-mail".equals(s) || "gzip".equals(s)
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
                                contenido //enviar las url pedidas para identificar la respuesta
                                + "\nadjunto le envio la respuesta del servidor\n"
                                + "Copyright Fabiel Leon 2014-" + calend.get(Calendar.YEAR)
                        );
                        mpResp.addBodyPart(texto);
                        String[] split = ((String) o).split("\n");
                        for (String string : split) {
                            trim = URLDecoder.decode(string.trim(), "UTF-8");
                            if (!isValidURL(trim)) {
                                texto.setText(texto.getContent() + "\n invalid URL: " + trim);
                                continue;
                            }
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
                                            ByteArrayInputStream bais = new ByteArrayInputStream(htpt.getMailText(Jsoup.connect(trim).timeout(0).get(), s).getBytes());
                                            while ((read = bais.read(bs)) != -1) {
                                                zos.write(bs, 0, read);
                                            }
                                            zos.flush();
                                            zos.closeEntry();
                                            break;
                                        }
//<editor-fold defaultstate="collapsed" desc="facebook y split">
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
//</editor-fold>
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
//                        respuesta.writeTo(System.out);
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
            } else {
                /**
                 * comprobar si es admin para agregar usuarios
                 */
                boolean isAdmin = false;
                for (InternetAddress admin : Utils.getInstance().getAdmins()) {
                    if (admin.getAddress().equals(from)) {
                        isAdmin = true;
                        break;
                    }
                }
                if (isAdmin) {
                    
                    MimeMessage send = new MimeMessage(getS());
                    String subject = message.getSubject().toLowerCase().trim();
                    String contenido = "";
                    if (message.getContent() instanceof Multipart) {
                        Multipart multi = (Multipart) message.getContent();
                        int count = multi.getCount();
                        for (int i = 0; i < count; i++) {
                            BodyPart bodyPart = multi.getBodyPart(i);
                            if (bodyPart.getContent() instanceof String) {
                                contenido = (String) bodyPart.getContent();
                                break;
                            }
                        }
                    } else if (message.getContent() instanceof String) {
                        contenido = (String) message.getContent();
                    }
                    //FIXME
//                contenido = contenido.replace("\n", "");
                    Object res = "ok";
                    switch (to) {
                        case "user":
                            User[] users = {};
                            switch (subject) {
                                case "re: add":
                                case "add":
                                    users = getMapper().readValue(contenido, users.getClass());
                                    for (User user : users) {
                                        getUC().create(user);
                                    }
                                    break;
                                case "delete":
                                    users = getMapper().readValue(contenido, users.getClass());
                                    for (User user : users) {
                                        Post post = new Post();
                                        post.setUserId(user.getUserId());
                                        getUC().destroy(user.getUserId());
                                        List<Post> findPost = getPC().findPost(post);
                                        for (Post fordelete : findPost) {
                                            getPC().destroy(fordelete.getPostId());
                                        }
                                    }
//                                o = getMapper().readValue(contenido, User.class);
//                                getUC().destroy(((User) o).getUserId());
                                    break;
                                case "edit":
                                    users = getMapper().readValue(contenido, users.getClass());
                                    for (User user : users) {
                                        getUC().edit(user);
                                    }
//                                o = getMapper().readValue(contenido, User.class);
//                                getUC().edit((User) o);
                                    break;
                                case "find":
                                    //FIXME poner por areglos y unir los resultados de las buscqueda por cada usuario del arreglo
                                    res = getUC().findUser(getMapper().readValue(contenido, User.class));
                                    break;
                                case "list":
                                    res = getUC().findUserEntities();
                                    break;
                                case "layout":
                                    res = User.layoutUser();
                                    break;
                                default:
                                    res = "las opciones para user son \n"
                                            + "\"opcion\" : \"parametro\"\n"
                                            + "add:     [user]\n"
                                            + "delete:  [user]\n"
                                            + "edit:    [user]\n"
                                            + "find:    user\n"
                                            + "list\n"
                                            + "layout\n";
                                    break;
                            }
                            break;
                        default:
                            res = "las opciones son\n"
                                    + "\"opcion\""
                                    + "user@\n"
                                    + "las opciones para user son \n"
                                    + "\"opcion\" : \"parametro\"\n"
                                    + "add:     [user]\n"
                                    + "delete:  [user]\n"
                                    + "edit:    [user]\n"
                                    + "find:    user\n"
                                    + "list\n"
                                    + "layout\n\n";
                            break;
                    }
                    String respuesta = getMapper().writeValueAsString(res)
                            //                .replace("{", "{\n").replace("}", "\n}")
                            //                        .replace(",", ",\n")
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                    System.out.println("respuesta " + respuesta);
                    send.setText(respuesta, "UTF-8");
                    send.setFrom(new InternetAddress(to + "." + subject + "ed" + "@myrevox.appspotmail.com"));
                    send.setSubject(subject + "ed");
                    send.setRecipients(Message.RecipientType.TO, from);
                    Transport.send(send);
                }
            }

        } catch (Exception ex) {
            try {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
                MimeMessage send = new MimeMessage(getS());
                send.setText(ex.getMessage(), "UTF-8");
                send.setFrom(new InternetAddress("error@myrevox.appspotmail.com"));
                send.setSubject("error");
                send.setRecipients(Message.RecipientType.TO, getAdmins());
                Transport.send(send);
            } catch (MessagingException ex1) {
                ex1.initCause(ex);
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private void test() throws IOException, Exception {
//       Post p = new Post();
//        p.setHead("asdasd");
//        p.setDayliCnt(Short.parseShort("123"));
//        p.setLast(Post.sdf.format(new Date()));
//        p.setPosted(new Date());
//        getUC().create(user);
//        User readValue = getMapper().readValue(getMapper().writeValueAsString(user), User.class);
//        System.out.println("userId " + readValue.getUserId());
//
//        p.setUserId(KeyFactory.keyToString(user.getUserId()));
//        getPC().create(p);
//        p.setText("asdasd");
//        getPC().edit(p);
//        System.out.println(getMapper().writeValueAsString(p));
//
//        List<User> findUser = getUC().findUser(readValue);
//        for (User user1 : findUser) {
//            System.out.println("find user " + getMapper().writeValueAsString(user1));
//        }
//        List<Post> findPost = getPC().findPost(p);
//        for (Post post : findPost) {
//            System.out.println("find post " + getMapper().writeValueAsString(post));
//        }
//        getPC().destroy(p.getPostId());
//        readValue.setMovil(234234);
//        getUC().edit(readValue);
//        User readValueNew = getMapper().readValue(getMapper().writeValueAsString(readValue), User.class);
//        getUC().destroy(readValueNew.getUserId());
        //all
//        List<User> findUserEntities = getUC().findUserEntities();
//        for (User user : findUserEntities) {
//            System.out.println("userList" + getMapper().writeValueAsString(user));
//        }
//        List<Post> findPostEntities = getPC().findPostEntities();
//        for (Post post : findPostEntities) {
//            System.out.println("postList" + getMapper().writeValueAsString(post));
//        }
    }

    public boolean isValidURL(String url) {
        URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }
        return true;
    }

    private URLConnection getURLConnection(String trim) throws MalformedURLException, IOException {
        URLConnection urlc = new URL(trim).openConnection();
        urlc.setConnectTimeout(0);
        urlc.setReadTimeout(0);
        return urlc;
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
