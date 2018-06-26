/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.servlets.cron;

import com.fabiel.jtpc2.controllers.PostController;
import com.fabiel.jtpc2.controllers.StatController;
import com.fabiel.jtpc2.entities.Post;
import com.fabiel.jtpc2.utils.Utils;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author administrador
 */
public class ReiniciarPost extends HttpServlet {

    public StatController getSc() {
        return Utils.getInstance().getSc();
    }

    public Session getS() {
        return Utils.getInstance().getS();
    }

    public InternetAddress[] getAdmins() {
        return Utils.getInstance().getAdmins();
    }

    public PostController getPC() {
        return Utils.getInstance().getPc();
    }

    public ObjectMapper getMapper() {
        return Utils.getInstance().getMapper();
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
        // try {

            List<Post> findPostEntities = getPC().findPostEntities();
            try {
                for (Post post : findPostEntities) {
                    post.setPostedCnt(0);
                    getPC().update(post);
                }
            } catch (Exception ex) {
                Logger.getLogger(ReiniciarPost.class.getName()).log(Level.SEVERE, null, ex);
            }
			
			
            // List<Stat> statErr = getSc().findStatErrorRange24Hours();
            // int bad = statErr.size();
            // int ok = (getSc().findStatRange24Hours().size() - bad);
            // StringBuilder builder = new StringBuilder();
            // while (!statErr.isEmpty()) {
                // Stat get = statErr.remove(0);
                // int cont = 0;
                // for (int i = 0; i < statErr.size(); i++) {
                    // Stat stat = statErr.get(i);
                    // if (stat.getMensaje().equals(get.getMensaje())) {
                        // statErr.remove(i);
                        // cont++;
                    // }
                // }
                // cont++;
                // builder.append(cont).append(" ").append(get.getMensaje()).append("\n");
            // }

            // String texto = "Se han reiniciado los post\n"
                    // + "Stat:\n"
                    // + "Se han publicado:\n"
                    // + ok + " post \n"
                    // + "y debido a errores se ha dejado de publicar " + bad + " post\n"
                    // + "Lista de erorres:\n"
                    // + builder;

            // MimeMessage send = new MimeMessage(getS());
            // send.setText(texto, "UTF-8");

            // send.setFrom(new InternetAddress("stat@myrevox.appspotmail.com"));
            // send.setSubject("Stat");
            // send.setRecipients(Message.RecipientType.TO, getAdmins());
            // Transport.send(send);
        // } catch (MessagingException ex) {
            // Logger.getLogger(ReiniciarPost.class.getName()).log(Level.SEVERE, null, ex);
        // }
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
