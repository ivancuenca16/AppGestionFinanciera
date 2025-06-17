package com.cuenca.appgestionfinanciera;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class FragmentCuenta extends Fragment {

    private TextView tvNombre, tvEmail;
    private MaterialButton btnEdit, btnLogout;
    private DatabaseReference usersRef;
    private String userId;

    public FragmentCuenta() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cuenta, container, false);

        // Toolbar
        MaterialToolbar tb = v.findViewById(R.id.toolbar_cuenta);
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(tb);
        }

        tvNombre  = v.findViewById(R.id.tvNombreUser);
        tvEmail   = v.findViewById(R.id.tvEmailUser);
        btnEdit   = v.findViewById(R.id.btnEditProfile);
        btnLogout = v.findViewById(R.id.btnLogout);

        // Obtener userId de sesión
        userId = SessionManager.getUserId(requireContext());
        if (userId == null) {
            navigateToLogin();
            return v;
        }

        // Referencia Firebase
        usersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        // Carga datos
        loadUserData();

        // Editar perfil
        btnEdit.setOnClickListener( btn -> showEditDialog() );

        // Cerrar sesión
        btnLogout.setOnClickListener( btn -> {
            SessionManager.saveUserId(requireContext(), null);
            navigateToLogin();
        });

        return v;
    }

    private void loadUserData() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                User u = snap.getValue(User.class);
                if (u != null) {
                    tvNombre.setText(u.name);
                    tvEmail.setText(u.email);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {
                Toast.makeText(requireContext(),
                        "Error cargando datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog() {
        View dialogV = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null, false);
        TextInputEditText etName  = dialogV.findViewById(R.id.etDialogName);
        TextInputEditText etEmail = dialogV.findViewById(R.id.etDialogEmail);

        // Pre-llenar con valores actuales
        etName.setText(tvNombre.getText());
        etEmail.setText(tvEmail.getText());

        new AlertDialog.Builder(requireContext())
                .setTitle("Editar perfil")
                .setView(dialogV)
                .setPositiveButton("Guardar", (d, which) -> {
                    String newName  = etName.getText().toString().trim();
                    String newEmail = etEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newEmail)) {
                        Toast.makeText(getContext(),
                                "Nombre y email no pueden estar vacíos",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Actualizar en Firebase
                    Map<String,Object> updates = new HashMap<>();
                    updates.put("name", newName);
                    updates.put("email", newEmail);
                    usersRef.updateChildren(updates)
                            .addOnSuccessListener(_a -> {
                                tvNombre.setText(newName);
                                tvEmail.setText(newEmail);
                                Toast.makeText(getContext(),
                                        "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(_e ->
                                    Toast.makeText(getContext(),
                                            "Error al actualizar", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void navigateToLogin() {
        startActivity(new Intent(getActivity(), LoginActivity.class));
        requireActivity().finish();
    }
}
