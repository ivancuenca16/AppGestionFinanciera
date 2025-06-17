package com.cuenca.appgestionfinanciera;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Muestra solo los movimientos con fecha > hoy.
 */
public class FragmentPeriodicos extends Fragment {

    private RecyclerView rvPeriodicos;
    private TransactionAdapter adapter;
    private List<Transaction> futureList = new ArrayList<>();
    private DatabaseReference transRef;
    private SimpleDateFormat sdf;

    public FragmentPeriodicos() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_periodicos, container, false);

        // 1) Verificar sesión
        String userId = SessionManager.getUserId(requireContext());
        if (userId == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
            return view;
        }

        // 2) RecyclerView setup
        rvPeriodicos = view.findViewById(R.id.rvPeriodicos);
        rvPeriodicos.setLayoutManager(new LinearLayoutManager(requireContext()));
        // <-- Aquí pasamos el Context y la lista
        adapter = new TransactionAdapter(requireContext(), futureList);
        rvPeriodicos.setAdapter(adapter);

        // 3) Formatter de fecha
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 4) Referencia a /transactions/{userId}
        transRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        // 5) Listener para cargar solo futuras (fecha > hoy)
        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                futureList.clear();

                // Definir mañana a 00:00
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.set(Calendar.HOUR_OF_DAY, 0);
                tomorrow.set(Calendar.MINUTE, 0);
                tomorrow.set(Calendar.SECOND, 0);
                tomorrow.set(Calendar.MILLISECOND, 0);
                tomorrow.add(Calendar.DAY_OF_MONTH, 1);

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Transaction tx = ds.getValue(Transaction.class);
                    if (tx == null) continue;
                    try {
                        if (sdf.parse(tx.fecha).getTime() >= tomorrow.getTimeInMillis()) {
                            futureList.add(tx);
                        }
                    } catch (Exception e) {
                        futureList.add(tx);
                    }
                }

                // Ordenar y notificar
                Collections.sort(futureList, Comparator.comparing(t -> t.fecha));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        return view;
    }
}
