package com.enviosya.shipment.domain;

import com.enviosya.shipment.exception.DatoErroneoException;
import com.enviosya.shipment.exception.EntidadNoExisteException;
import com.enviosya.shipment.mail.MailBean;
import com.enviosya.shipment.tool.CalculateCostBean;
import com.enviosya.shipment.persistence.ShipmentEntity;
import com.enviosya.shipment.tool.CalculateCostBean;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
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
import javax.persistence.PersistenceException;
import javax.xml.crypto.URIReferenceException;
import org.apache.log4j.Logger;



/**
 *
 * @author Gonzalo
 */
@Stateless
public class ShipmentBean {


    @EJB
    MailBean mailBean;
    @EJB
    CalculateCostBean calculate;

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

    public ShipmentEntity agregar(ShipmentEntity unShipmentEntity)
            throws DatoErroneoException {
        try {
            em.persist(unShipmentEntity);
            return unShipmentEntity;
        } catch (EJBException | PersistenceException  e) {
            log.error("Error en eliminar Shipment Entity: " + e.getMessage());
            throw new DatoErroneoException("Error al agregar un shipment. "
                    + "Verifique los datos ingresados.");
        }
    }

    public ShipmentEntity agregar(String body) throws DatoErroneoException {
       try {
            Gson gson = new Gson();
            ShipmentEntity unEnvio = gson.fromJson(body, ShipmentEntity.class);
            em.persist(unEnvio);
            return unEnvio;
        } catch (EJBException | PersistenceException  e) {
            log.error("Error en agregrar Shipment Entity: " + e.getMessage());
            throw new DatoErroneoException("Error al agregar un shipment. "
                    + "Verifique los datos ingresados.");
        }
    }

    public ShipmentEntity modificar(ShipmentEntity unEnvioEntity)
            throws EntidadNoExisteException {
        try {
            em.merge(unEnvioEntity);
            return unEnvioEntity;
        } catch (Exception e) {
            log.error("Error en modificar Shipment Entity: " + e.getMessage());
            throw new EntidadNoExisteException("Error al modificar un shipment."
                    + " El shipment con el id: " + unEnvioEntity.getId() + " "
                    + "no se encuentra.");
        }
    }

    public ShipmentEntity asignarCadete(Long id, Long idCad)
            throws EntidadNoExisteException {
        try {
            ShipmentEntity amodificar = em.find(ShipmentEntity.class, id);
            amodificar.setIdCadete(idCad);
            em.merge(amodificar);
            //Gson gson = new Gson();
            //gson.toJson(amodificar);
            enviarCreacionEnvio(amodificar);
            return amodificar;
        } catch (Exception e) {
            log.error("Error al asginar un cadete: " + e.getMessage());
            throw new EntidadNoExisteException("Error al asignar un cadete."
                    + " El shipment con el id: " + id + " "
                    + "no se encuentra.");
        }
    }

    private void enviarCreacionEnvio(ShipmentEntity unEnvio)
            throws Exception, JMSException {

        try (
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession()) {
            String dimension = getDimensionesImagen(unEnvio.getImagenPaquete());
            if (dimension.equalsIgnoreCase("-1")){
                throw new Exception("Error al obtener las dimensiones de "
                        + "la imagen");
            } else {
                double dato1 = 2;
                double dato2 = 2;
                double dato3 = 2;

                double costo = calculate.calcularCosto(dato1, dato2, dato3);

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
    //            MessageProducer productorDeMensajeEmisor =
    //                    session.createProducer(queueEmisor);

    //            MessageProducer productorDeMensajeReceptor =
    //                    session.createProducer(queueReceptor);
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
            }
            
            
        } catch (JMSException ex) {
            log.error("ERROR: Ha ocurrido un error al enviar "
                    + "un mensaje en la creación de "
                    + "un envío"  + ex.getMessage());
            throw new JMSException("Error en enviarCreacionEnvio. "
                    + ex.getMessage());
        }  catch (Exception e) {
            log.error("ERROR: Ha ocurrido un error al enviar "
                    + "un mensaje en la creación de "
                    + "un envío"  + e.getMessage());
            throw new Exception("Error en enviarCreacionEnvio. "
                    + e.getMessage());
        }
    }

