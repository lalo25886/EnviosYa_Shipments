
package com.enviosya.shipment.service;


import com.enviosya.shipment.domain.ShipmentBean;
import com.enviosya.shipment.exception.DatoErroneoException;
import com.enviosya.shipment.exception.EntidadNoExisteException;
import com.enviosya.shipment.persistence.ShipmentEntity;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.persistence.PersistenceException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Gonzalo
 */
@Path("shipment")
public class ShipmentResource {
    @EJB
    private ShipmentBean shipmentBean;

     @Context
    private UriInfo context;

    public ShipmentResource() {
    }

    @GET
    @Path("getShipments")
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() throws Exception {
        List<ShipmentEntity> list = shipmentBean.listar();
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @POST
    @Path("add")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response agregar(String body) {
        String r;
        Gson gson = new Gson();
        String vacio = "";
        String error = gson.toJson("Error al agregar un shipment. "
                + "Verifique los datos ingresados.");

        try {

            ShipmentEntity u = gson.fromJson(body, ShipmentEntity.class);
            if (!shipmentBean
                    .existeCliente(String.valueOf(u.getIdClienteOrigen()))) {
                String sinCli = gson.toJson("Error al agregar un "
                        + "shipment. Cliente emisor no registrado");
                return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(sinCli)
                    .build();
            }
            if (!shipmentBean
                    .existeCliente(String.valueOf(u.getIdClienteDestino()))) {
                String sinCli = gson.toJson("Error al agregar un "
                        + "shipment. Cliente destino no está registrado");
                return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(sinCli)
                    .build();
            }
            if (u.getDestinoLatitud().equalsIgnoreCase(vacio)
                || u.getDestinoLongitud().equalsIgnoreCase(vacio)
                || u.getOrigenLatitud().equalsIgnoreCase(vacio)
                || u.getOrigenLongitud().equalsIgnoreCase(vacio)) {
                String sinUbic = gson.toJson("Error al agregar un shipment. "
                        + "Verifique "
                        + "los datos de las ubicaciones.");
                return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(sinUbic)
                    .build();

            }
            if (u.getImagenPaquete().equalsIgnoreCase(vacio)) {
                String sinImag = gson.toJson("Error al agregar un "
                        + "shipment. Falta el dato "
                        + "de la imagen");
                return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(sinImag)
                    .build();

            }
            ShipmentEntity creado = shipmentBean.agregar(u);
            r = shipmentBean.getCadetesCercanos(u.getOrigenLatitud(),
                                                u.getOrigenLongitud());
            if (r == null) {
                if (!shipmentBean.eliminar(creado)) {
                    String re = gson.toJson("No hay cadetes disponibles, "
                            + "ver log por más detalles. "
                            + "El envío quedó creado con el id: "
                            + "" + creado.getId());
                       return Response
                        .status(Response.Status.ACCEPTED)
                        .entity(re)
                        .build();
                } else {
                    String re = gson.toJson("No hay cadetes disponibles, "
                            + "ver log por más detalles. "
                            + "El envío no fue generado.");
                       return Response
                        .status(Response.Status.ACCEPTED)
                        .entity(re)
                        .build();

                }
            }

        } catch (DatoErroneoException | IOException  e) {
            return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(error)
                    .build();
        } catch (Exception ex) {
             return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(error)
                    .build();
        }
        return Response
                    .status(Response.Status.CREATED)
                    .entity(r)
                    .build();
    }

    @POST
    @Path("assignCadet")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response asignarCadete(InputStream data)
            throws EntidadNoExisteException {
        Response r = null;
        String linea = "";
        String[] datos = new String[2];
        int contador = 0;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(data));
            while ((linea = in.readLine())!= null){
                linea = linea.replace("\"id\": \"", "");
                linea = linea.replace("\"idCadete\": \"", "");
                linea = linea.replace("\"", "");
                linea = linea.replace("	", "");
                linea = linea.replace("{", "");
                linea = linea.replace("}", "");
                linea = linea.replace(",", "");
                linea = linea.trim();

                if (!linea.equalsIgnoreCase("")) {
                    datos[contador] = linea;
                    contador++;
                }
            }
            if (!shipmentBean.isNumeric(datos[0])
                    || !shipmentBean.isNumeric(datos[1])) {
                String error = "Error con los datos ingeresados. Deben "
                        + "ser números enteros.";
                return Response
                            .status(Response.Status.ACCEPTED)
                            .entity(error)
                            .build();
            }
            Long id = Long.valueOf(datos[0]);
            Long idCadete = Long.valueOf(datos[1]);

            Gson gson = new Gson();

            ShipmentEntity modificado =
                    shipmentBean.asignarCadete(id, idCadete);
            if (modificado == null) {
                 String error = "Error al asingar el cadete. Verifique "
                        + "los datos.";
                return Response
                            .status(Response.Status.ACCEPTED)
                            .entity(error)
                            .build();
            } else {
                return Response
                        .status(Response.Status.CREATED)
                        .entity(gson.toJson(modificado))
                        .build();
            }
            } catch (IOException
                    | NumberFormatException
                    | EntidadNoExisteException e) {
                String error = "Error al asingar el cadete. Verifique "
                        + "los datos.";
                return Response
                            .status(Response.Status.ACCEPTED)
                            .entity(error)
                            .build();
            }
    }
