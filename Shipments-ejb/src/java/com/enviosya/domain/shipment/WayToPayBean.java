/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.enviosya.domain.shipment;


import com.enviosya.persistence.shipment.WayToPayEntity;
import com.google.gson.Gson;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Alvaro
 */
@Stateless
@LocalBean
public class WayToPayBean {
static Logger log = Logger.getLogger("FILE");
    @PersistenceContext
    private EntityManager em;
    @PostConstruct
    private void init() {
    }

public WayToPayEntity agregar(WayToPayEntity unPago) {
        try {
        em.persist(unPago);
        } catch (Exception e) {
            log.error("Error al agregar:" + this.getClass().toString()
                    + e.getMessage());
            return null;
        }
        return unPago;
    }

    public WayToPayEntity agregar(String body) {
       try {
       Gson gson = new Gson();
       WayToPayEntity unPago = gson.fromJson(body, WayToPayEntity.class);
        em.persist(unPago);
        return unPago;
        } catch (Exception e) {
            log.error("Error al agregar:" + this.getClass().toString()
                    + e.getMessage());
            return null;
        }
    }
    public WayToPayEntity modificar(Long id, String descripcionNueva) {
        try {
            WayToPayEntity unPago = em.find(WayToPayEntity.class, id);
            unPago.setDescripcion(descripcionNueva);
            em.merge(unPago);
            return unPago;
        } catch (Exception e) {
            log.error("Error al modificar:" + this.getClass().toString()
                    + e.getMessage());
            return null;
        }
    }
      public WayToPayEntity modificar(WayToPayEntity unPago) {
       try {
        em.merge(unPago);
        return unPago;
        } catch (Exception e) {
            log.error("Error al modificar:" + this.getClass().toString()
                    + e.getMessage());
            return null;
        }
    }
     public boolean eliminar(WayToPayEntity unPago) {
        try {
            WayToPayEntity aBorrar = em.find(WayToPayEntity.class,
                    unPago.getId());
            em.remove(aBorrar);
            return true;
        } catch (Exception e) {
            log.error("Error al eliminar:" + this.getClass().toString()
                    + e.getMessage());
        }
        return false;
    }

    public boolean eliminar(Long id) {
        try {
        WayToPayEntity unPago = em.find(WayToPayEntity.class, id);
        em.remove(unPago);
        return true;
         } catch (Exception e) {
            log.error("Error al eliminar:" + this.getClass().toString()
                    + e.getMessage());
        }
        return false;
    }

     public List<WayToPayEntity> listar() {
        List<WayToPayEntity> list =
                em.createQuery("select e from WayToPayEntity e").getResultList();
        return list;
    }
}
