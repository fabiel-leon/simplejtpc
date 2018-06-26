/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.controllers;

import com.fabiel.jtpc2.entities.Stat;
import com.fabiel.jtpc2.exceptions.IllegalOrphanException;
import com.fabiel.jtpc2.exceptions.NonexistentEntityException;
import com.fabiel.jtpc2.exceptions.PreexistingEntityException;
import com.fabiel.jtpc2.utils.EMF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

/**
 *
 * @author administrador
 */
public class StatController {

    public StatController() {

    }

    public EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

    public void create(Stat stat) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.persist(stat);
        } catch (Exception ex) {
            if (stat.getStatId() != null) {
                if (StatController.this.findStat(stat.getStatId()) != null) {
                    try {
                        throw new PreexistingEntityException("Stat " + stat + " already exists.", ex);
                    } catch (PreexistingEntityException ex1) {
                        Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(List<Stat> stats) throws NonexistentEntityException, IllegalOrphanException, Exception {
        EntityManager em = getEntityManager();
        for (Stat stat : stats) {
            Stat reference;
            reference = em.getReference(Stat.class, stat.getStatId());
            em.remove(reference);
        }
    }

    public List<Stat> findStats() {
        EntityManager em = getEntityManager();
        ArrayList<Stat> users;
        try {
            Query q = em.createQuery("select s from Stat s");
            users = new ArrayList<>(q.getResultList());
            Collections.sort(users);
        } finally {
            em.close();
        }
        return users;
    }

    public List<Stat> findStats(Date from, Date to) {
        EntityManager em = getEntityManager();
        ArrayList<Stat> users;
        try {
            Query q = em.createQuery("select s from Stat s where date > :from AND date < :to");
            q.setParameter("from", from);
            q.setParameter("to", to);
            users = new ArrayList<>(q.getResultList());
            Collections.sort(users);
        } finally {
            em.close();
        }
        return users;
    }

    public List<Stat> findStatInRangeBefore(int hourBefore) {
        Date current = new Date();
        return findStats(new Date(current.getTime() - TimeUnit.HOURS.toMillis(hourBefore)), current);
    }

    public List<Stat> findStatRange24Hours() {
        return findStatInRangeBefore(24);
    }

    private List<Stat> findStatError(Date from, Date to) {
        List<Stat> findStats = findStats(from, to);
        List<Stat> ses = new LinkedList<>();
        for (Stat stat : findStats) {
            if (!"OK".equals(stat.getMensaje())) {
                ses.add(stat);
            }
        }
        return ses;
    }

    public List<Stat> findStatErrorInRangeBefore(int hoursBefore) {
        Date current = new Date();
        return findStatError(new Date(current.getTime() - TimeUnit.HOURS.toMillis(hoursBefore)), current);
    }

    public List<Stat> findStatErrorRange24Hours() {
        Date current = new Date();
        return findStatError(new Date(current.getTime() - TimeUnit.HOURS.toMillis(24)), current);
    }

    public Stat findStat(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Stat.class, id);
        } finally {
            em.close();
        }
    }

    public int getStatCount() {
        return StatController.this.findStats().size();
    }
}
