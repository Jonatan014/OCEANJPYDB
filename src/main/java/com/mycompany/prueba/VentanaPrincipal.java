package com.mycompany.prueba;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {

    private JTextArea txtCodigo;
    private JTable tablaTokens;
    private JTextArea txtResultados;

    private AnalizadorLexico al = new AnalizadorLexico();
    private AnalizadorSemantico as = new AnalizadorSemantico();

    public VentanaPrincipal() {
        setTitle("myOceanJDBpyJS#++ on rails orlando estuvo aqui --ANUNCIESE AQUI-- este es un anuncio NO pagado (Pollos Garcia, los mas mejores pollos a la leña de todo el estado numero: 951 525 8293)");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.setBorder(BorderFactory.createTitledBorder("Código"));

        txtCodigo = new JTextArea();
        JScrollPane scrollCodigo = new JScrollPane(txtCodigo);

        JButton btnAnalizar = new JButton("Analizar");

        panelEntrada.add(scrollCodigo, BorderLayout.CENTER);
        panelEntrada.add(btnAnalizar, BorderLayout.SOUTH);

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder("Tabla de Tokens"));

        tablaTokens = new JTable();
        JScrollPane scrollTabla = new JScrollPane(tablaTokens);

        panelTabla.add(scrollTabla, BorderLayout.CENTER);
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
          ~~ → igual a      (==)
          !~ → diferente de (!=)
          ~- → menor igual  (<=)
          ~+ → mayor igual  (>=)
          -  → menor que    (<)
          +  → mayor que    (>)

        PALABRAS RESERVADAS:
          mostrar → imprime el valor de una variable
          ancla   → condicional if
          red     → alternativa else

        EJEMPLO BÁSICO:
          salmon p1 ~ 5.0;
          salmon p2 ~ 3.0;
          salmon r ~ p1 < p2;
          mostrar(r);

        EJEMPLO CON ANCLA / RED:
          camaron edad ~ 18;
          ancla(edad ~~ 18) {
            camaron x ~ 1;
            mostrar(x);
          } red {
            camaron y ~ 0;
            mostrar(y);
          }
        """);

        JScrollPane scrollAyuda = new JScrollPane(txtAyuda);
        scrollAyuda.setBorder(BorderFactory.createTitledBorder("Ayuda / Lenguaje"));
        scrollAyuda.setPreferredSize(new Dimension(400, 200));

        panelTabla.add(scrollAyuda, BorderLayout.SOUTH);

        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));

        txtResultados = new JTextArea();
        txtResultados.setEditable(false);
        JScrollPane scrollResultados = new JScrollPane(txtResultados);

        panelResultados.add(scrollResultados, BorderLayout.CENTER);

        JSplitPane splitSuperior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelEntrada, panelTabla);
        splitSuperior.setDividerLocation(400);

        add(splitSuperior, BorderLayout.CENTER);
        add(panelResultados, BorderLayout.SOUTH);
        panelResultados.setPreferredSize(new Dimension(800, 150));

        btnAnalizar.addActionListener(e -> analizarCodigo());

        setVisible(true);
    }

    private void analizarCodigo() {
        String codigo = txtCodigo.getText();

        try {
            ArrayList<Token> tokens = al.analizar(codigo);

            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("Token");
            modelo.addColumn("Lexema");
            modelo.addColumn("Patrón");
            modelo.addColumn("Reservada");

            for (Token t : tokens) {
                modelo.addRow(new Object[]{
                    t.tipo,
                    t.lexema,
                    obtenerPatron(t),
                    esReservada(t)
                });
            }

            tablaTokens.setModel(modelo);

            String resultado = as.analizar(tokens);
            txtResultados.setText(resultado.isEmpty() ? "Análisis correcto" : resultado);

        } catch (ErrorSemantico ex) {
            txtResultados.setText(ex.getMessage());
        }
    }

    private String obtenerPatron(Token t) {
        switch (t.tipo) {
            case "TIPO_DATO":        return "trucha|camaron|salmon";
            case "IDENTIFICADOR":    return "([a-zA-Z][a-zA-Z0-9])*";
            case "NUMERO":           return "camaron: \\d{1,10}\ncamaron: \\d{1,10}\\.\\d{1,8}";
            case "STRING":           return "\"[a-zA-Z0-9 ]*\"";
            case "OPERADOR":         return "[~<>$%]";
            case "COMPARADOR":       return "~~|!~|~-|~+|-|+";
            case "DELIMITADOR":      return "[();{}]";
            case "PALABRA_RESERVADA": return "mostrar|ancla|red";
            default:                 return "Desconocido";
        }
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
}