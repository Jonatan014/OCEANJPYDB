package com.mycompany.prueba;

import java.util.ArrayList;

public class AnalizadorSemantico {

    private TablaSimbolos tabla;

    public String analizar(ArrayList<Token> tokens) throws ErrorSemantico {
        tabla = new TablaSimbolos();
        StringBuilder salida = new StringBuilder();

        for (int i = 0; i < tokens.size(); i++) {

            Token t = tokens.get(i);

            // ── Declaración de variable ──────────────────────────────────────
            if (t.tipo.equals("TIPO_DATO")) {
                try {
                    Token identificador = tokens.get(i + 1);
                    Token operador      = tokens.get(i + 2);
                    Token valor         = tokens.get(i + 3);

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

                    // ¿Hay una expresión multi-término?
                    if (i + 4 < tokens.size() && tokens.get(i + 4).tipo.equals("OPERADOR")) {

                        int inicioExp = i + 3;
                        int finExp    = inicioExp;
                        while (finExp < tokens.size() && !tokens.get(finExp).lexema.equals(";")) {
                            finExp++;
                        }

                        resultado = evaluarExpresion(tokens, inicioExp, finExp - 1, tipo);
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

            // ── Sentencia mostrar ────────────────────────────────────────────
            else if (t.lexema.equals("mostrar")) {
                try {
                    Token abre   = tokens.get(i + 1);
                    Token id     = tokens.get(i + 2);
                    Token cierra = tokens.get(i + 3);
                    Token fin    = tokens.get(i + 4);

                    if (!abre.lexema.equals("("))
                        throw new ErrorSemantico("Se esperaba '(' en linea " + t.linea);
                    if (!id.tipo.equals("IDENTIFICADOR"))
                        throw new ErrorSemantico("Se esperaba identificador en linea " + t.linea);
                    if (!cierra.lexema.equals(")"))
                        throw new ErrorSemantico("Se esperaba ')' en linea " + t.linea);
                    if (!fin.lexema.equals(";"))
                        throw new ErrorSemantico("Se esperaba ';' en linea " + t.linea);

                    Variable var = tabla.obtener(id.lexema);
                    if (var == null)
                        throw new ErrorSemantico("Variable no declarada: " + id.lexema);

                    salida.append(formatearValor(var)).append("\n");
                    i += 4;

                } catch (IndexOutOfBoundsException e) {
                    throw new ErrorSemantico("Error en sentencia mostrar en linea " + t.linea);
                }
            }

            // ── Token inesperado ─────────────────────────────────────────────
            else if (!t.tipo.equals("DELIMITADOR") && !t.tipo.equals("PALABRA_RESERVADA")) {
                throw new ErrorSemantico("Sentencia no válida en linea " + t.linea);
            }
        }

        return salida.toString();
    }

    // ── Formateo de salida según tipo ────────────────────────────────────────
    private String formatearValor(Variable var) {
        if (var.tipo == TipoDato.CAMARON) {
            // Siempre mostrar como entero
            return String.valueOf(((Number) var.valor).intValue());
        }
        if (var.tipo == TipoDato.SALMON) {
            // Mostrar como decimal sin notación científica
            double d = ((Number) var.valor).doubleValue();
            // Quitar ceros innecesarios al final
            String s = String.format("%.8f", d).replaceAll("0+$", "").replaceAll("\\.$", ".0");
            return s;
        }
        // TRUCHA: quitar comillas del lexema almacenado
        String s = var.valor.toString();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    // ── Obtener TipoDato desde lexema ────────────────────────────────────────
    private TipoDato obtenerTipo(String tipo) {
        switch (tipo) {
            case "trucha":  return TipoDato.TRUCHA;
            case "camaron": return TipoDato.CAMARON;
            case "salmon":  return TipoDato.SALMON;
            default:        return null;
        }
    }

    // ── Validar y convertir un valor literal ─────────────────────────────────
    private Object validarValor(TipoDato tipo, Token valor) throws ErrorSemantico {
        switch (tipo) {

            case TRUCHA:
                if (!valor.tipo.equals("STRING")) {
                    throw new ErrorSemantico(
                        "Error: trucha solo acepta texto (linea " + valor.linea + ")");
                }
                return valor.lexema; // guardamos con comillas, formatearValor las quita

            case CAMARON:
                if (!valor.lexema.matches("-?\\d{1,10}")) {
                    throw new ErrorSemantico(
                        "Error: camaron solo puede tener hasta 10 dígitos enteros (linea "
                        + valor.linea + ")");
                }
                try {
                    return Integer.parseInt(valor.lexema);
                } catch (NumberFormatException e) {
                    throw new ErrorSemantico(
                        "Error: camaron solo acepta enteros válidos (linea " + valor.linea + ")");
                }

            case SALMON:
                if (!valor.lexema.matches("\\d{1,10}\\.\\d{1,8}")) {
                    throw new ErrorSemantico(
                        "Error: salmon acepta formato decimal con máximo 10 dígitos enteros "
                        + "y 8 decimales (ej: 3.14) en linea " + valor.linea);
                }
                try {
                    return Double.parseDouble(valor.lexema);
                } catch (NumberFormatException e) {
                    throw new ErrorSemantico(
                        "Error: valor decimal inválido (linea " + valor.linea + ")");
                }
        }
        return null;
    }

    // ── Prioridad de operadores ───────────────────────────────────────────────
    private int prioridad(String op) {
        switch (op) {
            case "$": case "%": return 2;
            case "<": case ">": return 1;
            default:            return 0;
        }
    }

    // ── Evaluar expresión (shunting-yard) ─────────────────────────────────────
    //   tipo: tipo destino de la variable, usado para decidir cómo operar con <
    private Object evaluarExpresion(ArrayList<Token> tokens, int inicio, int fin, TipoDato tipo)
            throws ErrorSemantico {

        ArrayList<Object> valores    = new ArrayList<>();
        ArrayList<String> operadores = new ArrayList<>();

        for (int i = inicio; i <= fin; i++) {
            Token t = tokens.get(i);

            if (t.tipo.equals("NUMERO") || t.tipo.equals("IDENTIFICADOR") || t.tipo.equals("STRING")) {
                valores.add(obtenerValor(t));
            }
            else if (t.tipo.equals("OPERADOR")) {
                while (!operadores.isEmpty() &&
                       prioridad(operadores.get(operadores.size() - 1)) >= prioridad(t.lexema)) {
                    Object v2 = valores.remove(valores.size() - 1);
                    Object v1 = valores.remove(valores.size() - 1);
                    String op = operadores.remove(operadores.size() - 1);
                    valores.add(operar(v1, v2, op, tipo));
                }
                operadores.add(t.lexema);
            }
        }

        while (!operadores.isEmpty()) {
            Object v2 = valores.remove(valores.size() - 1);
            Object v1 = valores.remove(valores.size() - 1);
            String op = operadores.remove(operadores.size() - 1);
            valores.add(operar(v1, v2, op, tipo));
        }

        // Convertir resultado al tipo destino
        Object resultado = valores.get(0);
        if (tipo == TipoDato.CAMARON && resultado instanceof Double) {
            return ((Double) resultado).intValue();
        }
        return resultado;
    }

    // ── Obtener valor de un token ─────────────────────────────────────────────
    private Object obtenerValor(Token t) throws ErrorSemantico {
        if (t.tipo.equals("NUMERO")) {
            String lex = t.lexema;
            // Si no tiene punto es entero
            if (!lex.contains(".")) return Integer.parseInt(lex);
            return Double.parseDouble(lex);
        }
        if (t.tipo.equals("STRING")) {
            return t.lexema; // con comillas
        }
        if (t.tipo.equals("IDENTIFICADOR")) {
            Variable var = tabla.obtener(t.lexema);
            if (var == null) throw new ErrorSemantico("Variable no definida: " + t.lexema);
            return var.valor;
        }
        throw new ErrorSemantico("Valor no válido en linea " + t.linea);
    }

    // ── Operar dos valores ────────────────────────────────────────────────────
    private Object operar(Object v1, Object v2, String op, TipoDato tipoDestino) throws ErrorSemantico {

        // < puede ser suma numérica O concatenación de strings según el tipo destino
        if (op.equals("<")) {
            if (tipoDestino == TipoDato.TRUCHA) {
                // Concatenación: quitar comillas internas, concatenar, volver a poner
                String s1 = quitarComillas(v1.toString());
                String s2 = quitarComillas(v2.toString());
                return "\"" + s1 + s2 + "\"";
            }
            // Suma numérica
            return toDouble(v1, op) + toDouble(v2, op);
        }

        switch (op) {
            case ">": return toDouble(v1, op) - toDouble(v2, op);
            case "$": return toDouble(v1, op) * toDouble(v2, op);
            case "%": {
                double d2 = toDouble(v2, op);
                return d2 != 0 ? toDouble(v1, op) / d2 : 0.0;
            }
        }

        throw new ErrorSemantico("Operador no válido: " + op);
    }

    private double toDouble(Object v, String op) throws ErrorSemantico {
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        if (v instanceof Double)  return (Double) v;
        throw new ErrorSemantico("Operación '" + op + "' solo es válida entre números");
    }

    private String quitarComillas(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}