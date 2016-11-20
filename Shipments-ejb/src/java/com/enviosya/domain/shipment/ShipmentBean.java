package com.enviosya.domain.shipment;

import com.enviosya.persistence.shipment.ShipmentEntity;
import com.google.gson.Gson;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.log4j.Logger;

/**
 *
 * @author Gonzalo
 */
@Stateless
@LocalBean
public class ShipmentBean {
 static Logger log = Logger.getLogger("FILE");
    @Resource(lookup = "jms/ConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(lookup = "jms/QueueCadete")
    private Queue queueCadete;
    @Resource(lookup = "jms/QueueEmisor")
    private Queue queueEmisor;
    @Resource(lookup = "jms/QueueReceptor")
    private Queue queueReceptor;
    @PersistenceContext
    private EntityManager em;
    @PostConstruct
    private void init() {
    }

    public ShipmentEntity agregar(ShipmentEntity unShipmentEntity) {
        try {
            em.persist(unShipmentEntity);
        //    enviarCreacionEnvio(unShipmentEntity);
            return unShipmentEntity;
        } catch (Exception e) {
            log.error("Error en eliminar Shipment Entity: " + e.getMessage());
        }
        return null;
    }

    public ShipmentEntity agregar(String body) {
       try {
            Gson gson = new Gson();
            ShipmentEntity unEnvio = gson.fromJson(body, ShipmentEntity.class);
            em.persist(unEnvio);
        //    enviarCreacionEnvio(unEnvio);
            return unEnvio;
        } catch (Exception e) {
            log.error("Error en agregrar Shipment Entity: " + e.getMessage());
        }
        return null;
    }

    public ShipmentEntity modificar(ShipmentEntity unEnvioEntity) {
        try {
            em.merge(unEnvioEntity);
            return unEnvioEntity;
        } catch (Exception e) {
            log.error("Error en modificar Shipment Entity: " + e.getMessage());
        }
        return null;
    }

    public boolean eliminar(ShipmentEntity unShipmentEntity) {
       try {
        ShipmentEntity aBorrar = em.find(ShipmentEntity.class, unShipmentEntity.getId());
        em.remove(aBorrar);
        return true;
        } catch (Exception e) {
             log.error("Error en eliminar Shipment Entity: " + e.getMessage());
        }
       return false;
    }

    public List<ShipmentEntity> listar() {
        List<ShipmentEntity> list =
                em.createQuery("select e from ShipmentEntity e").getResultList();
        return list;
    }

    public Shipment buscar(Long id) {
        ShipmentEntity ent = em.find(ShipmentEntity.class, id);
        Shipment e = new Shipment();
        e.setId(ent.getId());
        e.setDescripcion(ent.getDescripcion());
        return e;
    }

    public List<ShipmentEntity> buscar(String descripcion) {
        List<ShipmentEntity> list = em.createQuery("select e "
                + "from ShipmentEntity e "
                + "where e.descripcion = :desc")
                .setParameter("desc", descripcion).getResultList();
        return list;
    }

//    public List<ShipmentEntity> listarClienteEnvios(Long idRecibido) {
//         List<ShipmentEntity> retorno = null;
//        try {
//            retorno = em.createQuery("SELECT      e.id,"
//                                        + " e.descripcion,"
//                                        + " e.emisor.ci,"
//                                        + " e.emisor.nombre,"
//                                        + " e.emisor.apellido,"
//                                        + " e.dirRetiro,"
//                                        + " e.receptor.nombre,"
//                                        + " e.receptor.apellido,"
//                                        + " e.dirRecibo,"
//                                        + " e.cadete.nombre,"
//                                        + " e.cadete.email,"
//                                        + " e.vehiculo.matricula,"
//                                        + " e.vehiculo.descripcion "
//                                + "FROM EnvioEntity e "
//                                + "WHERE e.emisor.id = :id", ShipmentEntity.class)
//                                .setParameter("id", idRecibido).getResultList();
//       } catch (Exception e) {
//            log.error("Error en consultar Envio Entity: " + e.getMessage());
//        }
//       return retorno;
//   }
//
//    private void enviarCreacionEnvio(ShipmentEntity unEnvio) {
//        try (Connection connection = connectionFactory.createConnection();
//            Session session = connection.createSession()) {
//            MessageProducer productorDeMensajeCadete =
//                    session.createProducer(queueCadete);
//            MessageProducer productorDeMensajeEmisor =
//                    session.createProducer(queueEmisor);
//            MessageProducer productorDeMensajeReceptor =
//                    session.createProducer(queueReceptor);
//
//            Message mensaje =
//                    session.createTextMessage("Cadete tiene un "
//                            + "envio pendiente:" + unEnvio.toString());
//            productorDeMensajeCadete.send(mensaje);
//            mensaje = session.createTextMessage("Estimado cliente estamos "
//                    + "realizado en envio:" + unEnvio.getId()
//                    + " sera entregado por: " + unEnvio.getCadete().toString());
//
//            productorDeMensajeEmisor.send(mensaje);
//            mensaje = session.createTextMessage("Estimado cliente "
//                    + "el envio:" + unEnvio.getId() + " sera entregado por: "
//                    + unEnvio.getCadete().toString());
//            productorDeMensajeReceptor.send(mensaje);
//
//            log.info("Envio realizado:" + unEnvio.toString());
//        } catch (JMSException ex) {
//            log.error("ERROR:"  + ex.getMessage());
//        }
//    }
//
//    public List<EnvioEntity> listarCadeteEnvios(Long idRecibido) {
//         List<EnvioEntity> retorno = null;
//    try {
//            retorno =
//                em.createQuery("SELECT      e.id,"
//                                        + " e.descripcion,"
//                                        + " e.cadete.nombre,"
//                                        + " e.cadete.email,"
//                                        + " e.emisor.ci,"
//                                        + " e.emisor.nombre,"
//                                        + " e.emisor.apellido,"
//                                        + " e.dirRetiro,"
//                                        + " e.receptor.ci,"
//                                        + " e.receptor.nombre,"
//                                        + " e.receptor.apellido,"
//                                        + " e.dirRecibo,"
//                                        + " e.vehiculo.matricula,"
//                                        + " e.vehiculo.descripcion "
//                                + "FROM EnvioEntity e "
//                                + "WHERE e.cadete.id = :id", EnvioEntity.class)
//                                .setParameter("id", idRecibido).getResultList();
//        } catch (Exception e) {
//            log.error("ERROR:"  + e.getMessage());
//        }
//       return retorno;
//   }

}
