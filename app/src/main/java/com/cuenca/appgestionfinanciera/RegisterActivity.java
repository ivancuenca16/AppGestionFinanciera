package com.cuenca.appgestionfinanciera;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNewUsername;
    private EditText etNewPassword;
    private EditText etNewNombre;
    private EditText etNewEmail;
    private Button btnRegister;
    private TextView tvErrorRegistro;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNewUsername  = findViewById(R.id.etNewUsername);
        etNewPassword  = findViewById(R.id.etNewPassword);
        etNewNombre    = findViewById(R.id.etNewNombre);
        etNewEmail     = findViewById(R.id.etNewEmail);
        btnRegister    = findViewById(R.id.btnRegister);
        tvErrorRegistro= findViewById(R.id.tvErrorRegistro);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(v -> {
            tvErrorRegistro.setVisibility(View.GONE);
            String username = etNewUsername.getText().toString().trim();
            String password = etNewPassword.getText().toString().trim();
            String nombre   = etNewNombre.getText().toString().trim();
            String email    = etNewEmail.getText().toString().trim();

            if (TextUtils.isEmpty(username)
                    || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(nombre)
                    || TextUtils.isEmpty(email)) {
                tvErrorRegistro.setText("Todos los campos son obligatorios");
                tvErrorRegistro.setVisibility(View.VISIBLE);
                return;
            }

            // Comprobar si el usuario ya existe
            usersRef.child(username)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            if (snap.exists()) {
                                tvErrorRegistro.setText("El usuario ya existe");
                                tvErrorRegistro.setVisibility(View.VISIBLE);
                                return;
                            }
                            // Crear y guardar nuevo usuario
                            User newUser = new User(username, nombre, email, password);
                            usersRef.child(username)
                                    .setValue(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        // Guardar sesión y abrir HomeActivity
                                        SessionManager.saveUserId(RegisterActivity.this, username);
                                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        tvErrorRegistro.setText("Error: " + e.getMessage());
                                        tvErrorRegistro.setVisibility(View.VISIBLE);
                                    });
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvErrorRegistro.setText("Error de conexión");
                            tvErrorRegistro.setVisibility(View.VISIBLE);
                        }
                    });
        });
    }
}
