package com.cuenca.appgestionfinanciera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import org.json.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class FragmentEstadisticas extends Fragment {

    private WebView webView;
    private Spinner spinnerRango;
    private DatabaseReference transRef;
    private List<Transaction> allTransactions = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public FragmentEstadisticas() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_estadisticas, container, false);

        spinnerRango = v.findViewById(R.id.spinnerRango);
        webView      = v.findViewById(R.id.webviewStats);

        // Configurar WebView
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                render();  // dibuja al cargar
            }
        });
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public String getStatsData() {
                return buildStatsJson(spinnerRango.getSelectedItemPosition());
            }
        }, "statsData");
        webView.loadUrl("file:///android_asset/estadisticas.html");

        // Firebase → transactions/{userId}
        String userId = SessionManager.getUserId(requireContext());
        transRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(userId);
        transRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                allTransactions.clear();
                for (DataSnapshot ds: snap.getChildren()) {
                    Transaction tx = ds.getValue(Transaction.class);
                    if (tx != null) allTransactions.add(tx);
                }
                render();
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { }
        });

        // Al cambiar rango, recarga gráficos
        spinnerRango.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> a, View v, int p, long id) {
                render();
            }
            @Override public void onNothingSelected(AdapterView<?> a) {}
        });

        return v;
    }

    private void render() {
        webView.post(() ->
                webView.evaluateJavascript("renderStats();", null)
        );
    }

    /**
     * Crea el JSON según el rango:
     * 0=Día,1=Semana,2=Mes,3=Total
     */
    private String buildStatsJson(int rango) {
        float sumIn=0, sumOut=0;
        Calendar now = Calendar.getInstance();
        for (Transaction tx: allTransactions) {
            // filtrado según rango
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(sdf.parse(tx.fecha));
                boolean include=false;
                switch(rango) {
                    case 0: // hoy
                        include = now.get(Calendar.YEAR)==c.get(Calendar.YEAR)
                                && now.get(Calendar.DAY_OF_YEAR)==c.get(Calendar.DAY_OF_YEAR);
                        break;
                    case 1: // últimos 7 días
                        long diff = now.getTimeInMillis()-c.getTimeInMillis();
                        include = diff >=0 && diff < 1000L*60*60*24*7;
                        break;
                    case 2: // mismo mes/año
                        include = now.get(Calendar.YEAR)==c.get(Calendar.YEAR)
                                && now.get(Calendar.MONTH)==c.get(Calendar.MONTH);
                        break;
                    case 3: // todo
                        include = true;
                        break;
                }
                if (!include) continue;
            } catch (Exception e){ continue; }

            if ("ingreso".equalsIgnoreCase(tx.tipo)) sumIn += tx.importe;
            else sumOut += tx.importe;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("sumIn", sumIn);
            o.put("sumOut", sumOut);
        } catch (JSONException ignored){}
        return o.toString();
    }
}
