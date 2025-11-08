package com.example.lab6_20210740;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab6_20210740.models.FuelRecord;
import com.example.lab6_20210740.models.Vehicle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TechnicalReviewActivity extends AppCompatActivity {

    public static final String EXTRA_VEHICLE_ID = "vehicle_id";

    private TextView tvVehicleId;
    private TextView tvPlate;
    private TextView tvBrandModel;
    private TextView tvLastReview;
    private TextView tvLastKilometers;
    private ImageView ivQrCode;

    private Vehicle vehicle;
    private double lastKilometers;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technical_review);

        // Inicialización
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        loadVehicleDataFromFirestore();
    }

    private void initViews() {
        tvVehicleId = findViewById(R.id.tv_vehicle_id);
        tvPlate = findViewById(R.id.tv_plate);
        tvBrandModel = findViewById(R.id.tv_brand_model);
        tvLastReview = findViewById(R.id.tv_last_review);
        tvLastKilometers = findViewById(R.id.tv_last_kilometers);
        ivQrCode = findViewById(R.id.iv_qr_code);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    //Uso de Firestore
    private void loadVehicleDataFromFirestore() {
        String vehicleId = getIntent().getStringExtra(EXTRA_VEHICLE_ID);

        if (vehicleId != null && auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();

            // Buscar el vehículo en Firestore
            db.collection("vehicles")
                    .document(vehicleId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            vehicle = documentSnapshot.toObject(Vehicle.class);

                            // Verificar que el vehículo pertenezca al usuario actual
                            if (vehicle != null && vehicle.getUserId().equals(currentUserId)) {
                                // 🔥 Obtener el último kilometraje desde Firestore
                                loadLastKilometersFromFirestore(vehicleId);

                                // Mostrar información del vehículo
                                tvVehicleId.setText(vehicle.getId());
                                tvPlate.setText(vehicle.getPlate());
                                tvBrandModel.setText(vehicle.getBrand() + " " + vehicle.getModel());
                                tvLastReview.setText(vehicle.getTechnicalReviewDate());
                            } else {
                                Toast.makeText(this, "Vehículo no encontrado", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "Vehículo no encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error loading vehicle", e);
                        Toast.makeText(this, "Error al cargar vehículo", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "Error al cargar vehículo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Obtener último kilometraje desde Firestore
    private void loadLastKilometersFromFirestore(String vehicleId) {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("fuelRecords")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("vehicleId", vehicleId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double maxKilometers = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FuelRecord record = document.toObject(FuelRecord.class);
                        if (record.getCurrentKilometers() > maxKilometers) {
                            maxKilometers = record.getCurrentKilometers();
                        }
                    }

                    lastKilometers = maxKilometers;

                    // Actualizar UI y generar QR
                    if (lastKilometers > 0) {
                        tvLastKilometers.setText(String.format(Locale.getDefault(), "%.0f km", lastKilometers));
                    } else {
                        tvLastKilometers.setText(getString(R.string.sin_kilometraje));
                    }

                    generateQRCode();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading kilometers", e);
                    tvLastKilometers.setText(getString(R.string.sin_kilometraje));
                    generateQRCode();
                });
    }

    private void generateQRCode() {
        if (vehicle == null) {
            return;
        }

        // Crear el contenido del QR con la información del vehículo
        String qrContent = createQRContent();

        try {
            // Generar el código QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrContent,
                    BarcodeFormat.QR_CODE,
                    512,
                    512
            );

            // Convertir BitMatrix a Bitmap
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Mostrar el QR en el ImageView
            ivQrCode.setImageBitmap(bitmap);

            Toast.makeText(this, R.string.qr_generado, Toast.LENGTH_SHORT).show();

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar código QR", Toast.LENGTH_SHORT).show();
        }
    }

    private String createQRContent() {
        // Formato del contenido del QR:
        StringBuilder content = new StringBuilder();

        content.append("REVISIÓN TÉCNICA VEHICULAR\n");
        content.append("==========================\n\n");
        content.append("Placa: ").append(vehicle.getPlate()).append("\n");
        content.append("Vehículo: ").append(vehicle.getBrand()).append(" ").append(vehicle.getModel()).append("\n");
        content.append("Año: ").append(vehicle.getYear()).append("\n");
        content.append("Última Revisión: ").append(vehicle.getTechnicalReviewDate()).append("\n");

        if (lastKilometers > 0) {
            content.append("Kilometraje Actual: ").append(String.format(Locale.getDefault(), "%.0f km", lastKilometers)).append("\n");
        } else {
            content.append("Kilometraje Actual: No registrado\n");
        }

        content.append("\n==========================\n");
        content.append("FuelMonitor App");

        return content.toString();
    }
}