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
import com.fabiel.jtpc2.entities.Post;
import com.fabiel.jtpc2.entities.Proxy;
import com.fabiel.jtpc2.entities.Stat;
import com.fabiel.jtpc2.entities.User;
import com.fabiel.jtpc2.utils.Multipart;
import com.fabiel.jtpc2.utils.Utils;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
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
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

/**
 *
 * @author administrador
 */
public class Poster extends HttpServlet {

//    private ObjectMapper getMapper() {
//        return Utils.getInstance().getMapper();
//    }
    private UserController getUC() {
        return Utils.getInstance().getUc();
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            Queue queue = QueueFactory.getDefaultQueue();
            List<Proxy> findProxyEntities = getPxC().findProxyEntities();
            int cont = 0;
            if (findProxyEntities.isEmpty()) {
                throw new NullPointerException("no hay post que publicar");
            }
            for (Proxy proxy : findProxyEntities) {
                TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.PUT);
                as.param("host", proxy.getHost());
                as.countdownMillis((cont = cont + 5000) + getR().nextInt(3000));
                System.out.println("cont = " + cont);
                as.url("/post");
                queue.add(as);
            }
        } catch (Exception ex) {
            try {
                getSc().create(new Stat(ex.getMessage()));
//                sendErrorMail(ex);
            } catch (Exception ex1) {
                Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Proxy lastproxy = getPxC().findProxy(req.getParameter("host"));
            //<editor-fold defaultstate="collapsed" desc="nuevo ">
            String urls = formURL("http://kandalfghost.hopto.me/insertar-anuncio.html", lastproxy);
            URLConnection httpGet = getURLConnection(urls);
            Document document = Jsoup.parse(httpGet.getInputStream(), null, urls);
            Elements iframes = document.select("iframe");
            if (!iframes.isEmpty()) {
                Element iframe = iframes.first();
                String iframeURL = iframe.attr("src");
                URLConnection iframeGet = getURLConnection(iframeURL);
//                HttpGet iframeGet = new HttpGet(iframeURL);
//                HttpResponse iframeResponse = getClient().execute(iframeGet);
                Document iframeDoc = Jsoup.parse(iframeGet.getInputStream(), null, iframeURL);
                List<FormElement> iframeForms = iframeDoc.select("form").forms();
                if (!iframeForms.isEmpty()) {
                    FormElement iframeForm = iframeForms.get(0);
                    List<Connection.KeyVal> iframeData = iframeForm.formData();
                    List<FormElement> forms = document.select("form[name=\"insertad\"]").forms();
                    if (!forms.isEmpty()) {
                        FormElement get = forms.get(0);
                        List<Connection.KeyVal> formData = get.formData();
                        formData.addAll(iframeData);
                        //add categoria
                        formData.add(HttpConnection.KeyVal.create("category", ""));

                        Queue queue = QueueFactory.getDefaultQueue();
                        TimeUnit tu = TimeUnit.SECONDS;
                        int nextInt = getR().nextInt(10);
                        long addFuture = tu.toMillis(30) + tu.toMillis(nextInt);
                        long future = lastproxy.getLast().getTime() + addFuture;
                        Date current = new Date();
                        if (future < current.getTime()) {
                            future = current.getTime() + addFuture;
                        }
                        TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.POST).url("/post").etaMillis(future);
                        for (Connection.KeyVal keyVal : formData) {
                            as.param(keyVal.key(), keyVal.value());
                        }
                        as.header("phost", lastproxy.getHost());
                        queue.add(as);
                    } else {
                        throw new NullPointerException("no se encontro el formulario de insertar anuncio"
                                + "proxy: " + lastproxy.getHost() + " "
                                + document.body().outerHtml());
                    }
                } else {
                    throw new NullPointerException("no existen formularios en el iframe descargado\n"
                            + "proxy: " + lastproxy.getHost() + " "
                            + iframeDoc.body().outerHtml());
                }
            } else {
                throw new NullPointerException("no existe un tag que sea iframe en la pag de insertar anuncio\n"
                        + "proxy: " + lastproxy.getHost() + " "
                        + document.body().outerHtml());
            }
            //</editor-fold>
        } catch (Exception ex) {
            try {
                getSc().create(new Stat(ex.getMessage()));
//                sendErrorMail(ex);
            } catch (Exception ex1) {
                Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Post post = getUnUsedPost();
            if (post != null) {
                Proxy lastproxy = getPxC().findProxy(req.getHeader("phost"));
                User findUser = getUC().findUser(post.getUserId());
                Post promo = getPC().findPost(1l);
                System.out.println("lastproxy = " + lastproxy);
                String urls = formURL("http://kandalfghost.hopto.me/insertar-anuncio.html", lastproxy);
                Multipart multipart = new Multipart(getURLConnection(urls), "UTF-8");
                Map<String, String> parameterMap = req.getParameterMap();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    String key = entry.getKey();
                    String val = req.getParameter(key);
                    switch (key) {
                        case "cancel_form":
                        case "submit":
                        case "email_enabled":
                            continue;//eliminar innecesarios
                        case "ad_price":
                            val = (Integer.toString(post.getPrecio()));
                            break;
                        case "ad_headline":
                            val = ("telf:" + Long.toString(findUser.getMovil() != 0 ? findUser.getMovil() : findUser.getTelf()) + " " + post.getTitulo().toUpperCase());
                            break;
                        case "ad_text":
                            val = (post.getTexto().toUpperCase() + "\n\n"
                                    + (promo != null ? promo.getTexto() : ""));
                            break;
                        case "email":
                            val = (!"".equals(findUser.getMail()) ? findUser.getMail() : "promotion@realpromotion.es");
                            break;
                        case "name":
                            val = (findUser.getName().toUpperCase());
                            break;
                        case "phone":
                            val = (Long.toString(findUser.getMovil() != 0 ? findUser.getMovil() : findUser.getTelf()));
                            break;
                    }
                    if ("ad_picture_a".equals(key) || "ad_picture_b".equals(key) || "ad_picture_c".equals(key)) {
                        multipart.addFilePart(key, new byte[]{}, "");
                    } else {
                        multipart.addFormField(key, val);
                    }
                }
                Document doc = Jsoup.parse(multipart.getInputStream(), null, urls);
                String toLowerCase = doc.body().text().toLowerCase();
                boolean contains = toLowerCase.contains("tu anuncio ha sido insertado satisfactoriamente");
                if (contains) {
                    post.updatePosted();
                    lastproxy.updateLast();
                    getPC().update(post);
                    getPxC().update(lastproxy);
                    deleteUnUsedPost(post);
                } else {
                    throw new NullPointerException("La pagina no contiene las palabras \"tu anuncio ha sido insertado satisfactoriamente\":\n"
                            + "proxy: " + lastproxy.getHost() + "\n"
                            + toLowerCase);
                }
                Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("America/Havana"));
                instance.setTime(new Date());
                int get = instance.get(Calendar.HOUR_OF_DAY);
                if (get < 21) {
                    Queue queue = QueueFactory.getDefaultQueue();
                    TaskOptions as = TaskOptions.Builder.withMethod(TaskOptions.Method.PUT);
                    as.countdownMillis(getR().nextInt(2500));
                    as.url("/post");
                    queue.add(as);
                    getSc().create(new Stat("OK"));
                }
            } else {
                throw new NullPointerException("No hay post que publicar por hoy");
            }
        } catch (Exception ex) {
            try {
                getSc().create(new Stat(ex.getMessage()));
//                sendErrorMail(ex);
            } catch (Exception ex1) {
                Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String formURL(String revo, Proxy proxy) {
        return getOSURL() + "?" + "url=" + revo + "&host=" + proxy.getHost() + "&port=" + proxy.getPort();
    }

    private void sendErrorMail(java.lang.Exception ex) {
        try {
            MimeMessage send = new MimeMessage(getS());
            send.setText(ex.getMessage(), "UTF-8");
            send.setFrom(new InternetAddress("error@myrevox.appspotmail.com"));
            send.setSubject("error");
            send.setRecipients(Message.RecipientType.TO, getAdmins());
            Transport.send(send);
        } catch (MessagingException ex1) {
            Logger.getLogger(Poster.class.getName()).log(Level.SEVERE, null, ex1);
        }
    }

    private URLConnection getURLConnection(String trim) throws MalformedURLException, IOException {
        URLConnection urlc = new URL(trim).openConnection();
        urlc.setConnectTimeout(0);
        urlc.setReadTimeout(0);
        return urlc;
    }
}
