/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.servlets.posters;

import com.fabiel.jtpc2.controllers.PostController;
import com.fabiel.jtpc2.controllers.ProxyController;
import com.fabiel.jtpc2.controllers.StatController;
import com.fabiel.jtpc2.controllers.UserController;
import com.fabiel.jtpc2.entities.Category;
import com.fabiel.jtpc2.entities.Post;
import com.fabiel.jtpc2.entities.Stat;
import com.fabiel.jtpc2.entities.User;
import com.fabiel.jtpc2.utils.Multipart;
import com.fabiel.jtpc2.utils.Utils;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.util.ClientCookieManager;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

/**
 *
 * @author administrador
 */
public class CubaPoster extends HttpServlet {

    StringBuilder builder = new StringBuilder("Publicado:\n");

    private UserController getUC() {
        return Utils.getInstance().getUc();
    }

    public ObjectMapper getMapper() {
        return Utils.getInstance().getMapper();
    }

    private PostController getPC() {
        return Utils.getInstance().getPc();
    }

    private ProxyController getPxC() {
        return Utils.getInstance().getPxc();
    }

    private Session getS() {
        return Utils.getInstance().getS();
    }

    private InternetAddress[] getAdmins() {
        return Utils.getInstance().getAdmins();
    }

//    public String getPromo() {
//        return Utils.getInstance().getTextoPromo();
//    }
    private Random getR() {
        return Utils.getInstance().getR();
    }
//

    private String getOSURL() {
        return Utils.getInstance().getOstesturl();
    }

    private StatController getSc() {
        return Utils.getInstance().getSc();
    }

    private Post getUnUsedPost() {
        return Utils.getInstance().getUnUsedPost();
    }

