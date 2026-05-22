/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

/**
 *
 * @author jarqu
 */

public class Token {
    public String tipo;
    public String lexema;
    public int linea;

    public Token(String tipo, String lexema, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linea = linea;
    }

    @Override
    public String toString() {
        return tipo + " -> " + lexema + " (Linea " + linea + ")";
    }
}