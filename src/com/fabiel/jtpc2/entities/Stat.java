/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.entities;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author administrador
 */
@Entity
public class Stat implements Comparable<Stat> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statId;
    String mensaje;
    Date date;

    public Stat(String mensaje) {
        this.mensaje = mensaje;
        this.date = new Date();
    }

    @JsonIgnore
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getStatId() {
        return statId;
    }

    public void setStatId(Long statId) {
        this.statId = statId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    @JsonIgnore
    public Object getJdoDetachedState() {
        return new Object();
    }

    @Override
    public int compareTo(Stat p) {
        if (getDate().getTime() > p.getDate().getTime()) {
            return 1;
        }
        if (getDate().getTime() < p.getDate().getTime()) {
            return -1;
        }
        return 0;
    }

}
