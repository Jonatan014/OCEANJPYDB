/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

import java.util.ArrayList;

public class AnalizadorSemantico {
private TablaSimbolos tabla;
    public String analizar(ArrayList<Token> tokens) throws ErrorSemantico{
        tabla = new TablaSimbolos();
        StringBuilder salida = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {

            Token t = tokens.get(i);

            if (t.tipo.equals("TIPO_DATO")) {

    try {
        Token identificador = tokens.get(i + 1);
        Token operador = tokens.get(i + 2);
        Token valor = tokens.get(i + 3);

        if (!identificador.tipo.equals("IDENTIFICADOR")) {
            throw new ErrorSemantico("Se esperaba identificador en linea " + t.linea);
        }

        if (!operador.lexema.equals("~")) {
            throw new ErrorSemantico("Se esperaba '~' en linea " + t.linea);
        }

        TipoDato tipo = obtenerTipo(t.lexema);

        if (tipo == null) {
            throw new ErrorSemantico("Tipo de dato desconocido en linea " + t.linea);
        }

        Object resultado;

        if (i + 4 < tokens.size() && tokens.get(i + 4).tipo.equals("OPERADOR")) {

            int inicioExp = i + 3;
            int finExp = inicioExp;

            while (finExp < tokens.size() && !tokens.get(finExp).lexema.equals(";")) {
                finExp++;
            }

            resultado = evaluarExpresion(tokens, inicioExp, finExp - 1);

            i = finExp;

        } else {

            Token fin = tokens.get(i + 4);

            if (!fin.lexema.equals(";")) {
                throw new ErrorSemantico("Se esperaba ';' en linea " + t.linea);
            }

            resultado = validarValor(tipo, valor);

            i += 4;
        }

        tabla.agregar(new Variable(identificador.lexema, tipo, resultado));

    } catch (IndexOutOfBoundsException e) {
        throw new ErrorSemantico("Declaración incompleta en linea " + t.linea);
    }
}
            
            else if (t.lexema.equals("mostrar")) {

    try {
        Token abre = tokens.get(i + 1);
        Token id = tokens.get(i + 2);
        Token cierra = tokens.get(i + 3);
        Token fin = tokens.get(i + 4);

        if (!abre.lexema.equals("(")) {
            throw new ErrorSemantico("Se esperaba '(' en linea " + t.linea);
        }

        if (!id.tipo.equals("IDENTIFICADOR")) {
            throw new ErrorSemantico("Se esperaba identificador en linea " + t.linea);
        }

        if (!cierra.lexema.equals(")")) {
            throw new ErrorSemantico("Se esperaba ')' en linea " + t.linea);
        }

        if (!fin.lexema.equals(";")) {
            throw new ErrorSemantico("Se esperaba ';' en linea " + t.linea);
        }

        Variable var = tabla.obtener(id.lexema);

        if (var == null) {
            throw new ErrorSemantico("Variable no declarada: " + id.lexema);
        }

        salida.append(var.valor).append("\n");

        i += 4;

    } catch (IndexOutOfBoundsException e) {
        throw new ErrorSemantico("Error en sentencia mostrar en linea " + t.linea);
    }
}
           else if (!t.tipo.equals("DELIMITADOR") && !t.tipo.equals("PALABRA_RESERVADA")) {
    throw new ErrorSemantico("Sentencia no válida en linea " + t.linea);
}
        }
        return salida.toString();
    }

    private TipoDato obtenerTipo(String tipo) {
        switch (tipo) {
            case "trucha": return TipoDato.TRUCHA;
            case "camaron": return TipoDato.CAMARON;
            case "salmon": return TipoDato.SALMON;
            default: return null;
        }
    }

    private Object validarValor(TipoDato tipo, Token valor) throws ErrorSemantico {

        switch (tipo) {

            case TRUCHA:
                if (!valor.tipo.equals("STRING")) {
                    throw new ErrorSemantico("Error: Trucha solo acepta texto (linea " + valor.linea + ")");
                }
                return valor.lexema;

            case CAMARON:
                try {
                    return Integer.parseInt(valor.lexema);
                } catch (NumberFormatException e) {
                    throw new ErrorSemantico("Error: Camaron solo acepta enteros (linea " + valor.linea + ")");
                }

            case SALMON:
                try {
                    return Double.parseDouble(valor.lexema);
                } catch (NumberFormatException e) {
                    throw new ErrorSemantico("Error: Salmon solo acepta decimales (linea " + valor.linea + ")");
                }
        }

        return null;
    }
    
    private int prioridad(String op) {
        switch (op) {
            case "$":
            case "%":
                return 2;
            case "<":
            case ">":
                return 1;
        }
        return 0;
    }
    
    private double evaluarExpresion(ArrayList<Token> tokens, int inicio, int fin) throws ErrorSemantico {

    ArrayList<Object> valores = new ArrayList<>();
    ArrayList<String> operadores = new ArrayList<>();

    int i = inicio;

    while (i <= fin) {

        Token t = tokens.get(i);

        if (t.tipo.equals("NUMERO") || t.tipo.equals("IDENTIFICADOR")) {
            valores.add(obtenerValorNumerico(t));
        }

        else if (t.tipo.equals("OPERADOR")) {

            while (!operadores.isEmpty() &&
                   prioridad(operadores.get(operadores.size() - 1)) >= prioridad(t.lexema)) {

                double v2 = (double) valores.remove(valores.size() - 1);
                double v1 = (double) valores.remove(valores.size() - 1);
                String op = operadores.remove(operadores.size() - 1);

                valores.add(operar(v1, v2, op));
            }

            operadores.add(t.lexema);
        }

        i++;
    }

    while (!operadores.isEmpty()) {
        double v2 = (double) valores.remove(valores.size() - 1);
        double v1 = (double) valores.remove(valores.size() - 1);
        String op = operadores.remove(operadores.size() - 1);

        valores.add(operar(v1, v2, op));
    }

    return (double) valores.get(0);
}
    
    private double obtenerValorNumerico(Token t) throws ErrorSemantico {

    if (t.tipo.equals("NUMERO")) {
        return Double.parseDouble(t.lexema);
    }

    if (t.tipo.equals("IDENTIFICADOR")) {
        Variable var = tabla.obtener(t.lexema);
        if (var == null) {
            throw new ErrorSemantico("Variable no definida: " + t.lexema);
        }
        return Double.parseDouble(var.valor.toString());
    }

    throw new ErrorSemantico("Valor no válido en linea " + t.linea);
}
    
    private double operar(double v1, double v2, String op) throws ErrorSemantico {

    switch (op) {
        case "<": return v1 + v2;
        case ">": return v1 - v2;
        case "$": return v1 * v2;
        case "%": return v2 != 0 ? v1 / v2 : 0;
    }

    throw new ErrorSemantico("Operador no válido: " + op);
}
}