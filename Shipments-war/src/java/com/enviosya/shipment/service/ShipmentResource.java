
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
import java.net.MalformedURLException;
import java.util.List;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
    public String agregar(String body)
            throws DatoErroneoException, MalformedURLException, IOException {
        Gson gson = new Gson();
        ShipmentEntity u = gson.fromJson(body, ShipmentEntity.class);
        String r;
        ShipmentEntity creado = shipmentBean.agregar(u);
        if (creado == null) {
            r = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Shipment")
                    .build().toString();
        } else {
            r = shipmentBean.getCadetesCercanos(u.getOrigenLatitud(),
                                                u.getOrigenLongitud());
        }
        return r;
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
            while ((linea = in.readLine())!=null){
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
            Long id = Long.valueOf(datos[0]);
            Long idCadete = Long.valueOf(datos[1]);
            Gson gson = new Gson();
            ShipmentEntity modificado =
                    shipmentBean.asignarCadete(id, idCadete);
            if (modificado == null) {
                r = Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Shipment")
                        .build();
            } else {
                r = Response
                        .status(Response.Status.CREATED)
                        .entity(gson.toJson(modificado))
                        .build();
            }
            } catch (IOException | NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        return r;
    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modificar(String body) throws EntidadNoExisteException {
        Gson gson = new Gson();
        ShipmentEntity u = gson.fromJson(body, ShipmentEntity.class);
        Response r;
        ShipmentEntity modificado = shipmentBean.modificar(u);
        if (modificado == null) {
            r = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Shipment")
                    .build();
        } else {
            r = Response
                    .status(Response.Status.CREATED)
                    .entity(gson.toJson(modificado))
                    .build();
        }
        return r;
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response eliminar(String body) throws Exception {
        Gson gson = new Gson();
        ShipmentEntity u = gson.fromJson(body, ShipmentEntity.class);
        Response r;
        Boolean modificado = shipmentBean.eliminar(u);
        if (!modificado) {
            r = Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("Shipment")
                    .build();
        } else {
            r = Response
                    .status(Response.Status.CREATED)
                    .entity(gson.toJson(modificado))
                    .build();
        }
        return r;
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
            boolean confirmado =
                    shipmentBean.confirmarRecepcion(Long.valueOf(id));
            if (!confirmado) {
                r = Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity("Error al confirmar.")
                        .build();
            } else {
                r = Response
                        .status(Response.Status.CREATED)
                        .entity("Confirmación exitosa.")
                        .build();
            }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        return r;
    }

    @POST
    @Path("isclient")
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean esCliente(InputStream input) throws IOException{
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
            Long id = Long.valueOf(datos[0]);
            Long idCliente = Long.valueOf(datos[1]);
            retorno = shipmentBean.esCliente(id, idCliente);

            } catch (Exception e) {
                System.out.println("ERROR en esCliente "
                        + "(Resource) :" + e.getMessage());
            }
        return retorno;
    }
}

