package com.enviosya.shipment.tool;

import java.util.Objects;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 *
 * @author Gonzalo
 */
@Stateless
@LocalBean
public class CalculateCostBean {
    
    public double calcularCosto(double dato1,double dato2, double dato3) {
        double retorno = 0;
        retorno = dato1 * 3;
        retorno += (retorno +dato2) * 4;
        retorno += (retorno +dato3) * 2;
        return retorno;
    }
}