    public boolean eliminar(ShipmentEntity unShipmentEntity)  throws Exception {
       try {
        ShipmentEntity aBorrar = em.find(ShipmentEntity.class,
                                         unShipmentEntity.getId());
        em.remove(aBorrar);
        return true;
        } catch (Exception e) {
             log.error("Error en eliminar Shipment Entity: " + e.getMessage());
              throw new Exception("Error en eliminar. " + e.getMessage());
        }
    }

    public List<ShipmentEntity> listar() throws Exception {
        try {
            List<ShipmentEntity> list =
                    em.createQuery("select e from ShipmentEntity e")
                            .getResultList();
            return list;
        } catch (Exception e) {
             log.error("Error en listar Shipment Entity: " + e.getMessage());
              throw new Exception("Error en listar. " + e.getMessage());
        }
    }

    public Shipment buscar(Long id) throws EntidadNoExisteException { 
        try {
            ShipmentEntity ent = em.find(ShipmentEntity.class, id);
            Shipment e = new Shipment();
            e.setId(ent.getId());
            e.setDescripcion(ent.getDescripcion());
            return e;
        } catch (Exception e) {
            log.error("Error en buscar(Long id): " + e.getMessage());
            throw new EntidadNoExisteException("Error al buscar un shipment. "
                    + "El shipment con el id: " + id + " no "
                    + "se encuentra.");
        }
    }

    public List<ShipmentEntity> buscar(String descripcion)
            throws EntidadNoExisteException {
        List<ShipmentEntity> list = null;
        try {
            list = em.createQuery("select e "
                    + "from ShipmentEntity e "
                    + "where e.descripcion = :desc")
                    .setParameter("desc", descripcion).getResultList();
            return list;
        } catch (Exception e) {
            log.error("Error en buscar(String descripcion): " + e.getMessage());
            throw new EntidadNoExisteException("Error al buscar un shipment. "
                    + "El shipment con la descripción: " + descripcion + " no "
                    + "se encuentra.");
        }
    }
    //Metodo que devuelve los 4 cadetes mas proximos a la ubicación del
    // origen
    public String getCadetesCercanos(String latitud, String longitud)
            throws DatoErroneoException, MalformedURLException, IOException {
        String r = "";
        String link = "";
	try {
            link = "http://localhost:8080/Cadets-war/cadet/getCadets";
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
            int contador = 1;
            while ((output = br.readLine()) != null && contador <= 4) {
                    r = output + "\n";
                    contador++;
            }
            conn.disconnect();
            return r;
      } catch (MalformedURLException e) {
            log.error("Error en getCadetesCercanos[1]: " + e.getMessage());
            throw new MalformedURLException("Error en la URL: " + link);
      } catch (IOException e) {
            log.error("Error en getCadetesCercanos[2]: " + e.getMessage());
            throw new IOException("Error en getCadetesCercanos.");
      } catch (Exception e) {
            log.error("Error en getCadetesCercanos[3]: " + e.getMessage());
            throw new DatoErroneoException("Error en getCadetesCercanos[3]. "
                    + e.getMessage());
      }
}
    public String getCadeteNotificar(Long idCadete)
            throws DatoErroneoException, MalformedURLException, IOException {
        String r = "";
        String link = "";
	try {
            link = "http://localhost:8080/Cadets-war/"
                    + "cadet/getCadet/" + idCadete.toString();
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
            return r;
        } catch (MalformedURLException e) {
               log.error("Error en getCadeteNotificar[1]: " + e.getMessage());
               throw new MalformedURLException("Error en la URL: " + link);
         } catch (IOException e) {
               log.error("Error en getCadeteNotificar[2]: " + e.getMessage());
               throw new IOException("Error en getCadeteNotificar.");
         } catch (Exception e) {
               log.error("Error en getCadeteNotificar[3]: " + e.getMessage());
               throw new DatoErroneoException("Error en getCadeteNotificar[3]."
                       + " " + e.getMessage());
         }
    }