    private void deleteUnUsedPost(Post p) {
        Utils.getInstance().deleteUnUsedPost(p);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        senMail("respuesta", builder.toString());
//        builder.setLength(0);
        builder.delete(0, builder.length());
        builder.append("Publicado:\n");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doHead(req, resp); //To change body of generated methods, choose Tools | Templates.
        List<Post> findPostEntitiesforPublish = getPC().findPostEntitiesforPublish();
        int nextInt = 5000;
        for (Post post : findPostEntitiesforPublish) {
            Queue queue = QueueFactory.getDefaultQueue();
            TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.PUT);
            as.countdownMillis(nextInt);
            nextInt += getR().nextInt(3000) + 5000;
            as.url("/cubapost");
            as.param("post", getMapper().writeValueAsString(post));
            as.param("email", "false");
            queue.add(as);
        }
    }

    /**
     * schedule post submited by post@ method post
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Post readValue = getMapper().readValue(req.getParameter("post"), Post.class);
        Set<Category> categorias = readValue.getCategorias();
        int nextInt = 5000;
        Queue queue = QueueFactory.getDefaultQueue();
        for (Category category : categorias) {
            LinkedHashSet<Category> linkedHashSet = new LinkedHashSet<>();
            linkedHashSet.add(category);
            readValue.setCategorias(linkedHashSet);
            TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.POST);
            as.countdownMillis(nextInt);
            nextInt += getR().nextInt(3000) + 5000;
            as.url("/cubapost");
            as.param("post", getMapper().writeValueAsString(readValue));
            queue.add(as);
        }
        if ("true".equals(req.getParameter("email"))) {
            nextInt += getR().nextInt(3000) + 5000;
            TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.HEAD);
            as.countdownMillis(nextInt);
//        nextInt += getR().nextInt(10000);
            as.url("/cubapost");
//        as.param("post", getMapper().writeValueAsString(readValue));
            queue.add(as);
        } else {
            builder.delete(0, builder.length());
            builder.append("Publicado:\n");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Post post = getMapper().readValue(request.getParameter("post"), Post.class);
            User findUser = null;
            try {
                findUser = getUC().findUser(post.getUserId());
            } catch (Exception e) {
                Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, e);
            }
            if (findUser == null) {
                findUser = new User();
                findUser.setMail("fleon90@nauta.cu");
                findUser.setMovil(54260597l);
                findUser.setName("Fabiel");
                findUser.setTelf(54260597l);
                findUser.setUserId(12312l);
            }
            String urlString = "http://www.cubisima.com/compraventa/publicar.html";
            URL url = new URL(urlString);
            URLConnection openConnection = url.openConnection();
            openConnection.setReadTimeout(0);
            openConnection.setConnectTimeout(0);
            Document parse = Jsoup.parse(openConnection.getInputStream(), "UTF-8", urlString);
            List<FormElement> forms = parse.select("form").forms();
            if (!forms.isEmpty()) {
                FormElement form = forms.get(0);
                if (form != null) {
                    List<Connection.KeyVal> formData = form.formData();
                    //add categoria
//                formData.add(HttpConnection.KeyVal.create("category", ""));
                    URL postURL = new URL(form.absUrl("action"));
                    System.out.println("postURL = " + form.absUrl("action"));
                    URLConnection postConnection = postURL.openConnection();
                    ClientCookieManager manager = new ClientCookieManager();
                    manager.readCookies(openConnection);
                    manager.writeCookies(postConnection);
//                    List<HttpCookie> cookies = HttpCookie.parse("Set-Cookie: " + openConnection.getHeaderField("set-cookie"));
//                    for (HttpCookie httpCookie : cookies) {
//                        postConnection.addRequestProperty("Cookie", httpCookie.getName());
//                    }
                    postConnection.addRequestProperty("Cookie", openConnection.getHeaderField("set-cookie"));

                    postConnection.setReadTimeout(0);
                    postConnection.setConnectTimeout(0);
                    Multipart m = new Multipart(postConnection, "UTF-8");
                    for (Connection.KeyVal keyVal : formData) {
                        if (null != keyVal.key()) {
                            switch (keyVal.key()) {
                                case "CodTipoCategoria":
                                    keyVal.value(Integer.toString(post.currentCategoria().getValue()));
                                    m.addFormField(keyVal.key(), keyVal.value());
                                    break;
                                case "Titulo":
                                    keyVal.value((Long.toString(findUser.getMovil() != 0 ? findUser.getMovil() : findUser.getTelf()) + "-" + post.getTitulo().toUpperCase()));
                                    m.addFormField(keyVal.key(), keyVal.value());
                                    break;
                                case "PrecioArticulo":
                                    keyVal.value(Integer.toString(post.getPrecio()));
                                    m.addFormField(keyVal.key(), keyVal.value());
                                    break;
                                case "DetalleArticulo":
                                    keyVal.value("<p>" + post.getTexto().toUpperCase() + "</p>");
                                    m.addFormField(keyVal.key(), keyVal.value());
                                    break;
                                case "Contacto":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                    m.addFormField(keyVal.key(), findUser.getName());
                                    break;
                                case "Correo":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                    m.addFormField(keyVal.key(), findUser.getMail());
                                    break;
                                case "TelefonoContacto":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                    m.addFormField(keyVal.key(), Long.toString(findUser.getTelf()));
                                    break;
                                case "Movil":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                    m.addFormField(keyVal.key(), Long.toString(findUser.getMovil()));
                                    break;
//                                case "ctl00$MainPlaceHolder$TextDireccionContacto":
////                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
//                                    m.addFormField(keyVal.key(), "mi direccion");
//                                    break;
                                case "ClassifiedPhotos[0].ImageStream":
                                case "ClassifiedPhotos[1].ImageStream":
                                case "ClassifiedPhotos[2].ImageStream":
                                case "ClassifiedPhotos[3].ImageStream":
                                case "ClassifiedPhotos[4].ImageStream":
                                case "ClassifiedPhotos[5].ImageStream":
                                    m.addFilePart(keyVal.key(), new byte[]{}, "", "application/octet-stream");
                                    break;
                                default:
                                    m.addFormField(keyVal.key(), keyVal.value());
                                    break;
                            }
                        }
                        System.out.println("keyVal = " + keyVal.key() + ", value = " + keyVal.value());
                    }
                    System.out.println("");
                    System.out.println("headers:");
                    Map<String, List<String>> headerFields = openConnection.getHeaderFields();
                    for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                        String string = entry.getKey();
                        List<String> list = entry.getValue();
                        System.out.println(string + ": " + (Arrays.toString(list.toArray())));
                    }
                    String text = Jsoup.parse(m.getInputStream(), "UTF-8", form.absUrl("action")).body().text();
                    String patrocinio = "Publicado";
                    String datos = "Datos de contacto";
                    if (!text.contains(patrocinio) && !text.contains(datos)) {
                        builder.append(post.currentCategoria()).append("-").append("no existe la palabra \'Publicado\' en el texto").append("\n");
                        System.out.println("texto: " + text);
                        getSc().create(new Stat("no existe la palabra \'Publicado\' en el texto"));
//                        throw new Exception(text);
                    } else {
                        String substring = text.substring(text.indexOf(patrocinio) + patrocinio.length(), text.indexOf(datos));
                        builder.append(post.currentCategoria()).append("-").append(substring).append("\n");
                        getSc().create(new Stat("OK"));
//                        senMail("respuesta", substring);
                    }
                } else {
                    builder.append(post.currentCategoria()).append("-").append("no existe form en la pag de insertar anuncio").append("\n");
                    getSc().create(new Stat("no existe form en la pag de insertar anuncio"));
//                    throw new NullPointerException("no existe form en la pag de insertar anuncio\n");
                }
            } else {
                builder.append(post.currentCategoria()).append("-").append("no existe form en la pag de insertar anuncio").append("\n");
                getSc().create(new Stat("no existe form en la pag de insertar anuncio"));
            }
        } catch (Exception e) {
            try {
                getSc().create(new Stat(e.getMessage()));
            } catch (Exception ex) {
                Logger.getLogger(CubaPoster.class.getName()).log(Level.SEVERE, null, ex);
            }
            builder.append(e.getLocalizedMessage()).append("\n");
            Logger.getLogger(CubaPoster.class.getName()).log(Level.SEVERE, null, e);
//            sendErrorMail(e);
        }
    }

    private void sendErrorMail(java.lang.Exception ex) {
        senMail("error", ex.getLocalizedMessage());
    }

    private void senMail(String asunto, String mensaje) {
        try {
            MimeMessage send = new MimeMessage(getS());
            send.setText(mensaje, "UTF-8");
            send.setFrom(new InternetAddress("app@myrevox.appspotmail.com"));
            send.setSubject(asunto);
            send.setRecipients(Message.RecipientType.TO, getAdmins());
            Transport.send(send);
        } catch (MessagingException ex1) {
            Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex1);
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

}