//Response.status(Response.Status.ACCEPTED).entity(ret).build();
    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificar(String body)  {
        Gson gson = new Gson();
        ShipmentEntity u = gson.fromJson(body, ShipmentEntity.class);
        Response r;
        ShipmentEntity modificado;
        try {
            modificado = shipmentBean.modificar(u);
            r = Response
                    .status(Response.Status.CREATED)
                    .entity(gson.toJson(modificado))
                    .build();
        } catch (EntidadNoExisteException | PersistenceException ex) {
            String res = "Error al modificar el "
                    + "shipment. Verifique los datos.";
            return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(res)
                    .build();
        }
        return r;
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eliminar(String body) {
        Gson gson = new Gson();
        ShipmentEntity u = gson.fromJson(body, ShipmentEntity.class);
        try {
            boolean eliminado = shipmentBean.eliminar(u);
            String res = "Se ha eliminado correctamente el shipment.";
            return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(gson.toJson(res))
                    .build();
        } catch (EntidadNoExisteException | PersistenceException ex) {
            String res = "[1] Error al eliminar el shipment. "
                    + "Verifique los datos.";
            return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(res)
                    .build();
        } catch (Exception ex) {
            String res = "[2] Error al eliminar el shipment. "
                    + "Verifique los datos.";
            return Response
                    .status(Response.Status.ACCEPTED)
                    .entity(res)
                    .build();
        }
    }
//  @GET
//  @Path("/envioCliente/{id}")
//  @Consumes(MediaType.TEXT_HTML)
//  public String  getClienteEnvios(@PathParam("id") String id) {
//      String retorno = "";
//      ClienteEntity cliente = new ClienteEntity();
//      cliente.setId(Long.parseLong(id));
//      Gson gson = new Gson();
//      retorno = gson.toJson(envioBean.listarClienteEnvios(cliente.getId()));
//      return retorno;
//  }

    @POST
    @Path("confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirmarRecepcion(String id) {
        Response r = null;
        id = id.replace("\"id\": \"", "");
        id = id.replace("\"", "");
        id = id.replace("	", "");
        id = id.replace("{", "");
        id = id.replace("}", "");
        id = id.replace(",", "");
        id = id.trim();

        try {
            if (!shipmentBean.isNumeric(id)) {
                String error = "Error al confirmar, el dato "
                        + "ingresado debe ser un valor numérico.";
                return Response
                        .status(Response.Status.ACCEPTED)
                        .entity(error)
                        .build();
            }
            boolean confirmado =
                    shipmentBean.confirmarRecepcion(Long.valueOf(id));
            if (!confirmado) {
                String ret = "[1] Error al confirmar la recepción del envío.";
                r = Response
                        .status(Response.Status.ACCEPTED)
                        .entity(ret)
                        .build();
            } else {
                r = Response
                        .status(Response.Status.CREATED)
                        .entity("Se ha confirmado exitosamente el envío.")
                        .build();
            }
            } catch (DatoErroneoException e) {
                String ret = "[2] Error al confirmar la recepción del envío.";
                r = Response
                        .status(Response.Status.ACCEPTED)
                        .entity(ret)
                        .build();
            }
        return r;
    }

    @POST
    @Path("isclient")
    @Consumes(MediaType.APPLICATION_JSON)
    public String esCliente(InputStream input) {
        boolean retorno = false;
        String linea = "";
        String vacio = "";
        int contador = 0;
        String[] datos = new String[2];
        try {
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(input));
            while ((linea = in.readLine()) != null) {
                linea = linea.replace("\"id\": \"", "");
                linea = linea.replace("\"idCliente\": \"", "");
                linea = linea.replace("\"", "");
                linea = linea.replace("	", "");
                linea = linea.replace("{", "");
                linea = linea.replace("}", "");
                linea = linea.replace("\\n", "");
                linea = linea.replace(",", "");
                linea = linea.trim();
                if (!linea.equalsIgnoreCase(vacio)) {
                    datos[contador] = linea;
                    contador++;
                }
            }
            if (!shipmentBean.isNumeric(datos[0])
                    || !shipmentBean.isNumeric(datos[1])) {
                return "0";
            }
            Long id = Long.valueOf(datos[0]);
            Long idCliente = Long.valueOf(datos[1]);
            retorno = shipmentBean.esCliente(id, idCliente);

            } catch (PersistenceException e) {
                System.out.println("[1] ERROR en esCliente "
                        + "(Resource) :" + e.getMessage());
                return "0";
            } catch (EntidadNoExisteException ex) {
                System.out.println("[2] ERROR en esCliente "
                        + "(Resource) :" + ex.getMessage());
                return "0";
            } catch (IOException ex) {
                System.out.println("[3] ERROR en esCliente "
                        + "(Resource) :" + ex.getMessage());
                return "0";
        }
        if(retorno) { return "1";} else { return "0";}
    }

    @GET
    @Path("getShipment/{id}")
    @Consumes(MediaType.TEXT_HTML)
    public ShipmentEntity getCadeteNotificar(@PathParam("id") String id) {
        ShipmentEntity shipment = null;
        String error = "-5";
        ShipmentEntity unS = new ShipmentEntity();
        unS.setId(Long.parseLong(id));
        if (!shipmentBean.isNumeric(id)) {
                return shipment;
            }
        String cadete = "";
        try {
            shipment = shipmentBean.obtenerShipment(unS.getId());
            Gson gson = new Gson();
            cadete = gson.toJson(shipmentBean
                    .getCadeteNotificarEntidad(shipment.getIdCadete()));

            if (cadete.equalsIgnoreCase(error)) {
                return null;
            }
            return shipment;
        } catch (EntidadNoExisteException e) {
                return null;
        } catch (DatoErroneoException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
}

