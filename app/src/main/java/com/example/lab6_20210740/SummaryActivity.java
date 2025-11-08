package com.example.lab6_20210740;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20210740.models.FuelRecord;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {

    private BarChart barChart;
    private PieChart pieChart;
    private LinearLayout emptyState;
    private LinearLayout chartsContainer;
    private List<FuelRecord> fuelRecordList;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        //Inicialización
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        loadDataFromFirestore();
    }

    private void initViews() {
        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        emptyState = findViewById(R.id.empty_state);
        chartsContainer = findViewById(R.id.charts_container);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    // Datos de Firestore
    private void loadDataFromFirestore() {
        if (auth.getCurrentUser() == null) {
            updateUI();
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("fuelRecords")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fuelRecordList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FuelRecord record = document.toObject(FuelRecord.class);
                        fuelRecordList.add(record);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading fuel records for summary", e);
                    updateUI();
                });
    }

    private void updateUI() {
        if (fuelRecordList == null || fuelRecordList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            chartsContainer.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            chartsContainer.setVisibility(View.VISIBLE);
            setupBarChart();
            setupPieChart();
        }
    }

    private void setupBarChart() {
        Map<String, Float> monthlyConsumption = calculateMonthlyConsumption();

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Float> entry : monthlyConsumption.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Litros");
        dataSet.setColor(Color.parseColor("#63B4A9"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Configuración del gráfico
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.animateY(1000);

        // Configuración del eje X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        // Configuración del eje Y
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.invalidate();
    }

    private void setupPieChart() {
        Map<String, Float> fuelTypeConsumption = calculateFuelTypeConsumption();

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : fuelTypeConsumption.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Colores personalizados
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#63B4A9")); // Gasolina
        colors.add(Color.parseColor("#009688")); // GLP
        colors.add(Color.parseColor("#4DB6AC")); // GNV
        dataSet.setColors(colors);

        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Configuración del gráfico
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(1000);
        pieChart.getLegend().setEnabled(true);

        pieChart.invalidate();
    }

    private Map<String, Float> calculateMonthlyConsumption() {
        Map<String, Float> monthlyData = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        if (fuelRecordList == null) return monthlyData;

        for (FuelRecord record : fuelRecordList) {
            try {
                Date date = sdf.parse(record.getDate());
                if (date != null) {
                    String month = monthFormat.format(date);
                    float currentLiters = monthlyData.getOrDefault(month, 0f);
                    monthlyData.put(month, currentLiters + (float) record.getLiters());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return monthlyData;
    }

    private Map<String, Float> calculateFuelTypeConsumption() {
        Map<String, Float> fuelTypeData = new HashMap<>();

        if (fuelRecordList == null) return fuelTypeData;

        for (FuelRecord record : fuelRecordList) {
            String fuelType = record.getFuelType();
            float currentLiters = fuelTypeData.getOrDefault(fuelType, 0f);
            fuelTypeData.put(fuelType, currentLiters + (float) record.getLiters());
        }

        return fuelTypeData;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando la actividad se reanude
        loadDataFromFirestore();
    }
}