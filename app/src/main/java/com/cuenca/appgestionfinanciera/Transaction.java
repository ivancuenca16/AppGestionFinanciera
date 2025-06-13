package com.cuenca.appgestionfinanciera;

/**
 * POJO para una transacción (gasto o ingreso).
 * Debe coincidir con lo que guardas en Firebase Realtime Database
 * (nodos bajo “transactions”).
 */
public class Transaction {
    public String id;           // ID único (UUID)
    public String tipo;         // "ingreso" o "gasto"
    public String categoria;    // p. ej. "Alimentos", "Transporte", etc.
    public String descripcion;  // texto libre (puede estar vacío)
    public double importe;      // importe en euros
    public String fecha;        // formato "yyyy-MM-dd"
    public boolean periodico;   // true si es gasto/ingreso periódico

    // Constructor vacío requerido por Firebase
    public Transaction() { }

    // Constructor con todos los campos
    public Transaction(String id, String tipo, String categoria,
                       String descripcion, double importe,
                       String fecha, boolean periodico) {
        this.id = id;
        this.tipo = tipo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.importe = importe;
        this.fecha = fecha;
        this.periodico = periodico;
    }
}

