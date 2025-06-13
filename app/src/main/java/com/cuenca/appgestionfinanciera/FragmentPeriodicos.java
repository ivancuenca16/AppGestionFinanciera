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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Muestra solo los movimientos cuya fecha sea estrictamente posterior a hoy.
 */
public class FragmentPeriodicos extends Fragment {

    private RecyclerView rvPeriodicos;
    private TransactionAdapter adapter;
    private List<Transaction> futureList = new ArrayList<>();
    private DatabaseReference transRef;
    private SimpleDateFormat sdf;

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
        adapter = new TransactionAdapter(futureList);
        rvPeriodicos.setAdapter(adapter);

        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        transRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                futureList.clear();

                // Calendario al inicio de mañana
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
                        Date d = sdf.parse(tx.fecha);
                        Calendar c = Calendar.getInstance();
                        c.setTime(d);
                        // Incluir solo si fecha >= mañana
                        if (!c.before(tomorrow)) {
                            futureList.add(tx);
                        }
                    } catch (ParseException e) {
                        // Si falla el parseo, la añadimos por seguridad
                        futureList.add(tx);
                    }
                }

                // Ordena ascendente por fecha
                Collections.sort(futureList, Comparator.comparing(t -> t.fecha));
                adapter.updateList(futureList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        return view;
    }
}
