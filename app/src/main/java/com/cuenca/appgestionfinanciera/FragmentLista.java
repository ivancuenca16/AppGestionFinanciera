package com.cuenca.appgestionfinanciera;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FragmentLista extends Fragment {


    private TextInputEditText etBuscar;
    private Spinner spinnerTipoFiltro, spinnerCategoriaFiltro;
    private RecyclerView rvTransacciones;
    private TextView tvSinResultados;
    private TextView tvIngresosMes, tvGastosMes, tvTotalMes;

    private DatabaseReference transRef;
    private List<Transaction> allTransactions = new ArrayList<>();
    private TransactionAdapter adapter;
    private SimpleDateFormat sdf;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lista, container, false);

        // 1) Sesión
        String userId = SessionManager.getUserId(requireContext());
        if (userId == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
            return v;
        }

        // 2) Vistas
        etBuscar               = v.findViewById(R.id.etBuscar);
        spinnerTipoFiltro      = v.findViewById(R.id.spinnerTipoFiltro);
        spinnerCategoriaFiltro = v.findViewById(R.id.spinnerCategoriaFiltro);
        rvTransacciones        = v.findViewById(R.id.rvTransacciones);
        tvSinResultados        = v.findViewById(R.id.tvSinResultados);
        tvIngresosMes          = v.findViewById(R.id.tvIngresosMes);
        tvGastosMes            = v.findViewById(R.id.tvGastosMes);
        tvTotalMes             = v.findViewById(R.id.tvTotalMes);

        // 3) RecyclerView + Adapter
        rvTransacciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter(requireContext(), new ArrayList<>());
        rvTransacciones.setAdapter(adapter);

        // 4) Spinners
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(
                requireContext(), R.array.filtro_tipo,
                android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoFiltro.setAdapter(adapterTipo);

        ArrayAdapter<CharSequence> adapterCat = ArrayAdapter.createFromResource(
                requireContext(), R.array.categorias_array,
                android.R.layout.simple_spinner_item);
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoriaFiltro.setAdapter(adapterCat);

        // 5) Formatter fechas
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 6) Firebase
        transRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        // 7) Listeners filtros y búsqueda
        etBuscar.setOnEditorActionListener((tv, act, ev) -> {
            if (act == EditorInfo.IME_ACTION_SEARCH) {
                applyFilters();
                return true;
            }
            return false;
        });
        AdapterView.OnItemSelectedListener filtrosL = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View w, int pos, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        spinnerTipoFiltro.setOnItemSelectedListener(filtrosL);
        spinnerCategoriaFiltro.setOnItemSelectedListener(filtrosL);

        // 8) Carga inicial y ordenación una sola vez
        transRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                allTransactions.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    Transaction tx = ds.getValue(Transaction.class);
                    if (tx != null) allTransactions.add(tx);
                }
                // Ordenar allTransactions de más reciente a más antiguo
                Collections.sort(allTransactions, (t1, t2) -> {
                    try {
                        Date d1 = sdf.parse(t1.fecha);
                        Date d2 = sdf.parse(t2.fecha);
                        return d2.compareTo(d1);
                    } catch (ParseException e) {
                        return t2.fecha.compareTo(t1.fecha);
                    }
                });
                // Actualizar indicadores y lista
                updateMonthlyTotals();
                applyFilters();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) { }
        });

        return v;
    }

    /** Filtra sin volver a ordenar; mantiene el orden original (nuevo arriba). */
    private void applyFilters() {
        String txt    = etBuscar.getText().toString().trim().toLowerCase();
        int posTipo   = spinnerTipoFiltro.getSelectedItemPosition();
        int posCat    = spinnerCategoriaFiltro.getSelectedItemPosition();
        String selTipo = spinnerTipoFiltro.getSelectedItem().toString();
        String selCat  = spinnerCategoriaFiltro.getSelectedItem().toString();

        List<Transaction> resultado = new ArrayList<>();
        for (Transaction tx : allTransactions) {
            boolean okTxt  = txt.isEmpty()
                    || tx.categoria.toLowerCase().contains(txt)
                    || (tx.descripcion != null
                    && tx.descripcion.toLowerCase().contains(txt));
            boolean okTipo = posTipo == 0
                    || tx.tipo.equalsIgnoreCase(selTipo);
            boolean okCat  = posCat == 0
                    || tx.categoria.equalsIgnoreCase(selCat);

            if (okTxt && okTipo && okCat) {
                resultado.add(tx);
            }
        }

        adapter.updateList(resultado);
        tvSinResultados.setVisibility(resultado.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Calcula y muestra totales del mes actual. */
    private void updateMonthlyTotals() {
        Calendar ahora = Calendar.getInstance();
        int año = ahora.get(Calendar.YEAR);
        int mes = ahora.get(Calendar.MONTH);

        double sumIn = 0, sumOut = 0;
        for (Transaction tx : allTransactions) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(tx.fecha));
                if (cal.get(Calendar.YEAR) == año
                        && cal.get(Calendar.MONTH) == mes) {
                    if ("ingreso".equalsIgnoreCase(tx.tipo)) sumIn += tx.importe;
                    else sumOut += tx.importe;
                }
            } catch (ParseException e) { }
        }
        double net = sumIn - sumOut;
        tvIngresosMes.setText(String.format(Locale.getDefault(),"€ %.2f", sumIn));
        tvGastosMes  .setText(String.format(Locale.getDefault(),"€ %.2f", sumOut));
        tvTotalMes   .setText(String.format(Locale.getDefault(),"€ %.2f", net));
    }
}
