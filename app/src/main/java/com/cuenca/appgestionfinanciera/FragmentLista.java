package com.cuenca.appgestionfinanciera;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;

import java.util.*;

public class FragmentLista extends Fragment {

    private TextInputEditText etBuscar;
    private Spinner spinnerTipoFiltro, spinnerCategoriaFiltro;
    private RecyclerView rvTransacciones;
    private TextView tvSinResultados;

    private DatabaseReference transRef;
    private List<Transaction> allTransactions = new ArrayList<>();
    private TransactionAdapter adapter;

    public FragmentLista() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista, container, false);

        // 1) Sesión
        String userId = SessionManager.getUserId(requireContext());
        if (userId == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
            return view;
        }

        // 2) Vistas
        etBuscar              = view.findViewById(R.id.etBuscar);
        spinnerTipoFiltro     = view.findViewById(R.id.spinnerTipoFiltro);
        spinnerCategoriaFiltro= view.findViewById(R.id.spinnerCategoriaFiltro);
        rvTransacciones       = view.findViewById(R.id.rvTransacciones);
        tvSinResultados       = view.findViewById(R.id.tvSinResultados);

        // 3) RecyclerView + Adapter
        rvTransacciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(new ArrayList<>());
        rvTransacciones.setAdapter(adapter);

        // 4) Spinners
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(
                requireContext(), R.array.filtro_tipo, android.R.layout.simple_spinner_item
        );
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoFiltro.setAdapter(adapterTipo);

        ArrayAdapter<CharSequence> adapterCat = ArrayAdapter.createFromResource(
                requireContext(), R.array.categorias_array, android.R.layout.simple_spinner_item
        );
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaFiltro.setAdapter(adapterCat);

        // 5) Firebase transactions
        transRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        // 6) Listeners para filtrar
        etBuscar.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilters();
                return true;
            }
            return false;
        });
        spinnerTipoFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        spinnerCategoriaFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // 7) Leer datos
        transRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                allTransactions.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    Transaction tx = ds.getValue(Transaction.class);
                    if (tx != null) allTransactions.add(tx);
                }
                applyFilters();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) { }
        });

        return view;
    }

    private void applyFilters() {
        String textoBuscar      = etBuscar.getText().toString().trim().toLowerCase();
        String tipoSeleccionado = spinnerTipoFiltro.getSelectedItem().toString();
        String catSeleccionada  = spinnerCategoriaFiltro.getSelectedItem().toString();

        List<Transaction> filtered = new ArrayList<>();
        for (Transaction tx : allTransactions) {
            boolean matchesText = textoBuscar.isEmpty()
                    || tx.categoria.toLowerCase().contains(textoBuscar)
                    || (tx.descripcion != null
                    && tx.descripcion.toLowerCase().contains(textoBuscar));
            boolean matchesTipo = tipoSeleccionado.equals("Todos")
                    || tx.tipo.equalsIgnoreCase(tipoSeleccionado);
            boolean matchesCat  = catSeleccionada.equals("Todas")
                    || tx.categoria.equalsIgnoreCase(catSeleccionada);

            if (matchesText && matchesTipo && matchesCat) {
                filtered.add(tx);
            }
        }

        adapter.updateList(filtered);
        tvSinResultados.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
