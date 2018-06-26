/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.entities;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author administrador
 */
@Entity
public class Proxy implements Comparable<Proxy> {

    public static Proxy layoutProxy() {
        Proxy p = new Proxy();
        p.setHost("10.32.23.23");
        p.setPort(8080);
        return p;
    }
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long proxyId;
    @Id
    private String host;
    private int port;
    private Date last = new Date();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @JsonIgnore
    public Date getLast() {
        return last;
    }

    public void setLast(Date last) {
        this.last = last;
    }

    public void updateLast() {
        this.last = new Date();
    }

    @JsonIgnore
    public Object getJdoDetachedState() {
        return new Object();
    }

    @Override
    public int compareTo(Proxy p) {
        if (getLast().getTime() > p.getLast().getTime()) {
            return 1;
        }
        if (getLast().getTime() < p.getLast().getTime()) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Proxy{" + "host=" + host + ", port=" + port + ", last=" + last + '}';
    }
    
}
