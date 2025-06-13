package com.cuenca.appgestionfinanciera;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.*;
import com.google.android.material.textfield.*;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class FragmentAgrega extends Fragment {

    private MaterialButtonToggleGroup toggleTipo;
    private MaterialButton btnIngreso, btnGasto;
    private TextInputLayout tilImporte, tilDescripcion;
    private TextInputEditText etImporte, etDescripcion;
    private Spinner spinnerCategoria;
    private MaterialButton btnSeleccionarFecha, btnGuardarMovimiento;
    private TextView tvFechaSeleccionada, tvMensajeAgrega;

    private Calendar calendar;
    private SimpleDateFormat sdf;

    private DatabaseReference transRef;
    private String userId;

    public FragmentAgrega() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agrega, container, false);

        // 1) Recuperar sesión
        userId = SessionManager.getUserId(requireContext());
        if (userId == null) {
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
            return view;
        }

        // 2) Instanciar vistas
        toggleTipo           = view.findViewById(R.id.toggleTipo);
        btnIngreso           = view.findViewById(R.id.btnIngreso);
        btnGasto             = view.findViewById(R.id.btnGasto);
        tilImporte           = view.findViewById(R.id.tilImporte);
        etImporte            = view.findViewById(R.id.etImporte);
        spinnerCategoria     = view.findViewById(R.id.spinnerCategoria);
        tilDescripcion       = view.findViewById(R.id.tilDescripcion);
        etDescripcion        = view.findViewById(R.id.etDescripcion);
        btnSeleccionarFecha  = view.findViewById(R.id.btnSeleccionarFecha);
        tvFechaSeleccionada  = view.findViewById(R.id.tvFechaSeleccionada);
        btnGuardarMovimiento = view.findViewById(R.id.btnGuardarMovimiento);
        tvMensajeAgrega      = view.findViewById(R.id.tvMensajeAgrega);

        // 3) Fecha por defecto
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvFechaSeleccionada.setText(sdf.format(calendar.getTime()));

        // 4) Spinner de categorías
        ArrayAdapter<CharSequence> adapterCat = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.categorias_array,
                android.R.layout.simple_spinner_item
        );
        adapterCat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCat);

        // 5) Firebase → transactions/{userId}
        transRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);

        // 6) DatePicker
        btnSeleccionarFecha.setOnClickListener(v -> {
            int y = calendar.get(Calendar.YEAR),
                    m = calendar.get(Calendar.MONTH),
                    d = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(requireContext(), (dp, yy, mm, dd) -> {
                calendar.set(yy, mm, dd);
                tvFechaSeleccionada.setText(sdf.format(calendar.getTime()));
            }, y, m, d).show();
        });

        // 7) Guardar movimiento
        btnGuardarMovimiento.setOnClickListener(v -> {
            tvMensajeAgrega.setVisibility(View.GONE);
            tilImporte.setError(null);

            String importeStr  = etImporte.getText().toString().trim();
            String descripcion = etDescripcion.getText().toString().trim();
            String categoria   = spinnerCategoria.getSelectedItem().toString();
            String fecha       = sdf.format(calendar.getTime());
            String tipo        = (toggleTipo.getCheckedButtonId() == R.id.btnIngreso)
                    ? "ingreso" : "gasto";

            if (TextUtils.isEmpty(importeStr)) {
                tilImporte.setError("Ingresa un importe");
                return;
            }
            double importe;
            try {
                importe = Double.parseDouble(importeStr);
                if (importe <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                tilImporte.setError("Formato inválido");
                return;
            }

            String idTrans = transRef.push().getKey();
            if (idTrans == null) {
                tvMensajeAgrega.setText("Error al generar ID");
                tvMensajeAgrega.setVisibility(View.VISIBLE);
                return;
            }

            Transaction t = new Transaction(
                    idTrans, tipo, categoria, descripcion,
                    importe, fecha, false
            );
            transRef.child(idTrans)
                    .setValue(t)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(requireContext(),
                                "Guardado correctamente",
                                Toast.LENGTH_SHORT).show();
                        etImporte.setText("");
                        etDescripcion.setText("");
                        spinnerCategoria.setSelection(0);
                        toggleTipo.check(R.id.btnIngreso);
                        calendar = Calendar.getInstance();
                        tvFechaSeleccionada.setText(sdf.format(calendar.getTime()));
                    })
                    .addOnFailureListener(e -> {
                        tvMensajeAgrega.setText("Error: " + e.getMessage());
                        tvMensajeAgrega.setVisibility(View.VISIBLE);
                    });
        });

        return view;
    }
}
