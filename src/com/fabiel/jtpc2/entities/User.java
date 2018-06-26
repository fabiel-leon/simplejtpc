/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.entities;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author docencia
 */
@Entity
//@PersistenceCapable(identityType = IdentityType.DATASTORE, detachable = "false")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    String name="", mail="";
    boolean admin;
    long movil, telf;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

//    @JsonIgnore
//    public Key getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Key key) {
//        this.userId = key;
//    }
//
//    public String getId() {
//        return KeyFactory.keyToString(userId);
//    }
//
//    public void setId(String id) {
//        this.userId = KeyFactory.stringToKey(id);
//    }
//    @XmlTransient
//    @JsonIgnore
//    public List<Post> getPosts() {
//        return posts;
//    }
//
//    public void setPosts(List<Post> posts) {
//        this.posts = posts;
//    }
    public static User layoutUser() {
        User u = new User();
        u.setMail("@");
        u.setAdmin(true);
        u.setName("");
        u.setMovil(5);
        u.setTelf(7);
        u.setUserId(Long.MIN_VALUE);
        return u;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public long getMovil() {
        return movil;
    }

    public void setMovil(long movil) {
        this.movil = movil;
    }

    public long getTelf() {
        return telf;
    }

    public void setTelf(long telf) {
        this.telf = telf;
    }

    @JsonIgnore
    public Object getJdoDetachedState() {
        return new Object();
    }

    @Override
    @XmlTransient
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.userId);
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.mail);
        hash = 37 * hash + (int) (this.movil ^ (this.movil >>> 32));
        hash = 37 * hash + (int) (this.telf ^ (this.telf >>> 32));
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
        final User other = (User) obj;
        if (!Objects.equals(this.userId, other.userId)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.mail, other.mail)) {
            return false;
        }
        if (this.movil != other.movil) {
            return false;
        }
        return this.telf == other.telf;
    }
}
