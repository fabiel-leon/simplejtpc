/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.controllers;

import com.fabiel.jtpc2.utils.EMF;
import com.fabiel.jtpc2.entities.Post;
import com.fabiel.jtpc2.entities.User;
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
public class PostController {

    public PostController() {

    }

    public EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

    private void verificarUser(EntityManager em, Long id) {
        User user = em.find(User.class, id);
        if (user == null) {
            throw new NullPointerException("El user con " + id + " no existe");
        }
    }

    public void create(Post post) throws PreexistingEntityException, Exception {

        EntityManager em = null;
        try {
            em = getEntityManager();

//            em.getTransaction().begin();
            verificarUser(em, post.getUserId());
            em.persist(post);

//            em.getTransaction().commit();
        } catch (Exception ex) {
            if (post.getPostId() != null) {
                if (findPost(post.getPostId()) != null) {
                    try {
                        throw new PreexistingEntityException("Post " + post + " already exists.", ex);
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

    public void edit(Post post) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            verificarUser(em, post.getUserId());
            Post find = em.find(Post.class, post.getPostId());
            post.setPosted(find.getPosted());
            post.setDinero(find.getDinero());
            post.setPostedCnt(find.getPostedCnt());
            post = em.merge(post);
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = post.getPostId();
                if (PostController.this.findPost(id) == null) {
                    throw new NonexistentEntityException("The post with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public synchronized void update(Post post) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            verificarUser(em, post.getUserId());
            post = em.merge(post);
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = post.getPostId();
                if (PostController.this.findPost(id) == null) {
                    throw new NonexistentEntityException("The post with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
//            em.getTransaction().begin();
            Post reference;
            try {
                reference = em.getReference(Post.class, id);
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
            }
//            User zip = reference.getUser();
//            if (zip != null) {
//                zip.getPosts().remove(reference);
//                zip = em.merge(zip);
//            }
            em.remove(reference);
//            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Post> findPostEntities() {
        return findPostEntities(true, -1, -1);
    }

    public List<Post> findPostEntities(int maxResults, int firstResult) {
        return findPostEntities(false, maxResults, firstResult);
    }

    private List<Post> findPostEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select p from Post p");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Post> findPostEntitiesforPublish() {
        return findPostEntitiesforPublish(true, -1);
    }

    public List<Post> findPostEntitiesforPublish(int maxResults) {
        return findPostEntitiesforPublish(false, maxResults);
    }

    private List<Post> findPostEntitiesforPublish(boolean all, int maxResults) {
        List<Post> findPostEntities = findPostEntities();
        List<Post> forPublish = new ArrayList<>();
        for (Post post : findPostEntities) {
//            System.out.println("post posted_cnt < daylipublish = " + (post.getPostedCnt() < post.getDayliPublish()));
//            System.out.println("post dinero > 0 = " + (post.getDinero() > 0));
            if ((post.getPostedCnt() < post.getDayliPublish()) && (post.getDinero() > 0)) {
                forPublish.add(post);
            }
        }
        Collections.sort(forPublish);
        if (all) {
            return forPublish;
        }

        return forPublish.subList(0, maxResults);
    }

    public Post findPost(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Post.class, id);
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

    public List<Post> findPost(Post post) {
        EntityManager em = getEntityManager();
        ArrayList<Post> posts;
        try {
            StringBuilder sb = new StringBuilder("select p from Post p");
            boolean where = false, postId = false, userId = false, head = false, text = false, price = false, dayliCnt = false;
            //key
            if (post.getPostId() != null) {
                where = appendInContext(sb, where);
                sb.append("postId = :postId");
                postId = true;
            }
            //key
            if (post.getUserId() != null) {
                where = appendInContext(sb, where);
                sb.append("userId = :userId");
                userId = true;
            }
            //head
            if (!"".equals(post.getTitulo()) && post.getTitulo() != null) {
                where = appendInContext(sb, where);
                sb.append("titulo = :titulo");
                head = true;
            }
            //text
            if (post.getTexto() != null && !"".equals(post.getTexto())) {
                where = appendInContext(sb, where);
                sb.append("texto = :texto");
                text = true;
            }
            //price
            if (post.getPrecio() != 0) {
                where = appendInContext(sb, where);
                sb.append("precio = :precio");
                price = true;
            }
            //cntDayli
            if (post.getDayliPublish() != 0) {
                appendInContext(sb, where);
                sb.append("dayliPublish = :dayliPublish");
                dayliCnt = true;
            }
//            System.out.println("sb post= " + sb);
            Query q = em.createQuery(sb.toString());
            if (userId) {
                q.setParameter("userId", post.getUserId());
            }
            if (postId) {
                q.setParameter("postId", post.getPostId());
            }
            if (head) {
                q.setParameter("titulo", post.getTitulo());
            }
            if (dayliCnt) {
                q.setParameter("dayliPublish", post.getDayliPublish());
            }
            if (text) {
                q.setParameter("texto", post.getTexto());
            }
            if (price) {
                q.setParameter("precio", post.getPrecio());
            }
            posts = new ArrayList<Post>(q.getResultList());
        } finally {
            em.close();
        }
        return posts;
    }
//    private List<Post> findPostEntitiesByUser(Key userKey) {
//        EntityManager em = getEntityManager();
//        try {
//            Query q = em.createQuery("select p from Post p where ");
//            q.setParameter("userKey", userKey);
//            
//            return q.getResultList();
//        } finally {
//            em.close();
//        }
//    }

    public int getPostCount() {
        return PostController.this.findPostEntities().size();
    }
    
    

}
