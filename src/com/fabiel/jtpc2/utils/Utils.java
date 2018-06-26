/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.utils;

import com.fabiel.jtpc2.controllers.PostController;
import com.fabiel.jtpc2.controllers.ProxyController;
import com.fabiel.jtpc2.controllers.StatController;
import com.fabiel.jtpc2.controllers.UserController;
import com.fabiel.jtpc2.entities.Post;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.j4gae.GaeJacksonModule;

/**
 *
 * @author administrador
 */
public class Utils {

    ObjectMapper mapper = new ObjectMapper();
    InternetAddress[] admins;
    UserController uc = new UserController();
    PostController pc = new PostController();
    ProxyController pxc = new ProxyController();
    StatController sc = new StatController();
    Properties p = new Properties();
    Session s = Session.getDefaultInstance(p);
    final Collection<Post> posts = new ConcurrentLinkedQueue<>();
//    String textoPromo = "";
    Random r = new Random();
    BasicCookieStore cookieStore = new BasicCookieStore();
    CloseableHttpClient httpclient = HttpClients.custom().
            setDefaultCookieStore(cookieStore).
            setUserAgent("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.102 Safari/537.36").
            setDefaultRequestConfig(
                    RequestConfig.custom().
                    setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).
                    setRelativeRedirectsAllowed(true).
                    setRedirectsEnabled(true).
                    setSocketTimeout(0).
                    setConnectTimeout(0).
                    setConnectionRequestTimeout(0).build()).build();
    String ostesturl = "http://ostest-truetest.rhcloud.com/OSTestApp/";

    private Utils() {
        p.put("mail.smtp.allow8bitmime", "true");//8 bitmime para usar 8 bits en vez de 7
//        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://google.com").openConnection();
//        connection.
        mapper.registerModule(new GaeJacksonModule());
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            this.admins = new InternetAddress[]{new InternetAddress("fleon90@nauta.cu")};
        } catch (AddressException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Post getUnUsedPost() {
        List<Post> findPostEntitiesforPublish = getPc().findPostEntitiesforPublish();
        Post postUsing = null;
        for (Post post : findPostEntitiesforPublish) {
            if (!posts.contains(post)) {
                posts.add(post);
                postUsing = post;
                break;
            }
        }
        return postUsing;
    }

    public String getOstesturl() {
        return ostesturl;
    }

    public void setOstesturl(String ostesturl) {
        this.ostesturl = ostesturl;
    }

    public void deleteUnUsedPost(Post p) {
        synchronized (posts) {
            posts.remove(p);
        }
    }

    public StatController getSc() {
        return sc;
    }

    public Random getR() {
        return r;
    }

    public void setR(Random r) {
        this.r = r;
    }

//    public CloseableHttpClient getHttpclient() {
//        return httpclient;
//    }
//    public String getTextoPromo() {
//        return textoPromo;
//    }
//
//    public void setTextoPromo(String textoPromo) {
//        this.textoPromo = textoPromo;
//    }
    public ProxyController getPxc() {
        return pxc;
    }

    public UserController getUc() {
        return uc;
    }

    public PostController getPc() {
        return pc;
    }

    public Session getS() {
        return s;
    }

    public InternetAddress[] getAdmins() {
        return admins;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public static Utils getInstance() {
        return MapperHolder.INSTANCE;
    }

    private static class MapperHolder {

        private static final Utils INSTANCE = new Utils();
    }
}
