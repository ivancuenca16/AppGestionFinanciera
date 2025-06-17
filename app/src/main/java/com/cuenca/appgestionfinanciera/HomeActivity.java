package com.cuenca.appgestionfinanciera;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FragmentManager fragmentManager;

    private FragmentAgrega fragmentAgrega;
    private FragmentLista fragmentLista;
    private FragmentPeriodicos fragmentPeriodicos;
    private FragmentEstadisticas fragmentEstadisticas;
    private FragmentCuenta fragmentCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fragmentManager      = getSupportFragmentManager();
        fragmentAgrega       = new FragmentAgrega();
        fragmentLista        = new FragmentLista();
        fragmentPeriodicos   = new FragmentPeriodicos();
        fragmentEstadisticas = new FragmentEstadisticas();
        fragmentCuenta       = new FragmentCuenta();

        bottomNav = findViewById(R.id.bottom_navigation);

        // Cargar "Listar" por defecto
        fragmentManager.beginTransaction()
                .replace(R.id.containerFragments, fragmentLista)
                .commit();
        bottomNav.setSelectedItemId(R.id.nav_listar);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment seleccionado = null;

            int id = item.getItemId();
            if (id == R.id.nav_agregar) {
                seleccionado = fragmentAgrega;
            } else if (id == R.id.nav_listar) {
                seleccionado = fragmentLista;
            } else if (id == R.id.nav_periodicos) {
                seleccionado = fragmentPeriodicos;
            } else if (id == R.id.nav_estadisticas) {
                seleccionado = fragmentEstadisticas;
            }     else if (id == R.id.nav_cuenta)
                seleccionado = fragmentCuenta;


            if (seleccionado != null) {
                fragmentManager.beginTransaction()
                        .replace(R.id.containerFragments, seleccionado)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
