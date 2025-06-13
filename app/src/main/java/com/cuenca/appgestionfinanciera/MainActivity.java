package com.cuenca.appgestionfinanciera; // ajústalo a tu paquete real

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cuenca.appgestionfinanciera.LoginActivity;
import com.cuenca.appgestionfinanciera.RegisterActivity;

/**
 * MainActivity con dos opciones: Iniciar sesión o Registrarse
 */
public class MainActivity extends AppCompatActivity {

    private Button btnIrLogin;
    private Button btnIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        btnIrLogin    = findViewById(R.id.btnIrLogin);
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        //Al hacer clic en "Iniciar sesión", vamos a LoginActivity
        btnIrLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Al hacer clic en "Registrarse", vamos a RegisterActivity
        btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
