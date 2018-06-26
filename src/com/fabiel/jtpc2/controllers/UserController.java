/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.controllers;

import com.fabiel.jtpc2.utils.EMF;
import com.fabiel.jtpc2.entities.User;
import com.fabiel.jtpc2.exceptions.IllegalOrphanException;
import com.fabiel.jtpc2.exceptions.NonexistentEntityException;
import com.fabiel.jtpc2.exceptions.PreexistingEntityException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Root;

/**
 *
 * @author docencia
 */
public class UserController implements Serializable {

    public UserController() {

    }

    public EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

    public void create(User user) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
//            em.getTransaction().begin();
            verifyUserEmail(user);
            verifyUserMovil(user);
            em.persist(user);
//            em.getTransaction().commit();
        } catch (Exception ex) {
            if (user.getUserId() != null) {
                if (findUser(user.getUserId()) != null) {
                    try {
                        throw new PreexistingEntityException("User " + user + " already exists.", ex);
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

    public void edit(User user) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
//            em.getTransaction().begin();
            User persistentUser = em.find(User.class, user.getUserId());
            if (persistentUser == null) {
                throw new Exception();
            }
            user = em.merge(user);
//            em.getTransaction().commit();
        } catch (Exception ex) {
            try {
                em.getTransaction().rollback();
            } catch (Exception re) {
                try {
                    throw new Exception("An error occurred attempting to roll back the transaction.", re);
                } catch (Exception ex1) {
                    Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = user.getUserId();
                if (findUser(id) == null) {
                    try {
                        throw new NonexistentEntityException("The user with id " + id + " no longer exists.");
                    } catch (NonexistentEntityException ex1) {
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

    public void destroy(Long id) throws NonexistentEntityException, IllegalOrphanException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
//            em.getTransaction().begin();
            User reference;
            try {
                reference = em.getReference(User.class, id);
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
            }
            em.remove(reference);
//            em.getTransaction().commit();
        } catch (NonexistentEntityException ex) {
            try {
                em.getTransaction().rollback();
            } catch (Exception re) {
                throw new Exception("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<User> findUserEntities() {
        return findUserEntities(true, -1, -1);
    }

    public List<User> findUserEntities(int maxResults, int firstResult) {
        return findUserEntities(false, maxResults, firstResult);
    }

    private List<User> findUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        ArrayList<User> users;
        try {
            Query q = em.createQuery("select u from User u");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            users = new ArrayList<User>(q.getResultList());
        } finally {
            em.close();
        }
        return users;
    }

    public User findUser(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    private boolean appendInContext(StringBuilder sb, boolean where) {
        if (!where) {
            where = true;
            sb.append(" where ");
        } else {
            sb.append(" AND ");
        }
        return where;
    }

    public List<User> findUser(User user) {
        EntityManager em = getEntityManager();
        ArrayList<User> users;
        try {
            StringBuilder sb = new StringBuilder("select u from User u");
            boolean where = false, userId = false, mail = false, movil = false, name = false, telf = false;
            //key
            if (user.getUserId() != null) {
                where = appendInContext(sb, where);
                sb.append("userId = :userId");
                userId = true;
            }
            //mail
            if (!"".equals(user.getMail()) && user.getMail() != null) {
                where = appendInContext(sb, where);
                sb.append("mail = :mail");
                mail = true;
            }
            //movil
            if (user.getMovil() != 0) {
                where = appendInContext(sb, where);
                sb.append("movil = :movil");
                movil = true;
            }
            //name
            if (!"".equals(user.getName()) && user.getName() != null) {
                where = appendInContext(sb, where);
                sb.append("name = :name");
                name = true;
            }
            //telf
            if (user.getTelf() != 0) {
                appendInContext(sb, where);
                sb.append("telf = :telf");
                telf = true;
            }

//            System.out.println("sb = " + sb);
            Query q = em.createQuery(sb.toString());
            if (userId) {
                q.setParameter("userId", user.getUserId());
            }
            if (movil) {
                q.setParameter("movil", user.getMovil());
            }
            if (mail) {
                q.setParameter("mail", user.getMail());
            }
            if (name) {
                q.setParameter("name", user.getName());
            }
            if (telf) {
                q.setParameter("telf", user.getTelf());
            }
            users = new ArrayList<User>(q.getResultList());
        } finally {
            em.close();
        }
        return users;
    }

    public int getUserCount() {
        return findUserEntities().size();
    }

    private void verifyUserMovil(User user) throws PreexistingEntityException {
        EntityManager em = getEntityManager();
        try {
            if (user.getMovil() != 0) {
                Query q = em.createQuery("select u from User u where movil = :movil");
                q.setParameter("movil", user.getMovil());
                q.setMaxResults(1);
                q.getSingleResult();
                throw new PreexistingEntityException("User " + user + " whit movil " + user.getMovil() + " already exists.");
            }
        } catch (javax.persistence.NoResultException e) {
        } finally {
            em.close();
        }
    }

    public void verifyUserEmail(User user) throws PreexistingEntityException {
        EntityManager em = getEntityManager();
        try {
            if (user.getMail() != null && !"".equals(user.getMail())) {
                Query q = em.createQuery("select u from User u where mail = :mail");
                q.setParameter("mail", user.getMail());
                q.setMaxResults(1);
                q.getSingleResult();
                throw new PreexistingEntityException("User " + user + " whit mail " + user.getMail() + " already exists.");
            }
        } catch (javax.persistence.NoResultException e) {
        } finally {
            em.close();
        }
    }

    public boolean existUserEmail(String mail) {
        User user = new User();
        user.setMail(mail);
        try {
            verifyUserEmail(user);
        } catch (PreexistingEntityException ex) {
//            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            return true;
        }
        return false;
    }
}
