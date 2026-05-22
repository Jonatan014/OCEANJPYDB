/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jarqu
 */

public class Variable {
    public String nombre;
    public TipoDato tipo;
    public Object valor;

    public Variable(String nombre, TipoDato tipo, Object valor) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = valor;
    }
}