package com.cuenca.appgestionfinanciera;

import android.os.Bundle;
import android.view.*;
import android.webkit.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.*;
import org.json.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class FragmentEstadisticas extends Fragment {

    private WebView webView;
    private DatabaseReference transRef;
    private List<Transaction> allTransactions = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_estadisticas, container, false);

        webView = v.findViewById(R.id.webviewStats);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view, String url) {
                webView.evaluateJavascript("renderCharts();", null);
            }
        });

        // Exponer getStatsData() a JS
        webView.addJavascriptInterface(new Object(){
            @JavascriptInterface
            public String getStatsData() {
                return buildStatsJson();
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
                for (DataSnapshot ds : snap.getChildren()) {
                    Transaction tx = ds.getValue(Transaction.class);
                    if (tx != null) allTransactions.add(tx);
                }
                // Refrescar gráfico
                webView.post(() -> webView.evaluateJavascript("renderCharts();", null));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) { }
        });

        return v;
    }

    /**
     * JSON con:
     *  - sumIn7, sumOut7: ingresos/gastos últimos 7 días
     *  - labels (["Mar","Abr","May","Jun"]) y savings ([...]) para la barra
     */
    private String buildStatsJson() {
        Calendar now = Calendar.getInstance();
        long cutoff = now.getTimeInMillis() - 7L*24*60*60*1000;

        double sumIn7 = 0, sumOut7 = 0;
        // datos de ahorro mensual
        int year = now.get(Calendar.YEAR);
        int[] meses = {Calendar.MARCH, Calendar.APRIL, Calendar.MAY, Calendar.JUNE};
        String[] labels = {"Mar","Abr","May","Jun"};
        double[] savings = new double[labels.length];

        for (Transaction tx : allTransactions) {
            try {
                Date d = sdf.parse(tx.fecha);
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                boolean isIngreso = "ingreso".equalsIgnoreCase(tx.tipo);

                // Últimos 7 días para el pie
                if (d.getTime() >= cutoff) {
                    if (isIngreso) sumIn7 += tx.importe;
                    else sumOut7 += tx.importe;
                }

                // Ahorro por mes (si es este año)
                if (c.get(Calendar.YEAR) == year) {
                    for (int i = 0; i < meses.length; i++) {
                        if (c.get(Calendar.MONTH) == meses[i]) {
                            savings[i] += isIngreso ? tx.importe : -tx.importe;
                        }
                    }
                }

            } catch (Exception ignored) {}
        }

        try {
            JSONObject root = new JSONObject();
            // Pie últimos 7 días
            root.put("sumIn7", sumIn7);
            root.put("sumOut7", sumOut7);
            // Barra mensual
            JSONArray jLabels = new JSONArray();
            for (String l : labels) jLabels.put(l);
            root.put("labels", jLabels);
            JSONArray jSav = new JSONArray();
            for (double v : savings) jSav.put(v);
            root.put("savings", jSav);

            return root.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
