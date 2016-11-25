package com.enviosya.shipment.domain;

import com.enviosya.shipment.mail.MailBean;
import com.enviosya.shipment.persistence.ShipmentEntity;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
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
public class ShipmentBean {


     @EJB
    MailBean mailBean;
    
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

    public ShipmentEntity asignarCadete(Long id, Long idCad) {
          
        try {
            ShipmentEntity amodificar = em.find(ShipmentEntity.class,id);
            amodificar.setIdCadete(idCad);
            em.merge(amodificar);
            enviarCreacionEnvio(amodificar);
            return amodificar;
        } catch (Exception e) {
            log.error("Error en modificar Shipment Entity: " + e.getMessage());
        }
        return null;
    }
    
    private void enviarCreacionEnvio(ShipmentEntity unEnvio) {
        
        try (
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession()) {

            MessageProducer productorDeMensajeCadete =
                    session.createProducer(queueCadete);

            MessageProducer productorDeMensajeEmisor =
                    session.createProducer(queueEmisor);

            MessageProducer productorDeMensajeReceptor =
                    session.createProducer(queueReceptor);
            String cadeteNotif = getCadeteNotificar(unEnvio.getId());
            Message mensaje =
            
            session.createTextMessage(cadeteNotif+ " - Nuevo envío - "
                    + "Estimado cadete "
                    +unEnvio.getIdCadete().toString() 
                    + ", usted tiene el "
                    + "envio número : "+unEnvio.getId()+ " pendiente.");
            productorDeMensajeCadete.send(mensaje);            
            
            String mensajeNuevo = cadeteNotif+ " - "
                    + "Nuevo envío - Estimado cadete "
                    + unEnvio.getIdCadete().toString() 
                    + ", usted tiene el "
                    + "envio número : "+unEnvio.getId()+ " pendiente.";
            mailBean.enviarMail(mensajeNuevo);
            log.info("Envio realizado:" + unEnvio.toString());
           
        } catch (JMSException ex) {
            log.error("ERROR:"  + ex.getMessage());
        }
    }

    public boolean eliminar(ShipmentEntity unShipmentEntity) {
       try {
        ShipmentEntity aBorrar = em.find(ShipmentEntity.class, 
                                         unShipmentEntity.getId());
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
    //Metodo que devuelve los 4 cadetes mas proximos a la ubicación del 
    // origen
    public String getCadetesCercanos(String latitud, String longitud) {
        String r="";
	try {

            URL url = new URL("http://localhost:8080/Cadets-war/cadet/getCadets");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String output = "";
            int contador = 1;
            while ((output = br.readLine()) != null && contador <= 4) {
                    r = output + "\n"; 
                    contador++;
            }
            conn.disconnect();
      } catch (MalformedURLException e) {
            e.printStackTrace();
      } catch (IOException e) {
            e.printStackTrace();
      }
      return r;
}
    public String getCadeteNotificar(Long idCadete) {
        String r="";
	try {
            String link = "http://localhost:8080/Cadets-war/cadet/getCadet/"+idCadete.toString();
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String output = "";
            while ((output = br.readLine()) != null) {
                    r = output; 
            }
            conn.disconnect();

      } catch (MalformedURLException e) {

            e.printStackTrace();

      } catch (IOException e) {

            e.printStackTrace();

      }
      return r;
    }    
}
