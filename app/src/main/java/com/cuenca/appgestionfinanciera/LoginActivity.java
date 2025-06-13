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
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvErrorLogin;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername    = findViewById(R.id.etUsername);
        etPassword    = findViewById(R.id.etPassword);
        btnLogin      = findViewById(R.id.btnLogin);
        tvErrorLogin  = findViewById(R.id.tvErrorLogin);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnLogin.setOnClickListener(v -> {
            tvErrorLogin.setVisibility(View.GONE);
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                tvErrorLogin.setText("Usuario y contraseña son obligatorios");
                tvErrorLogin.setVisibility(View.VISIBLE);
                return;
            }

            // Buscar directamente por clave (username)
            usersRef.child(username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                tvErrorLogin.setText("Usuario no encontrado");
                                tvErrorLogin.setVisibility(View.VISIBLE);
                                return;
                            }
                            User user = snapshot.getValue(User.class);
                            if (user != null && user.password.equals(password)) {
                                // Guardar sesión y abrir HomeActivity
                                SessionManager.saveUserId(LoginActivity.this, user.id);
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                tvErrorLogin.setText("Contraseña incorrecta");
                                tvErrorLogin.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvErrorLogin.setText("Error de conexión");
                            tvErrorLogin.setVisibility(View.VISIBLE);
                        }
                    });
        });
    }
}
