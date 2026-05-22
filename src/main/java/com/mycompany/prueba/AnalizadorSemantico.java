package com.mycompany.prueba;

import java.util.ArrayList;

public class AnalizadorSemantico {

    private TablaSimbolos tabla;

    public String analizar(ArrayList<Token> tokens)
            throws ErrorSemantico, ErrorSintactico, ErrorLogico {
        tabla = new TablaSimbolos();
        StringBuilder salida = new StringBuilder();
        analizarBloque(tokens, 0, tokens.size() - 1, salida);
        return salida.toString();
    }

    // ── Recorre tokens de inicio a fin e interpreta sentencias ───────────────
    private int analizarBloque(ArrayList<Token> tokens, int inicio, int fin,
                                StringBuilder salida)
            throws ErrorSemantico, ErrorSintactico, ErrorLogico {

        int i = inicio;
        while (i <= fin) {
            Token t = tokens.get(i);

            // ancla (if)
            if (t.lexema.equals("ancla")) {
                i = procesarAncla(tokens, i, fin, salida);
                continue;
            }

            // Declaración: tipo id ~ valor;  o  id tipo ~ valor;
            if (t.tipo.equals("TIPO_DATO") || t.tipo.equals("IDENTIFICADOR")) {
                i = procesarDeclaracion(tokens, i, fin, salida);
                continue;
            }

            // mostrar(id);
            if (t.lexema.equals("mostrar")) {
                i = procesarMostrar(tokens, i, salida);
                continue;
            }

            // Tokens ignorables
            if (t.tipo.equals("DELIMITADOR") || t.tipo.equals("PALABRA_RESERVADA")) {
                i++;
                continue;
            }

            throw new ErrorSemantico("Sentencia no válida en linea " + t.linea);
        }
        return i;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ANCLA / RED
    // ════════════════════════════════════════════════════════════════════════
    private int procesarAncla(ArrayList<Token> tokens, int i, int fin,
                               StringBuilder salida)
            throws ErrorSemantico, ErrorSintactico, ErrorLogico {

        Token t = tokens.get(i); // 'ancla'
        i++;

        if (i > fin || !tokens.get(i).lexema.equals("("))
            throw new ErrorSintactico("Se esperaba '(' después de 'ancla' en linea " + t.linea);
        i++;

        // Recolectar condición hasta ')'
        int inicioCondicion = i;
        int prof = 1;
        while (i <= fin && prof > 0) {
            String lex = tokens.get(i).lexema;
            if (lex.equals("(")) prof++;
            else if (lex.equals(")")) prof--;
            if (prof > 0) i++;
            else break;
        }
        int finCondicion = i - 1;
        i++; // saltar ')'

        boolean condicion = evaluarCondicion(tokens, inicioCondicion, finCondicion);

        if (i > fin || !tokens.get(i).lexema.equals("{"))
            throw new ErrorSintactico("Se esperaba '{' después de la condición en linea " + t.linea);
        i++;

        int cierreAncla = encontrarCierre(tokens, i, fin);
        if (condicion)
            analizarBloque(tokens, i, cierreAncla - 1, salida);
        i = cierreAncla + 1;

        // red (else) — opcional
        if (i <= fin && tokens.get(i).lexema.equals("red")) {
            i++;
            if (i > fin || !tokens.get(i).lexema.equals("{"))
                throw new ErrorSintactico("Se esperaba '{' después de 'red' en linea " + t.linea);
            i++;

            int cierreRed = encontrarCierre(tokens, i, fin);
            if (!condicion)
                analizarBloque(tokens, i, cierreRed - 1, salida);
            i = cierreRed + 1;
        }

        return i;
    }

    /** Devuelve el índice del '}' que cierra el bloque abierto en 'inicio'. */
    private int encontrarCierre(ArrayList<Token> tokens, int inicio, int fin)
            throws ErrorSintactico {
        int prof = 1;
        int i = inicio;
        while (i <= fin) {
            String lex = tokens.get(i).lexema;
            if (lex.equals("{")) prof++;
            else if (lex.equals("}")) { if (--prof == 0) return i; }
            i++;
        }
        throw new ErrorSintactico("Bloque sin cerrar '}'");
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONDICIÓN
    // ════════════════════════════════════════════════════════════════════════
    /**
     * Evalúa la condición de un ancla.
     * Soporta:
     *   · izq COMPARADOR der   — comparación explícita (~~, !~, ~-, ~+, -, +)
     *   · expresión aritmética — truthy si resultado ≠ 0 / texto no vacío
     */
    private boolean evaluarCondicion(ArrayList<Token> tokens, int inicio, int fin)
            throws ErrorSemantico, ErrorSintactico, ErrorLogico {

        // Buscar primer COMPARADOR
        int posComp = -1;
        for (int i = inicio; i <= fin; i++) {
            if (tokens.get(i).tipo.equals("COMPARADOR")) { posComp = i; break; }
        }

        if (posComp != -1) {
            Token compTok = tokens.get(posComp);
            Object izq = evaluarLado(tokens, inicio, posComp - 1);
            Object der = evaluarLado(tokens, posComp + 1, fin);
            return comparar(izq, der, compTok.lexema, compTok.linea);
        }

        // Sin comparador → truthy
        Object res = evaluarLado(tokens, inicio, fin);
        if (res instanceof Double)  return ((Double)  res) != 0.0;
        if (res instanceof Integer) return ((Integer) res) != 0;
        if (res instanceof String)  return !((String) res).isEmpty();
        throw new ErrorSemantico("Condición inválida en linea " + tokens.get(inicio).linea);
    }

    private Object evaluarLado(ArrayList<Token> tokens, int inicio, int fin)
            throws ErrorSemantico, ErrorLogico {
        if (inicio == fin) return obtenerValorObj(tokens.get(inicio));
        return evaluarExpresion(tokens, inicio, fin); // devuelve double
    }

    private boolean comparar(Object izq, Object der, String op, int linea)
            throws ErrorSemantico {

        // Comparación de Strings (solo ~~ y !~)
        if (izq instanceof String && der instanceof String) {
            String si = izq.toString();
            String sd = der.toString();
            switch (op) {
                case "~~": return si.equals(sd);
                case "!~": return !si.equals(sd);
                default: throw new ErrorSemantico(
                    "Operador '" + op + "' no válido para texto en linea " + linea);
            }
        }

        double di = toDouble(izq, op, linea);
        double dd = toDouble(der, op, linea);
        switch (op) {
            case "~~": return di == dd;
            case "!~": return di != dd;
            case "~-": return di <= dd;
            case "~+": return di >= dd;
            case "-":  return di <  dd;
            case "+":  return di >  dd;
            default: throw new ErrorSemantico("Operador de comparación desconocido: " + op);
        }
    }

    private double toDouble(Object v, String op, int linea) throws ErrorSemantico {
        if (v instanceof Double)  return (Double) v;
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        throw new ErrorSemantico(
            "Operador '" + op + "' solo es válido entre números en linea " + linea);
    }

    // ════════════════════════════════════════════════════════════════════════
    // DECLARACIÓN
    // ════════════════════════════════════════════════════════════════════════
    private int procesarDeclaracion(ArrayList<Token> tokens, int i, int fin,
                                    StringBuilder salida)
            throws ErrorSemantico, ErrorSintactico, ErrorLogico {
        Token t = tokens.get(i);
        try {
            Token identificador, tipoToken, operador, valor;

            if (t.tipo.equals("TIPO_DATO")) {
                // trucha x ~ "hola";
                tipoToken    = t;
                identificador = tokens.get(i + 1);
                operador     = tokens.get(i + 2);
                valor        = tokens.get(i + 3);
            } else {
                // x trucha ~ "hola";
                identificador = t;
                tipoToken    = tokens.get(i + 1);
                operador     = tokens.get(i + 2);
                valor        = tokens.get(i + 3);
                if (!tipoToken.tipo.equals("TIPO_DATO"))
                    throw new ErrorSemantico("Se esperaba tipo de dato en linea " + t.linea);
            }

            if (!identificador.tipo.equals("IDENTIFICADOR"))
                throw new ErrorSintactico(
                    "Se esperaba identificador en linea " + t.linea);
            if (!operador.lexema.equals("~"))
                throw new ErrorSintactico(
                    "Se esperaba '~' en linea " + t.linea);

            TipoDato tipo = obtenerTipo(tipoToken.lexema);
            if (tipo == null)
                throw new ErrorSemantico("Tipo de dato desconocido en linea " + t.linea);

            Object resultado;

            if (i + 4 < tokens.size() && tokens.get(i + 4).tipo.equals("OPERADOR")) {
                int inicioExp = i + 3;
                int finExp = inicioExp;
                while (finExp < tokens.size() && !tokens.get(finExp).lexema.equals(";")) finExp++;

                if (tipo == TipoDato.TRUCHA)
                    resultado = evaluarConcatenacion(tokens, inicioExp, finExp - 1);
                else
                    resultado = evaluarExpresion(tokens, inicioExp, finExp - 1);

                i = finExp;
            } else {
                Token finTok = tokens.get(i + 4);
                if (!finTok.lexema.equals(";"))
                    throw new ErrorSintactico("Falta ';' al final de la instrucción en linea " + t.linea);
                resultado = validarValor(tipo, valor);
                i += 4;
            }

            tabla.agregar(new Variable(identificador.lexema, tipo, resultado));
            return i + 1;

        } catch (IndexOutOfBoundsException e) {
            throw new ErrorSintactico("Declaración incompleta en linea " + t.linea);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // MOSTRAR
    // ════════════════════════════════════════════════════════════════════════
    private int procesarMostrar(ArrayList<Token> tokens, int i, StringBuilder salida)
            throws ErrorSemantico, ErrorSintactico {
        Token t = tokens.get(i);
        try {
            Token abre   = tokens.get(i + 1);
            Token id     = tokens.get(i + 2);
            Token cierra = tokens.get(i + 3);
            Token fin    = tokens.get(i + 4);

            if (!abre.lexema.equals("("))
                throw new ErrorSintactico("Se esperaba '(' después de mostrar en linea " + t.linea);
            if (!id.tipo.equals("IDENTIFICADOR"))
                throw new ErrorSintactico("Se esperaba identificador en linea " + t.linea);
            if (!cierra.lexema.equals(")"))
                throw new ErrorSintactico("Se esperaba ')' en linea " + t.linea);
            if (!fin.lexema.equals(";"))
                throw new ErrorSintactico("Falta ';' al final de la instrucción en linea " + t.linea);

            Variable var = tabla.obtener(id.lexema);
            if (var == null)
                throw new ErrorSemantico("Variable no declarada: " + id.lexema);

            salida.append(var.valor).append("\n");
            return i + 5;

        } catch (IndexOutOfBoundsException e) {
            throw new ErrorSemantico("Error en sentencia mostrar en linea " + t.linea);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private TipoDato obtenerTipo(String tipo) {
        switch (tipo) {
            case "trucha":  return TipoDato.TRUCHA;
            case "camaron": return TipoDato.CAMARON;
            case "salmon":  return TipoDato.SALMON;
            default:        return null;
        }
    }

    private Object validarValor(TipoDato tipo, Token valor) throws ErrorSemantico {
        switch (tipo) {
            case TRUCHA:
                if (!valor.tipo.equals("STRING"))
                    throw new ErrorSemantico(
                        "Error: Trucha solo acepta texto (linea " + valor.linea + ")");
                return valor.lexema;

            case CAMARON:
                if (!valor.tipo.equals("NUMERO"))
                    throw new ErrorSemantico(
                        "TypeMismatchException:\ncamaron no acepta valores tipo "
                        + valor.tipo + " (linea " + valor.linea + ")");
                if (!valor.lexema.matches("-?\\d{1,10}"))
                    throw new ErrorSemantico(
                        "ErrorSemantico:\ncamaron solo puede tener hasta 10 dígitos enteros "
                        + "(linea " + valor.linea + ")");
                try { return Integer.parseInt(valor.lexema); }
                catch (NumberFormatException e) {
                    throw new ErrorSemantico(
                        "NumberFormatException:\nvalor entero invalido en linea " + valor.linea);
                }

            case SALMON:
                if (!valor.tipo.equals("NUMERO"))
                    throw new ErrorSemantico(
                        "TypeMismatchException:\nsalmon no acepta valores tipo "
                        + valor.tipo + " (linea " + valor.linea + ")");
                if (!valor.lexema.matches("\\d{1,10}\\.\\d{1,8}"))
                    throw new ErrorSemantico(
                        "ErrorSemantico:\nsalmon solo acepta formato decimal (#.#) "
                        + "máximo 8 decimales (linea " + valor.linea + ")");
                try { return Double.parseDouble(valor.lexema); }
                catch (NumberFormatException e) {
                    throw new ErrorSemantico(
                        "NumberFormatException:\nvalor decimal invalido en linea " + valor.linea);
                }
        }
        return null;
    }

    private int prioridad(String op) {
        switch (op) {
            case "$": case "%": return 2;
            case "<": case ">": return 1;
            default:            return 0;
        }
    }

    private double evaluarExpresion(ArrayList<Token> tokens, int inicio, int fin)
            throws ErrorSemantico, ErrorLogico {
        ArrayList<Double>  valores    = new ArrayList<>();
        ArrayList<String>  operadores = new ArrayList<>();

        for (int i = inicio; i <= fin; i++) {
            Token t = tokens.get(i);
            if (t.tipo.equals("NUMERO") || t.tipo.equals("IDENTIFICADOR")) {
                valores.add(obtenerValorNumerico(t));
            } else if (t.tipo.equals("OPERADOR")) {
                while (!operadores.isEmpty() &&
                       prioridad(operadores.get(operadores.size() - 1)) >= prioridad(t.lexema)) {
                    double v2 = valores.remove(valores.size() - 1);
                    double v1 = valores.remove(valores.size() - 1);
                    String op = operadores.remove(operadores.size() - 1);
                    valores.add(operar(v1, v2, op));
                }
                operadores.add(t.lexema);
            }
        }
        while (!operadores.isEmpty()) {
            double v2 = valores.remove(valores.size() - 1);
            double v1 = valores.remove(valores.size() - 1);
            String op = operadores.remove(operadores.size() - 1);
            valores.add(operar(v1, v2, op));
        }
        return valores.get(0);
    }

    private String evaluarConcatenacion(ArrayList<Token> tokens, int inicio, int fin)
            throws ErrorSemantico {
        StringBuilder resultado = new StringBuilder();
        for (int i = inicio; i <= fin; i++) {
            Token t = tokens.get(i);
            if (t.lexema.equals("<")) continue; // operador concatenación
            if (t.tipo.equals("STRING")) {
                resultado.append(t.lexema);
            } else if (t.tipo.equals("IDENTIFICADOR")) {
                Variable var = tabla.obtener(t.lexema);
                if (var == null)
                    throw new ErrorSemantico("Variable no declarada: " + t.lexema);
                resultado.append(var.valor.toString());
            } else {
                throw new ErrorSemantico("Concatenación inválida en linea " + t.linea);
            }
        }
        return resultado.toString();
    }

    private double obtenerValorNumerico(Token t) throws ErrorSemantico {
        if (t.tipo.equals("NUMERO")) return Double.parseDouble(t.lexema);
        if (t.tipo.equals("IDENTIFICADOR")) {
            Variable var = tabla.obtener(t.lexema);
            if (var == null) throw new ErrorSemantico("Variable no definida: " + t.lexema);
            return Double.parseDouble(var.valor.toString());
        }
        throw new ErrorSemantico("Valor no válido en linea " + t.linea);
    }

    /** Obtiene el valor de un token como Object (para condiciones mixtas). */
    private Object obtenerValorObj(Token t) throws ErrorSemantico {
        if (t.tipo.equals("NUMERO")) {
            String lex = t.lexema;
            if (lex.contains(".")) return Double.parseDouble(lex);
            return Integer.parseInt(lex);
        }
        if (t.tipo.equals("STRING")) return t.lexema;
        if (t.tipo.equals("IDENTIFICADOR")) {
            Variable var = tabla.obtener(t.lexema);
            if (var == null) throw new ErrorSemantico("Variable no definida: " + t.lexema);
            return var.valor;
        }
        throw new ErrorSemantico("Valor no válido en linea " + t.linea);
    }

    private double operar(double v1, double v2, String op)
            throws ErrorSemantico, ErrorLogico {
        switch (op) {
            case "<": return v1 + v2;
            case ">": return v1 - v2;
            case "$": return v1 * v2;
            case "%":
                if (v2 == 0) throw new ErrorLogico("División entre cero en operación matemática");
                return v1 / v2;
        }
        throw new ErrorSemantico("Operador no válido '" + op + "'");
    }
}