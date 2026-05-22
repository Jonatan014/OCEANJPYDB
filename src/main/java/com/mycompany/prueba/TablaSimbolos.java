/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jonat
 */

import java.util.HashMap;


public class TablaSimbolos {
    private HashMap<String, Variable> tabla = new HashMap<>();

    public void agregar(Variable var) throws ErrorSemantico {
        if (tabla.containsKey(var.nombre)) {
            throw new ErrorSemantico("Variable ya declarada: " + var.nombre);
        }
        tabla.put(var.nombre, var);
    }

    public Variable obtener(String nombre) {
        return tabla.get(nombre);
    }
}