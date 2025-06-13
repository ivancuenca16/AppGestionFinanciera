package com.cuenca.appgestionfinanciera;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;
    private SimpleDateFormat sdfFrom = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfTo   = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaccion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactionList.get(position);

        // 1) Categoría
        holder.tvCategoria.setText(tx.categoria);

        // 2) Fecha: transformar de “yyyy-MM-dd” a “dd/MM/yyyy”
        try {
            String fechaFormateada = sdfTo.format(sdfFrom.parse(tx.fecha));
            holder.tvFecha.setText(fechaFormateada);
        } catch (ParseException e) {
            holder.tvFecha.setText(tx.fecha);
        }

        // 3) Descripción (si está vacía, ocultar el TextView)
        if (tx.descripcion == null || tx.descripcion.trim().isEmpty()) {
            holder.tvDescripcion.setVisibility(View.GONE);
        } else {
            holder.tvDescripcion.setVisibility(View.VISIBLE);
            holder.tvDescripcion.setText(tx.descripcion);
        }

        // 4) Importe: con signo y color según tipo
        String importeStr = currencyFormatter.format(tx.importe);
        if ("ingreso".equalsIgnoreCase(tx.tipo)) {
            holder.tvImporte.setText("+ " + importeStr);
            holder.tvImporte.setTextColor(holder.itemView.getResources().getColor(R.color.green));
            holder.indicadorTipo.setBackgroundTintList(
                    holder.itemView.getResources().getColorStateList(R.color.green));
        } else {
            holder.tvImporte.setText("- " + importeStr);
            holder.tvImporte.setTextColor(holder.itemView.getResources().getColor(R.color.red));
            holder.indicadorTipo.setBackgroundTintList(
                    holder.itemView.getResources().getColorStateList(R.color.red));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    // Actualiza la lista (se dará en FragmentLista cada vez que cambien los filtros)
    public void updateList(List<Transaction> nuevaLista) {
        this.transactionList = nuevaLista;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View indicadorTipo;
        TextView tvCategoria, tvFecha, tvDescripcion, tvImporte;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            indicadorTipo   = itemView.findViewById(R.id.viewTipoIndicador);
            tvCategoria     = itemView.findViewById(R.id.tvCategoriaItem);
            tvFecha         = itemView.findViewById(R.id.tvFechaItem);
            tvDescripcion   = itemView.findViewById(R.id.tvDescripcionItem);
            tvImporte       = itemView.findViewById(R.id.tvImporteItem);
        }
    }
}
