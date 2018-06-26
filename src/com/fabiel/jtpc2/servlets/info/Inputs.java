/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.servlets.info;

import com.fabiel.jtpc2.entities.Category;
import com.fabiel.jtpc2.entities.Post;
import com.fabiel.jtpc2.utils.Multipart;
import com.fabiel.jtpc2.utils.Utils;
import com.google.appengine.tools.util.ClientCookieManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

/**
 *
 * @author administrador
 */
public class Inputs extends HttpServlet {

    public ObjectMapper getMapper() {
        return Utils.getInstance().getMapper();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String urlString = "http://www.cubisima.com/compraventa/publicar.html";
            URL url = new URL(urlString);
            URLConnection openConnection = url.openConnection();
            openConnection.setReadTimeout(0);
            openConnection.setConnectTimeout(0);
            Document parse = Jsoup.parse(openConnection.getInputStream(), "UTF-8", urlString);
            Elements select = parse.select("input");
            if (!select.isEmpty()) {
                for (Element ele : select) {
                    out.write(("name = " + ele.attr("name") + ", value = " + ele.val() + ", type = " + ele.attr("type") + "\n"));
                }
            }
            StringBuilder builder = new StringBuilder();
            Map<String, List<String>> headerFields = openConnection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                String string = entry.getKey();
                List<String> list = entry.getValue();
                builder.append(string).append(": ").append(Arrays.toString(list.toArray())).append("\n");
            }
            out.println("headerFields = \n" + builder);
            /* TODO output your page here. You may use following sample code. */
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try (PrintWriter out = resp.getWriter()) {
            String urlString = "http://www.cubisima.com/compraventa/publicar.html";
            URL url = new URL(urlString);
            URLConnection openConnection = url.openConnection();
            openConnection.setReadTimeout(0);
            openConnection.setConnectTimeout(0);
            StringBuilder builder = new StringBuilder();
            Map<String, List<String>> headerFields = openConnection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                String string = entry.getKey();
                List<String> list = entry.getValue();
                builder.append(string).append(": ").append(Arrays.toString(list.toArray())).append("\n");
            }
            out.println("headerFields = \n" + builder);

        }
//        super.doHead(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String urlString = "http://www.cubisima.com/compraventa/publicar.html";
            URL url = new URL(urlString);
            URLConnection openConnection = url.openConnection();
            openConnection.setReadTimeout(0);
            openConnection.setConnectTimeout(0);
            Document parse = Jsoup.parse(openConnection.getInputStream(), "UTF-8", urlString);
            Elements select = parse.select("input");
            if (!select.isEmpty()) {
                for (Element ele : select) {
                    out.println(("name = " + ele.attr("name") + ", value = " + ele.val() + ", type = " + ele.attr("type")));
                }
            }
            out.println("");
            out.println("headers:");
            Map<String, List<String>> headerFields = openConnection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                String string = entry.getKey();
                List<String> list = entry.getValue();
                out.println(string + ": " + (Arrays.toString(list.toArray())));
            }
//            out.println("headerFields = \n" + builder);
            /* TODO output your page here. You may use following sample code. */
        }
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
//            Post readValue = getMapper().readValue(request.getParameter("post"), Post.class);
        try (PrintWriter out = response.getWriter()) {
            Category[] cs = new Category[]{Category.accesorio, Category.antiguedades};
            for (Category category : cs) {
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
                                        keyVal.value(Integer.toString(category.getValue()));
                                        m.addFormField(keyVal.key(), keyVal.value());
                                        break;
                                    case "Titulo":
                                        keyVal.value(("venta memorias flash"));
                                        m.addFormField(keyVal.key(), keyVal.value());
                                        break;
                                    case "PrecioArticulo":
                                        keyVal.value(Integer.toString(10));
                                        m.addFormField(keyVal.key(), keyVal.value());
                                        break;
                                    case "DetalleArticulo":
                                        keyVal.value("<p>" + "venta flash" + "</p>");
                                        m.addFormField(keyVal.key(), keyVal.value());
                                        break;
                                    case "Contacto":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                        m.addFormField(keyVal.key(), "Ani");
                                        break;
                                    case "Correo":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                        m.addFormField(keyVal.key(),  "adn@gmail.com");
                                        break;
                                    case "TelefonoContacto":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                        m.addFormField(keyVal.key(), "5352345612");
                                        break;
                                    case "Movil":
//                        keyVal.value("<p>" + readValue.getTexto() + "</p>");
                                        m.addFormField(keyVal.key(), "5352345612");
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

//                        out.println("");
//                        out.println("headers:");
//                        Map<String, List<String>> headerFields = openConnection.getHeaderFields();
//                        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
//                            String string = entry.getKey();
//                            List<String> list = entry.getValue();
//                            out.println(string + ": " + (Arrays.toString(list.toArray())));
//                        }
//                    senMail("respuesta",
                        out.println("");
                        out.println("respuesta ficticia:");
                        String text = Jsoup.parse(m.getInputStream(), "UTF-8", form.absUrl("action")).body().text();
                        String patrocinio = "Patrocinar anuncio";
                        String datos = "Datos de contacto";
                        if (!text.contains(patrocinio) && !text.contains(datos)) {
                            out.println(text);
                        } else {
                            String substring = text.substring(text.indexOf(patrocinio) + patrocinio.length(), text.indexOf(datos));
                            out.println(substring);
                        }
                    } else {
                        out.println("no existe form en la pag");
                    }
                } else {
                    out.println("lista de formularios vacia no existe");
                }
            }
        } catch (Exception e) {
            log(e.getMessage(), e);
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
