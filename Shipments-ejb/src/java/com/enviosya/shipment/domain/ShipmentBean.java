package com.enviosya.shipment.domain;

import com.enviosya.shipment.mail.MailBean;
import com.enviosya.shipment.persistence.ShipmentEntity;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

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
            ShipmentEntity amodificar =
                    em.find(ShipmentEntity.class, id);
            amodificar.setIdCadete(idCad);
            em.merge(amodificar);
            enviarCreacionEnvio(amodificar);
            return amodificar;
        } catch (Exception e) {
            log.error("Error en modificar Shipment Entity: " + e.getMessage());
        }
        return null;
    }

    private void enviarCreacionEnvio(ShipmentEntity unEnvio)
            throws UnirestException {

        try (
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession()) {
            //Acá consulto el servicio provisto por
            //Mathias para luego calcular
            //el costo del envío
           // String dimension = getDimensionesImagen("");
            double dato1 = 2;
            double dato2 = 2;
            double dato3 = 2;
            double costo = calcularCosto(dato1, dato2, dato3);
            String dirOrigen = 
                    convertirUbicacion(unEnvio.getOrigenLatitud(),
                                       unEnvio.getOrigenLongitud(),
                                       0);
            String dirDestino = 
                    convertirUbicacion(unEnvio.getDestinoLatitud(),
                                       unEnvio.getDestinoLongitud(),
                                       1);


            MessageProducer productorDeMensajeCadete =
                    session.createProducer(queueCadete);

            MessageProducer productorDeMensajeEmisor =
                    session.createProducer(queueEmisor);

            MessageProducer productorDeMensajeReceptor =
                    session.createProducer(queueReceptor);
            String cadeteNotif = getCadeteNotificar(unEnvio.getIdCadete());
            Message mensaje =
            session.createTextMessage(cadeteNotif + " - Nuevo envío - "
                    + "Estimado cadete "
                    + unEnvio.getIdCadete().toString()
                    + ", usted tiene el "
                    + "envio número : "
                    + unEnvio.getId() + " pendiente. "
                    + "El costo del envío es: $"
                    + String.valueOf(costo) + ". "
                    + "La dirección de origen es: " + dirOrigen
                    + " y la dirección de destino es: " + dirDestino + ".");
            productorDeMensajeCadete.send(mensaje);

            String mensajeNuevo = cadeteNotif + " - Nuevo envío - "
                    + "Estimado cadete "
                    + unEnvio.getIdCadete().toString()
                    + ", usted tiene el "
                    + "envio número : "
                    + unEnvio.getId() + " pendiente. "
                    + "El costo del envío es: $"
                    + String.valueOf(costo) + ". "
                    + "La dirección de origen es: " + dirOrigen
                    + " y la dirección de destino es: " + dirDestino + "."
                    + "";

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
        List<ShipmentEntity> list = null;
        try {
            list = em.createQuery("select e "
                    + "from ShipmentEntity e "
                    + "where e.descripcion = :desc")
                    .setParameter("desc", descripcion).getResultList();
        }catch(Exception e){
            log.error("Error en buscar(String descripcion): " + e.getMessage());
        }    
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
            log.error("Error en getCadetesCercanos[1]: " + e.getMessage()); 
      } catch (IOException e) {
            log.error("Error en getCadetesCercanos[2]: " + e.getMessage());
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
            log.error("Error en getCadeteNotificar[1]: " + e.getMessage());
      } catch (IOException e) {
            log.error("Error en getCadeteNotificar[2]: " + e.getMessage());
      }
      return r;
    }
    
    public double calcularCosto(double dato1,double dato2, double dato3){
        double retorno = 0;
        try {
            retorno = dato1 * 3;
            retorno += (retorno +dato2) * 4;
            retorno += (retorno +dato3) * 2;
        } catch (Exception e) {
            log.error("Error al calcular el "
                    + "costo del Shipment: " + e.getMessage());
        }
        return retorno;
    }

    public String getDimensionesImagen(String dato) throws UnirestException {
        String r="";
	try {
            System.out.println("PPPPPPPPPPPPPPPPPPPPPPPP"
                    + "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPP"
                    + "PPPPPPPPPPPPPPPPP: ");
            HttpResponse<JsonNode> response =
            Unirest.post("https://mathifonseca-ort-arqsoft-sizer-v1.p.mashape.com/dimensions")
            .header("X-Mashape-Key",
                    "eTrCJvP4D6mshPu4UGwWBw8p5mdwp16MJUIjsn60S9YjmloF4h")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            //.body("{\"image\":\"data:image/"+dato+"\"}")
            .body("{\"image\":\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAMAAADXqc3KAAAB+FBMVEUAAAA/mUPidDHiLi5Cn0XkNTPmeUrkdUg/m0Q0pEfcpSbwaVdKskg+lUP4zA/iLi3msSHkOjVAmETdJSjtYFE/lkPnRj3sWUs8kkLeqCVIq0fxvhXqUkbVmSjwa1n1yBLepyX1xxP0xRXqUkboST9KukpHpUbuvRrzrhF/ljbwaljuZFM4jELaoSdLtElJrUj1xxP6zwzfqSU4i0HYnydMtUlIqUfywxb60AxZqEXaoifgMCXptR9MtklHpEY2iUHWnSjvvRr70QujkC+pUC/90glMuEnlOjVMt0j70QriLS1LtEnnRj3qUUXfIidOjsxAhcZFo0bjNDH0xxNLr0dIrUdmntVTkMoyfL8jcLBRuErhJyrgKyb4zA/5zg3tYFBBmUTmQTnhMinruBzvvhnxwxZ/st+Ktt5zp9hqota2vtK6y9FemNBblc9HiMiTtMbFtsM6gcPV2r6dwroseLrMrbQrdLGdyKoobKbo3Zh+ynrgVllZulTsXE3rV0pIqUf42UVUo0JyjEHoS0HmsiHRGR/lmRz/1hjqnxjvpRWfwtOhusaz0LRGf7FEfbDVmqHXlJeW0pbXq5bec3fX0nTnzmuJuWvhoFFhm0FtrziBsjaAaDCYWC+uSi6jQS3FsSfLJiTirCOkuCG1KiG+wSC+GBvgyhTszQ64Z77KAAAARXRSTlMAIQRDLyUgCwsE6ebm5ubg2dLR0byXl4FDQzU1NDEuLSUgC+vr6urq6ubb29vb2tra2tG8vLu7u7uXl5eXgYGBgYGBLiUALabIAAABsElEQVQoz12S9VPjQBxHt8VaOA6HE+AOzv1wd7pJk5I2adpCC7RUcHd3d3fXf5PvLkxheD++z+yb7GSRlwD/+Hj/APQCZWxM5M+goF+RMbHK594v+tPoiN1uHxkt+xzt9+R9wnRTZZQpXQ0T5uP1IQxToyOAZiQu5HEpjeA4SWIoksRxNiGC1tRZJ4LNxgHgnU5nJZBDvuDdl8lzQRBsQ+s9PZt7s7Pz8wsL39/DkIfZ4xlB2Gqsq62ta9oxVlVrNZpihFRpGO9fzQw1ms0NDWZz07iGkJmIFH8xxkc3a/WWlubmFkv9AB2SEpDvKxbjidN2faseaNV3zoHXvv7wMODJdkOHAegweAfFPx4G67KluxzottCU9n8CUqXzcIQdXOytAHqXxomvykhEKN9EFutG22p//0rbNvHVxiJywa8yS2KDfV1dfbu31H8jF1RHiTKtWYeHxUvq3bn0pyjCRaiRU6aDO+gb3aEfEeVNsDgm8zzLy9egPa7Qt8TSJdwhjplk06HH43ZNJ3s91KKCHQ5x4sw1fRGYDZ0n1L4FKb9/BP5JLYxToheoFCVxz57PPS8UhhEpLBVeAAAAAElFTkSuQmCC\"}")
            .asJson();
            r = response.toString();
            System.out.println("RESPUESTA DE DIMENSION: "
                    + response.toString());
        } catch (UnirestException e) {
            log.error("Error en getDimensionesImagen[1]: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error en getDimensionesImagen[2]: " + e.getMessage());
      }
      return r;
    }
    //El tipo es 0 latitud y 1 longitud
    //La idea es generar un methodo que conusma algun
    //servivio que provea dicha conversion
    public String convertirUbicacion(String latitud,String longitud, int tipo){
        String dir = "";
        if (tipo == 0) {
            dir = "Pablo de María N°1111";
        } else {
            dir = "Av. 18 de Julio N°2222";
        }
        return dir;
    }

    public boolean confirmarRecepcion(Long id) {
        int valor = 1;
        boolean  retorno = false;
        try {
            ShipmentEntity amodificar =
                    em.find(ShipmentEntity.class, id);
            amodificar.setConfirmado(valor);
            em.merge(amodificar);
            if (amodificar.getConfirmado() == valor) {
                retorno = true;
                String cadeteNotif =
                        getCadeteNotificar(amodificar.getIdCadete());
                String remitenteNotif =
                        getClienteNotificar(amodificar.getIdClienteOrigen());
                String destinatarioNotif =
                        getClienteNotificar(amodificar.getIdClienteDestino());
                String mensajeEmisor = remitenteNotif
                    + " - Confirmación de envío - "
                    + "Estimado cliente "
                    + amodificar.getIdClienteOrigen().toString()
                    + ", tenemos el agrado de informarle que el envío número "
                    + amodificar.getId() + " fue recepcionado exitosamente. "
                    + "El cadete que realizó el envío es el número "
                    + amodificar.getIdCadete() + " y su correo electrónico es "
                    + cadeteNotif + ". "
                    + "Acceder al link para calificar el servicio y al cadete "
                    + "<u>https://www.google.com.uy</b>"
                    + "<b>EnviosYa! le agradece por su preferencia.</b>";

                mailBean.enviarMail(mensajeEmisor);
                log.info("Confirmación de envío (origen):" + remitenteNotif);

                String mensajeDestinatario = destinatarioNotif
                    + " - Confirmación de envío - "
                    + "Estimado cliente "
                    + amodificar.getIdClienteDestino().toString()
                    + ", tenemos el agrado de informarle que el envío número "
                    + amodificar.getId() + " fue recepcionado exitosamente. "
                    + "El cadete que realizó el envío es el número "
                    + amodificar.getIdCadete() + " y su correo electrónico es "
                    + cadeteNotif + ". "
                    + "Acceder al link para calificar el servicio y al cadete "
                    + "<u>https://www.google.com.uy</b>"
                    + "<b>EnviosYa! le agradece por su preferencia.</b>";

                mailBean.enviarMail(mensajeDestinatario);
                log.info("Confirmación de envío (destino):" + remitenteNotif);
            }
        } catch (Exception e) {
            log.error("Error en confirmarRecepcion: " + e.getMessage());
        }
        return retorno;
    }
    public String getClienteNotificar(Long idCliente) {
        String r = "";
	try {
            String link = "http://localhost:8080/Clients-war/client/"
                    + "getClient/" + idCliente.toString();
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
            log.error("Error en getCadeteNotificar[1]: " + e.getMessage());
      } catch (IOException e) {
            log.error("Error en getCadeteNotificar[2]: " + e.getMessage());
      }
      return r;
    }
}
