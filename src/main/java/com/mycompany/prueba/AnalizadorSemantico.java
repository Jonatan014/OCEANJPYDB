/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prueba;

import java.util.ArrayList;

public class AnalizadorSemantico {
private TablaSimbolos tabla;
    public String analizar(ArrayList<Token> tokens) throws ErrorSemantico, ErrorSintactico, ErrorLogico{
        tabla = new TablaSimbolos();
        StringBuilder salida = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {

            Token t = tokens.get(i);

           if (t.tipo.equals("TIPO_DATO") || t.tipo.equals("IDENTIFICADOR")) {

    try {
        Token identificador;
        Token operador;
        Token valor;
        Token tipoToken;

        if (t.tipo.equals("TIPO_DATO")) {

            // forma vieja:
            // trucha x ~ "hola";
            
            tipoToken = t;
            identificador = tokens.get(i + 1);
            operador = tokens.get(i + 2);
            valor = tokens.get(i + 3);
            
        } else {

            // forma nueva:
            // x trucha ~ "hola";

            identificador = t;
            tipoToken = tokens.get(i + 1);
            operador = tokens.get(i + 2);
            valor = tokens.get(i + 3);

            if (!tipoToken.tipo.equals("TIPO_DATO")) {
                throw new ErrorSemantico(
                    "Se esperaba tipo de dato en linea " + t.linea
                );
            }
        }
        
        if (i + 3 < tokens.size()) {
            Token siguiente = tokens.get(i + 3);

            if (siguiente.tipo.equals("OPERADOR")) {
                throw new ErrorSemantico(
                    "Operador inválido '" + operador.lexema + siguiente.lexema +
                    "' en linea " + t.linea
                );
            }
        }
        
        if (!identificador.tipo.equals("IDENTIFICADOR")) {
            throw new ErrorSintactico(
                "Se esperaba identificador después del tipo de dato en linea "
                + t.linea
            );
        }

        if (!operador.lexema.equals("~")) {
            throw new ErrorSintactico(
                "Se esperaba operador de asignación '~' en linea "
                + t.linea
            );
        }

        TipoDato tipo = obtenerTipo(tipoToken.lexema);

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

            if (tipo == TipoDato.TRUCHA) {
                resultado = evaluarConcatenacion(tokens, inicioExp, finExp - 1);
            } else {
                resultado = evaluarExpresion(tokens, inicioExp, finExp - 1);
            }

            i = finExp;

        } else {

            Token fin = tokens.get(i + 4);

            if (!fin.lexema.equals(";")) {
                throw new ErrorSintactico(
                    "Falta ';' al final de la instrucción en linea "
                    + t.linea
                );
            }

            resultado = validarValor(tipo, valor);

            i += 4;
        }

        tabla.agregar(new Variable(identificador.lexema, tipo, resultado));

    } catch (IndexOutOfBoundsException e) {
        throw new ErrorSintactico(
            "Declaración incompleta en linea "
            + t.linea
        );
    }
}
            
            else if (t.lexema.equals("mostrar")) {

    try {
        Token abre = tokens.get(i + 1);
        Token id = tokens.get(i + 2);
        Token cierra = tokens.get(i + 3);
        Token fin = tokens.get(i + 4);

        if (!abre.lexema.equals("(")) {
            throw new ErrorSintactico(
                "Se esperaba '(' después de mostrar en linea "
                + t.linea
            );
        }

        if (!id.tipo.equals("IDENTIFICADOR")) {
            throw new ErrorSintactico(
                "Se esperaba identificador después del tipo de dato en linea "
                + t.linea
            );
        }

        if (!cierra.lexema.equals(")")) {
            throw new ErrorSintactico(
                "Se esperaba ')' en linea "
                + t.linea
            );
        }

        if (!fin.lexema.equals(";")) {
            throw new ErrorSintactico(
                "Falta ';' al final de la instrucción en linea "
                + t.linea
            );
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
                throw new ErrorSemantico(
                    "Error: Trucha solo acepta texto (linea " + valor.linea + ")"
                );
            }
            return valor.lexema;

        case CAMARON:

    // primero validar que sea NUMERO
    if (!valor.tipo.equals("NUMERO")) {

        throw new ErrorSemantico(
            "TypeMismatchException:\n" +
            "camaron no acepta valores tipo " + valor.tipo +
            " (linea " + valor.linea + ")"
        );
    }

    String aux = valor.lexema;

    if (!aux.matches("-?\\d{1,10}")) {

        throw new ErrorSemantico(
            "ErrorSemantico:\n" +
            "camaron solo puede tener hasta 10 digitos enteros " +
            "(linea " + valor.linea + ")"
        );
    }

    try {

        return Integer.parseInt(aux);

    } catch (NumberFormatException e) {

        throw new ErrorSemantico(
            "NumberFormatException:\n" +
            "valor entero invalido en linea " + valor.linea
        );
    }


case SALMON:

    // primero validar que sea NUMERO
    if (!valor.tipo.equals("NUMERO")) {

        throw new ErrorSemantico(
            "TypeMismatchException:\n" +
            "salmon no acepta valores tipo " + valor.tipo +
            " (linea " + valor.linea + ")"
        );
    }

    String numero = valor.lexema;

    if (!numero.matches("\\d{1,10}\\.\\d{1,8}")) {

        throw new ErrorSemantico(
            "ErrorSemantico:\n" +
            "salmon solo acepta formato decimal (#.#) " +
            "y maximo 8 decimales (linea " + valor.linea + ")"
        );
    }

    try {

        return Double.parseDouble(numero);

    } catch (NumberFormatException e) {

        throw new ErrorSemantico(
            "NumberFormatException:\n" +
            "valor decimal invalido en linea " + valor.linea
        );
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
    
    private double evaluarExpresion(ArrayList<Token> tokens, int inicio, int fin)
throws ErrorSemantico, ErrorLogico {
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
    
    private String evaluarConcatenacion(ArrayList<Token> tokens, int inicio, int fin)
        throws ErrorSemantico {

    StringBuilder resultado = new StringBuilder();

    for (int i = inicio; i <= fin; i++) {

        Token t = tokens.get(i);

        // Ignorar operador <
        if (t.lexema.equals("<")) {
            continue;
        }

        // Texto directo
        if (t.tipo.equals("STRING")) {
            resultado.append(t.lexema);
        }

        // Variables
        else if (t.tipo.equals("IDENTIFICADOR")) {

            Variable var = tabla.obtener(t.lexema);

            if (var == null) {
                throw new ErrorSemantico(
                    "Variable no declarada: " + t.lexema
                );
            }

            resultado.append(var.valor.toString());
        }

        else {
            throw new ErrorSemantico(
                "Concatenación inválida en linea " + t.linea
            );
        }
    }

    return resultado.toString();
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
    
    private double operar(double v1, double v2, String op)
    throws ErrorSemantico, ErrorLogico {

        switch (op) {

            case "<":
                return v1 + v2;

            case ">":
                return v1 - v2;

            case "$":
                return v1 * v2;

            case "%":

                if (v2 == 0) {
                    throw new ErrorLogico(
                        "División entre cero en operación matemática"
                    );
                }

                return v1 / v2;
        }

        throw new ErrorSemantico(
            "Operador no válido '" + op + "'"
        );
    }
}