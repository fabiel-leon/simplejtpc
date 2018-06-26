/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.controllers;

import com.fabiel.jtpc2.utils.EMF;
import com.fabiel.jtpc2.entities.Proxy;
import com.fabiel.jtpc2.exceptions.IllegalOrphanException;
import com.fabiel.jtpc2.exceptions.NonexistentEntityException;
import com.fabiel.jtpc2.exceptions.PreexistingEntityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;

/**
 *
 * @author administrador
 */
public class ProxyController {

    public ProxyController() {

    }

    public EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

    public void create(Proxy proxy) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            if (proxy.getPort() == 0) {
                throw new Exception("proxy whitout port");
            }
            em.persist(proxy);
        } catch (Exception ex) {
            if (proxy.getHost() != null) {
                if (findProxy(proxy.getHost()) != null) {
                    try {
                        throw new PreexistingEntityException("Proxy " + proxy + " already exists.", ex);
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

    public void edit(Proxy proxy) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            Proxy pers = em.find(Proxy.class, proxy.getHost());
            if (pers == null) {
                throw new Exception();
            } else if (proxy.getPort() == 0) {
                throw new Exception("proxy whitout port");
            }
            proxy.setLast(pers.getLast());
            proxy = em.merge(proxy);
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
                String id = proxy.getHost();
                if (findProxy(id) == null) {
                    try {
                        throw new NonexistentEntityException("The proxy with id " + id + " no longer exists.");
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

    public void update(Proxy proxy) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            proxy = em.merge(proxy);
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
                String id = proxy.getHost();
                if (findProxy(id) == null) {
                    try {
                        throw new NonexistentEntityException("The proxy with id " + id + " no longer exists.");
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

    public void destroy(String id) throws NonexistentEntityException, IllegalOrphanException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
//            em.getTransaction().begin();
            Proxy reference;
            try {
                reference = em.getReference(Proxy.class, id);
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The proxy with id " + id + " no longer exists.", enfe);
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

    public List<Proxy> findProxyEntities() {
        return findProxyEntities(true, -1, -1);
    }

    public List<Proxy> findProxyEntities(int maxResults, int firstResult) {
        return findProxyEntities(false, maxResults, firstResult);
    }

    private List<Proxy> findProxyEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        ArrayList<Proxy> proxys;
        try {
            Query q = em.createQuery("select p from Proxy p");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            proxys = new ArrayList<Proxy>(q.getResultList());
            Collections.sort(proxys);
        } finally {
            em.close();
        }
        return proxys;
    }

    public Proxy findProxy(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Proxy.class, id);
        } finally {
            em.close();
        }
    }

}
