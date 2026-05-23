package com.mycompany.prueba;
 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
 
public class VentanaPrincipal extends JFrame {
 
    private JTextArea txtCodigo;
    private JTable    tablaTokens;
    private JTextArea txtResultados;
    private String    arbolGenerado = "";
 
    private AnalizadorLexico    al = new AnalizadorLexico();
    private AnalizadorSemantico as = new AnalizadorSemantico();
 
    public VentanaPrincipal() {
        setTitle("OceanJpy++#DBjsNativeShell on rails");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
 
        // ── Panel de código ──────────────────────────────────────────────────
        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Código"));
 
        txtCodigo = new JTextArea();
        panelEntrada.add(new JScrollPane(txtCodigo), BorderLayout.CENTER);
 
        JButton btnAnalizar = new JButton("Analizar");
        JButton btnArbol    = new JButton("Mostrar Producción");
        JPanel  panelBotones = new JPanel();
        panelBotones.add(btnAnalizar);
        panelBotones.add(btnArbol);
        panelEntrada.add(panelBotones, BorderLayout.SOUTH);
 
        // ── Panel de tabla + ayuda ───────────────────────────────────────────
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Tokens"));
 
        tablaTokens = new JTable();
        panelTabla.add(new JScrollPane(tablaTokens), BorderLayout.CENTER);
 
        JTextArea txtAyuda = new JTextArea();
        txtAyuda.setEditable(false);
        txtAyuda.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtAyuda.setText("""
        === SIMBOLOGÍA DEL LENGUAJE ===
 
        TIPOS DE DATO:
          trucha  → String (texto)
          camaron → int (número entero)
          salmon  → double (decimal)
 
        OPERADORES ARITMÉTICOS:
          <  → suma (+)
          >  → resta (-)
          ~  → asignación (=)
          $  → multiplicación (*)
          %  → división (/)
 
        OPERADORES DE COMPARACIÓN:
          ~~  → igual a        (==)
          !~  → diferente de   (!=)
          ~-  → menor o igual  (<=)
          ~+  → mayor o igual  (>=)
          -   → menor que      (<)
          +   → mayor que      (>)
 
        PALABRAS RESERVADAS:
          mostrar → imprime el valor de una variable
          ancla   → condicional if
          red     → alternativa else
 
        EJEMPLO BÁSICO:
          salmon p1 ~ 5.0;
          salmon p2 ~ 3.0;
          salmon r ~ p1 < p2;
          mostrar(r);
 
        EJEMPLO ANCLA / RED:
          camaron edad ~ 20;
          ancla(edad + 18) {
            camaron mayor ~ 1;
            mostrar(mayor);
          } red {
            camaron menor ~ 0;
            mostrar(menor);
          }
        """);
 
        JScrollPane scrollAyuda = new JScrollPane(txtAyuda);
        scrollAyuda.setBorder(BorderFactory.createTitledBorder("Ayuda / Lenguaje"));
        scrollAyuda.setPreferredSize(new Dimension(400, 200));
        panelTabla.add(scrollAyuda, BorderLayout.SOUTH);
 
        // ── Panel de resultados ───────────────────────────────────────────────
        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));
 
        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        panelResultados.add(new JScrollPane(txtResultados), BorderLayout.CENTER);
 
        // ── Layout principal ──────────────────────────────────────────────────
        JSplitPane splitSuperior =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEntrada, panelTabla);
        splitSuperior.setDividerLocation(400);
 
        add(splitSuperior, BorderLayout.CENTER);
        add(panelResultados, BorderLayout.SOUTH);
        panelResultados.setPreferredSize(new Dimension(800, 150));
 
        btnAnalizar.addActionListener(e -> analizarCodigo());
        btnArbol.addActionListener(e -> mostrarProduccion());
 
        setVisible(true);
    }
 
    // ── Análisis completo ─────────────────────────────────────────────────────
    private void analizarCodigo() {
        String codigo = txtCodigo.getText();
        try {
            ArrayList<Token> tokens = al.analizar(codigo);
 
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Tipo");
            modelo.addColumn("Lexema");
            modelo.addColumn("Patrón");
            modelo.addColumn("Reservada");
            modelo.addColumn("Token");

            for (Token t : tokens) {
                modelo.addRow(new Object[]{
                t.tipo,
                t.lexema,
                obtenerPatron(t),
                esReservada(t),
                t.lexema
            });
}
            tablaTokens.setModel(modelo);
 
            String resultado = as.analizar(tokens);
            arbolGenerado = generarArbol(tokens);
            txtResultados.setText(resultado.isEmpty() ? "Análisis correcto" : resultado);
 
        } catch (ErrorLexico    ex) { txtResultados.setText("ERROR LÉXICO\n\n"    + ex.getMessage()); }
          catch (ErrorSintactico ex) { txtResultados.setText("ERROR SINTÁCTICO\n\n"+ ex.getMessage()); }
          catch (ErrorSemantico  ex) { txtResultados.setText("ERROR SEMÁNTICO\n\n" + ex.getMessage()); }
          catch (ErrorLogico     ex) { txtResultados.setText("ERROR LÓGICO\n\n"    + ex.getMessage()); }
          catch (Exception       ex) { txtResultados.setText("ERROR GENERAL\n\n"   + ex.getMessage()); }
    }
 
    // ── Mostrar árbol sintáctico ──────────────────────────────────────────────
    private void mostrarProduccion() {
        if (arbolGenerado.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Primero analiza un código", "Información",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JTextArea area = new JTextArea(arbolGenerado);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scroll,
            "Producción / Árbol Sintáctico", JOptionPane.INFORMATION_MESSAGE);
    }
 
    // ── Patrón por tipo de token ──────────────────────────────────────────────
    private String obtenerPatron(Token t) {
    switch (t.tipo) {
        case "TIPO_DATO":
            switch (t.lexema) {
                case "trucha":  return "[a-zA-Z][a-zA-Z0-9]{0,9}\\s+trucha\\s*~\\s*\"[^\"]*\"";
                case "camaron": return "[a-zA-Z][a-zA-Z0-9]{0,9}\\s+camaron\\s*~\\s*-?\\d{1,10}";
                case "salmon":  return "[a-zA-Z][a-zA-Z0-9]{0,9}\\s+salmon\\s*~\\s*\\d{1,10}\\.\\d{1,8}";
            }
            break;
        case "IDENTIFICADOR":    return "[a-zA-Z][a-zA-Z0-9]{0,9}";
        case "NUMERO":           return "\\d{1,10}(\\.\\d{1,8})?";
        case "STRING":           return "\"[^\"]*\"";
        case "OPERADOR":
            switch (t.lexema) {
                case "<": return "[<]";
                case ">": return "[>]";
                case "~": return "[~]";
                case "$": return "\\$";
                case "%": return "[%]";
            }
            break;
        case "COMPARADOR":
            switch (t.lexema) {
                case "~~": return "~~";
                case "!~": return "!~";
                case "~-": return "~-";
                case "~+": return "~\\+";
                case "-":  return "-";
                case "+":  return "\\+";
            }
            break;
        case "DELIMITADOR":
            switch (t.lexema) {
                case ";": return ";";
                case "(": return "\\(";
                case ")": return "\\)";
                case "{": return "\\{";
                case "}": return "\\}";
            }
            break;
        case "PALABRA_RESERVADA":
            switch (t.lexema) {
                case "mostrar": return "mostrar\\([a-zA-Z][a-zA-Z0-9]{0,9}\\);";
                case "ancla":   return "ancla\\(.+\\)\\s*\\{[^}]*\\}";
                case "red":     return "red\\s*\\{[^}]*\\}";
            }
            break;
    }
    return "Desconocido";
}
 
    private String esReservada(Token t) {
        switch (t.tipo) {
            case "PALABRA_RESERVADA":
            case "TIPO_DATO":
            case "OPERADOR":
            case "COMPARADOR":
            case "DELIMITADOR":
                return "Sí";
            default:
                return "No";
        }
    }
 
    // ── Árbol sintáctico ──────────────────────────────────────────────────────
    private String generarArbol(ArrayList<Token> tokens) {
        StringBuilder arbol = new StringBuilder();
 
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
 
            // ancla
            if (t.lexema.equals("ancla")) {
                arbol.append("<ancla>\n");
                arbol.append(" ├── palabra_reservada → ancla\n");
                // condición entre paréntesis
                int j = i + 2; // saltar 'ancla' y '('
                arbol.append(" ├── <condicion>\n");
                while (j < tokens.size() && !tokens.get(j).lexema.equals(")")) {
                    arbol.append(" │     ├── ").append(tokens.get(j).lexema).append("\n");
                    j++;
                }
                arbol.append(" └── bloque { ... }\n\n");
                continue;
            }
 
            // red
            if (t.lexema.equals("red")) {
                arbol.append("<red>\n");
                arbol.append(" └── bloque { ... }\n\n");
                continue;
            }
 
            // declaración
            if (t.tipo.equals("IDENTIFICADOR") || t.tipo.equals("TIPO_DATO")) {
                arbol.append("<declaracion>\n");
                arbol.append(" ├── ").append(t.tipo.toLowerCase())
                     .append(" → ").append(t.lexema).append("\n");
                if (i + 1 < tokens.size())
                    arbol.append(" ├── ").append(tokens.get(i+1).tipo.toLowerCase())
                         .append(" → ").append(tokens.get(i+1).lexema).append("\n");
                if (i + 2 < tokens.size())
                    arbol.append(" ├── <asignacion> → ").append(tokens.get(i+2).lexema).append("\n");
                arbol.append(" ├── <expresion>\n");
                int j = i + 3;
                while (j < tokens.size() && !tokens.get(j).lexema.equals(";")) {
                    Token aux = tokens.get(j);
                    String rama = switch (aux.tipo) {
                        case "NUMERO"      -> "<numero> → " + aux.lexema;
                        case "STRING"      -> "<string> → \"" + aux.lexema + "\"";
                        case "IDENTIFICADOR" -> "<identificador> → " + aux.lexema;
                        case "OPERADOR"    -> "<operador> → " + aux.lexema;
                        default            -> aux.lexema;
                    };
                    arbol.append(" │     ├── ").append(rama).append("\n");
                    j++;
                }
                arbol.append(" └── <delimitador> → ;\n\n");
                continue;
            }
 
            // mostrar
            if (t.lexema.equals("mostrar")) {
                arbol.append("<mostrar>\n");
                arbol.append(" ├── palabra_reservada → mostrar\n");
                if (i + 2 < tokens.size())
                    arbol.append(" └── identificador → ")
                         .append(tokens.get(i+2).lexema).append("\n\n");
            }
        }
        return arbol.toString();
    }
}
 