package com.cuenca.appgestionfinanciera;

import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final Context context;
    private final List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> list) {
        this.context = context;
        this.transactionList = list;
    }

    /**
     * Actualiza la lista mostrada y refresca el RecyclerView.
     */
    public void updateList(List<Transaction> newList) {
        transactionList.clear();
        transactionList.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_transaccion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactionList.get(position);
        holder.tvCategoria.setText(tx.categoria);
        holder.tvDescripcion.setText(tx.descripcion == null ? "" : tx.descripcion);
        holder.tvFecha.setText(tx.fecha);
        String sign = tx.tipo.equalsIgnoreCase("ingreso") ? "+ " : "- ";
        holder.tvImporte.setText(String.format(Locale.getDefault(), "%s€ %.2f", sign, tx.importe));
        int color = tx.tipo.equalsIgnoreCase("ingreso")
                ? context.getColor(R.color.green)
                : context.getColor(R.color.red);
        holder.tvImporte.setTextColor(color);

        holder.ivDelete.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;
            Transaction txToDelete = transactionList.get(adapterPosition);
            String userId = SessionManager.getUserId(context);
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("transactions")
                    .child(userId)
                    .child(txToDelete.id);
            ref.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        transactionList.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        notifyItemRangeChanged(adapterPosition, transactionList.size());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context,
                            "Error al eliminar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoria, tvDescripcion, tvFecha, tvImporte;
        ImageView ivDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaItem);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionItem);
            tvFecha = itemView.findViewById(R.id.tvFechaItem);
            tvImporte = itemView.findViewById(R.id.tvImporteItem);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