    public String getDimensionesImagen(String dato) throws UnirestException,
             Exception {
        String r = "";
        String link = "";
	try {
            link = "https://mathifonseca-ort-arqsoft-sizer-v1."
                    + "p.mashape.com/dimensions";
            HttpResponse<JsonNode> response =  Unirest.post(link)
            .header("X-Mashape-Key",
                    "eTrCJvP4D6mshPu4UGwWBw8p5mdwp16MJUIjsn60S9YjmloF4h")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body("{\"image\":\"data:image/png;base64,iVBORw0KGgoA"
                        + "AAANSUhEUgAAABgAAAAYCAMAAADXqc3KAAAB+FBMVEUAAAA/mUP"
                        + "idDHiLi5Cn0XkNTPmeUrkdUg/m0Q0pEfcpSbwaVdKskg+lUP4"
                        + "zA/iLi3msSHkOjVAmETdJSjtYFE/lkPnRj3sWUs8kkLeqCVIq"
                        + "0fxvhXqUkbVmSjwa1n1yBLepyX1xxP0xRXqUkboST9KukpHpU"
                        + "buvRrzrhF/ljbwaljuZFM4jELaoSdLtElJrUj1xxP6zwzfqSU"
                        + "4i0HYnydMtUlIqUfywxb60AxZqEXaoifgMCXptR9MtklHpEY2"
                        + "iUHWnSjvvRr70QujkC+pUC/90glMuEnlOjVMt0j70QriLS1Lt"
                        + "EnnRj3qUUXfIidOjsxAhcZFo0bjNDH0xxNLr0dIrUdmntVTk"
                        + "MoyfL8jcLBRuErhJyrgKyb4zA/5zg3tYFBBmUTmQTnhMinruB"
                        + "zvvhnxwxZ/st+Ktt5zp9hqota2vtK6y9FemNBblc9HiMiTtMb"
                        + "FtsM6gcPV2r6dwroseLrMrbQrdLGdyKoobKbo3Zh+ynrgVllZ"
                        + "ulTsXE3rV0pIqUf42UVUo0JyjEHoS0HmsiHRGR/lmRz/1hjqnx"
                        + "jvpRWfwtOhusaz0LRGf7FEfbDVmqHXlJeW0pbXq5bec3fX0nTn"
                        + "zmuJuWvhoFFhm0FtrziBsjaAaDCYWC+uSi6jQS3FsSfLJiTi"
                        + "rCOkuCG1KiG+wSC+GBvgyhTszQ64Z77KAAAARXRSTlMAIQRD"
                        + "LyUgCwsE6ebm5ubg2dLR0byXl4FDQzU1NDEuLSUgC+vr6urq6"
                        + "ubb29vb2tra2tG8vLu7u7uXl5eXgYGBgYGBLiUALabIAAABsE"
                        + "lEQVQoz12S9VPjQBxHt8VaOA6HE+AOzv1wd7pJk5I2adpCC7R"
                        + "UcHd3d3fXf5PvLkxheD++z+yb7GSRlwD/+Hj/APQCZWxM5M+g"
                        + "oF+RMbHK594v+tPoiN1uHxkt+xzt9+R9wnRTZZQpXQ0T5uP1I"
                        + "QxToyOAZiQu5HEpjeA4SWIoksRxNiGC1tRZJ4LNxgHgnU5nJZ"
                        + "BDvuDdl8lzQRBsQ+s9PZt7s7Pz8wsL39/DkIfZ4xlB2Gqsq62"
                        + "ta9oxVlVrNZpihFRpGO9fzQw1ms0NDWZz07iGkJmIFH8xxkc3a"
                        + "/WWlubmFkv9AB2SEpDvKxbjidN2faseaNV3zoHXvv7wMODJdkO"
                        + "HAegweAfFPx4G67KluxzottCU9n8CUqXzcIQdXOytAHqXxomvy"
                        + "khEKN9EFutG22p//0rbNvHVxiJywa8yS2KDfV1dfbu31H8jF1"
                        + "RHiTKtWYeHxUvq3bn0pyjCRaiRU6aDO+gb3aEfEeVNsDgm8zz"
                        + "Ly9egPa7Qt8TSJdwhjplk06HH43ZNJ3s91KKCHQ5x4sw1fRGY"
                        + "DZ0n1L4FKb9/BP5JLYxToheoFCVxz57PPS8UhhEpLBVeAAAAA"
                        + "ElFTkSuQmCC\"}")
            .asJson();
            String length = "";
            String height = "";
            String weight = "";
            if (response.getBody().getObject() != null) {
                length = response.getBody().getObject()
                        .get("length").toString();
                height = response.getBody().getObject()
                        .get("height").toString();
                weight = response.getBody().getObject()
                        .get("weight").toString();
                r = length + "*" + height + "*" + weight;
                return r;
            }else {
                return "-1";
            } 
        } catch (UnirestException e) {
               log.error("Error en getDimensionesImagen[1]: " + e.getMessage());
               throw new UnirestException("Error en getDimensionesImagen.");
        } catch (Exception e) {
               log.error("Error en getDimensionesImagen[2]: " + e.getMessage());
               throw new Exception("Error en getDimensionesImagen.");
         }
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

    public boolean confirmarRecepcion(Long id)
            throws DatoErroneoException {
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
                    + amodificar.getId().toString()
                    + " fue recepcionado exitosamente. "
                    + "El cadete que realizó el envío es el número "
                    + amodificar.getIdCadete() + " y su correo electrónico es "
                    + cadeteNotif + ". "
                    + "Acceder al link para calificar el servicio y al cadete "
                    + "https://www.google.com.uy "
                    + "EnviosYa! le agradece por su preferencia.";
                mailBean.enviarMail(mensajeEmisor);
                log.info("Confirmación de envío (origen):" + remitenteNotif);

                String mensajeDestinatario = destinatarioNotif
                    + " - Confirmación de envío - "
                    + "Estimado cliente "
                    + amodificar.getIdClienteDestino().toString()
                    + ", tenemos el agrado de informarle que el envío número "
                    + amodificar.getId().toString()
                    + " fue recepcionado exitosamente. "
                    + "El cadete que realizó el envío es el número "
                    + amodificar.getIdCadete() + " y su correo electrónico es "
                    + cadeteNotif + ". "
                    + "Acceder al link para calificar el servicio y al cadete "
                    + "https://www.google.com.uy "
                    + "EnviosYa! le agradece por su preferencia.";

                mailBean.enviarMail(mensajeDestinatario);
                log.info("Confirmación de envío (destino):" + remitenteNotif);
            }
        } catch (Exception e) {
             log.error("Error en confirmarRecepcion: " + e.getMessage());
            throw new DatoErroneoException("Error en confirmarRecepcion."
                    + "" + e.getMessage());
        }
        return retorno;
    }

    public String getClienteNotificar(Long idCliente)
            throws MalformedURLException, DatoErroneoException, IOException {
        String r = "";
	try {
            String link = "http://localhost:8080/Clients-war/"
                    + "client/getClient/" + idCliente.toString();
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
            log.error("Error en getClienteNotificar[1]: " + e.getMessage());
            throw new MalformedURLException("Error en getClienteNotificar[1]."
                    + "" + e.getMessage());
      } catch (IOException e) {
             log.error("Error en getClienteNotificar[2]: " + e.getMessage());
            throw new IOException("Error en getClienteNotificar[2]."
                    + "" + e.getMessage());
      } catch (Exception e) {
             log.error("Error en getClienteNotificar[3]: " + e.getMessage());
            throw new DatoErroneoException("Error en getClienteNotificar[3]."
                    + "" + e.getMessage());
      }
      return r;
    }

    public boolean esCliente(Long id, Long idCli)
            throws EntidadNoExisteException {
        boolean retorno = false;
        try {
            ShipmentEntity envio = em.find(ShipmentEntity.class, id);
            if (Objects.equals(envio.getIdClienteDestino(), idCli)
                    || Objects.equals(envio.getIdClienteOrigen(), idCli)) {
                retorno = true;
            }
        } catch (Exception e) {
             log.error("Error en esCliente[1]: " + e.getMessage());
            throw new EntidadNoExisteException("Error en esCliente[1]."
                    + "" + e.getMessage());
        }
        return retorno;
    }

    public ShipmentEntity obtenerShipment(Long id) throws EntidadNoExisteException {
        ShipmentEntity unS = null;
        try {
            unS = em.find(ShipmentEntity.class, id);
            String cadete = getCadeteNotificarEntidad(unS.getIdCadete());
            
        } catch (Exception e) {
            log.error("Error al obtenerShipment: " + e.getMessage());
            throw new EntidadNoExisteException("Error en obtenerShipment. "
                    + "El shipment con el id: " + id + " no "
                    + "se encuentra.");
        }
        return unS;
    }

     public String getCadeteNotificarEntidad(Long idCadete)
            throws DatoErroneoException, MalformedURLException, IOException {
        String r = "";
        String link = "";
	try {
            link = "http://localhost:8080/Cadets-war/"
                    + "cadet/getCadetEntity/" + idCadete.toString();
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
                    r += output;
            }
            System.out.println("DATOS DEL CADETE : " + r);
            conn.disconnect();
            return r;
        } catch (MalformedURLException e) {
               log.error("Error en getCadeteNotificarEntidad[1]:"
                       + " " + e.getMessage());
               throw new MalformedURLException("Error en la URL: " + link);
         } catch (IOException e) {
               log.error("Error en getCadeteNotificarEntidad[2]:"
                       + " " + e.getMessage());
               throw new IOException("Error en getCadeteNotificarEntidad.");
         } catch (Exception e) {
               log.error("Error en getCadeteNotificarEntidad[3]: "
                       + "" + e.getMessage());
               throw new DatoErroneoException("Error en "
                       + "getCadeteNotificarEntidad[3]."
                       + " " + e.getMessage());
         }
    }

     public String getReviewCadete(Long idEnvio, Long idCadete)
            throws DatoErroneoException, MalformedURLException, IOException {
        String r = "";
        String link = "";
	try {

            link = "http://localhost:8080/Reviews-war/"
                    + "review/getListReviews/" + idCadete.toString();
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
                    r += output;
            }
            System.out.println("LISTA DE REVIEW : " + r);
            conn.disconnect();
            return r;
        } catch (MalformedURLException e) {
               log.error("Error en getCadeteNotificarEntidad[1]:"
                       + " " + e.getMessage());
               throw new MalformedURLException("Error en la URL: " + link);
         } catch (IOException e) {
               log.error("Error en getCadeteNotificarEntidad[2]:"
                       + " " + e.getMessage());
               throw new IOException("Error en getCadeteNotificarEntidad.");
         } catch (Exception e) {
               log.error("Error en getCadeteNotificarEntidad[3]: "
                       + "" + e.getMessage());
               throw new DatoErroneoException("Error en "
                       + "getCadeteNotificarEntidad[3]."
                       + " " + e.getMessage());
         }
    }
//Response.status(Response.Status.ACCEPTED).entity(ret).build();
}
