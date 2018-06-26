/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.entities;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author docencia
 */
@Entity
public class Post implements Comparable<Post> {

//    public static final transient SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    String titulo;//titulo del post
//    @Basic(fetch = FetchType.LAZY)
    Text texto = new Text(""); //texto del post
    int precio;//precio del post
//    @Temporal(TemporalType.DATE)
    Date //last = new Date(),
            //ultima ves que se publico el anuncio
            posted = new Date();
    @JsonIgnore
    int lastCategoria = 0;//apuntador del array de categorias(apunta a la ultima categorai que se publico dentro del array de categorias) 

    int dayliPublish, //la cantidad de publicaciones diarias que se deberian hacer
            postedCnt,//cantidad publicaciones que se han hecho en el dia
            dinero;//cantidad de dinero restante que le queda al post, por cada publicacion reduce su numero en 1
    Long userId;//id del usuario al que pertenece este post
    Set<Category> categorias = new HashSet<Category>();//array de categorias
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @JsonIgnore
    private final Blob images[] = new Blob[3];//imagenes del post 

    public static Post layoutPost() {
        Post u = new Post();
        u.setTitulo("");
        u.setTexto("para escribir una linea nueva escriba \\n dentro del parrafo ");
        u.setPrecio(100);
        u.setDayliPublish(10);
        u.setUserId(0l);
        Set<Category> hashSet = new HashSet<>();
        Collections.addAll(hashSet, Category.values());
        u.setCategorias(hashSet);
        return u;
    }

    @JsonProperty
    public int getDinero() {
        return dinero;
    }

    @JsonIgnore
    public void setDinero(int dinero) {
        this.dinero = dinero;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Set<Category> getCategorias() {
        return categorias;
    }

    public void setCategorias(Set<Category> categorias) {
//        Set<Integer> categ = new HashSet<>();
//        for (String string : categorias) {
//            categ.add(Category.valueOf(head).getValue());
//        }
        this.categorias = categorias;
    }

    @JsonIgnore
    public void setPostedCnt(int postedCnt) {
        this.postedCnt = postedCnt;
    }

    @JsonProperty
    public int getPostedCnt() {
        return postedCnt;
    }

    @JsonIgnore
    public Date getPosted() {
        return posted;
    }

    public void setPosted(Date posted) {
        this.posted = posted;
    }

//    public String getLast() {
//        return sdf.format(last);
//    }
//
//    public void setLast(String last) {
//        try {
//            Date temp = sdf.parse(last);
//            this.last = temp;
//        } catch (ParseException ex) {
//            Logger.getLogger(Post.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    public Category currentCategoria() {
        if (lastCategoria >= categorias.size()) {
            lastCategoria = 0;
        }
        return (Category) categorias.toArray()[lastCategoria];
    }

    public synchronized void updatePosted() {
        this.posted = new Date();
        this.postedCnt = postedCnt + 1;
        this.dinero = dinero - 1;
        lastCategoria = lastCategoria + 1;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTexto() {
        return texto.getValue();
    }

    public void setTexto(String text) {
        this.texto = new Text(text);
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public int getDayliPublish() {
        return dayliPublish;
    }

    public void setDayliPublish(int dayliPublish) {
        this.dayliPublish = dayliPublish;
    }

    @JsonIgnore
    public Object getJdoDetachedState() {
        return new Object();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.postId);
        hash = 23 * hash + Objects.hashCode(this.titulo);
        hash = 23 * hash + Objects.hashCode(this.texto);
        hash = 23 * hash + Objects.hashCode(this.userId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Post other = (Post) obj;
        if (!Objects.equals(this.postId, other.postId)) {
            return false;
        }
        if (!Objects.equals(this.titulo, other.titulo)) {
            return false;
        }
        if (!Objects.equals(this.texto, other.texto)) {
            return false;
        }
        return Objects.equals(this.userId, other.userId);
    }

    @Override
    public int compareTo(Post p) {
        if (getPosted().getTime() > p.getPosted().getTime()) {
            return 1;
        }
        if (getPosted().getTime() < p.getPosted().getTime()) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Post{" + "postId=" + postId + ", titulo=" + titulo + ", texto=" + texto + ", precio=" + precio + ", posted=" + posted + ", lastCategoria=" + lastCategoria + ", dayliPublish=" + dayliPublish + ", postedCnt=" + postedCnt + ", dinero=" + dinero + ", userId=" + userId + ", categorias=" + categorias + '}';
    }

}
