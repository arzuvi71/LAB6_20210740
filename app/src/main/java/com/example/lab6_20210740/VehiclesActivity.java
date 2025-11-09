package com.example.lab6_20210740;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20210740.adapters.VehicleAdapter;
import com.example.lab6_20210740.models.Vehicle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VehiclesActivity extends AppCompatActivity {

    private RecyclerView recyclerVehicles;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddVehicle;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        loadVehiclesFromFirestore();
    }

    private void initViews() {
        recyclerVehicles = findViewById(R.id.recycler_vehicles);
        emptyState = findViewById(R.id.empty_state);
        fabAddVehicle = findViewById(R.id.fab_add_vehicle);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        vehicleList = new ArrayList<>();
        vehicleAdapter = new VehicleAdapter(vehicleList);

        vehicleAdapter.setOnQRClickListener(vehicle -> {
            Intent intent = new Intent(VehiclesActivity.this, TechnicalReviewActivity.class);
            intent.putExtra(TechnicalReviewActivity.EXTRA_VEHICLE_ID, vehicle.getId());
            startActivity(intent);
        });

        vehicleAdapter.setOnEditClickListener(vehicle -> showEditVehicleDialog(vehicle));

        vehicleAdapter.setOnDeleteClickListener(vehicle -> showDeleteConfirmationDialog(vehicle));

        recyclerVehicles.setLayoutManager(new LinearLayoutManager(this));
        recyclerVehicles.setAdapter(vehicleAdapter);
    }

    private void setupFab() {
        fabAddVehicle.setOnClickListener(v -> showAddVehicleDialog());
    }

    private void loadVehiclesFromFirestore() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("vehicles")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vehicleList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Vehicle vehicle = document.toObject(Vehicle.class);
                        vehicleList.add(vehicle);
                    }
                    vehicleAdapter.notifyDataSetChanged();
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading vehicles", e);
                    Toast.makeText(this, "Error al cargar vehículos", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddVehicleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_vehicle, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText etVehicleId = dialogView.findViewById(R.id.et_vehicle_id);
        TextInputEditText etVehiclePlate = dialogView.findViewById(R.id.et_vehicle_plate);
        TextInputEditText etVehicleBrand = dialogView.findViewById(R.id.et_vehicle_brand);
        TextInputEditText etVehicleModel = dialogView.findViewById(R.id.et_vehicle_model);
        TextInputEditText etVehicleYear = dialogView.findViewById(R.id.et_vehicle_year);
        TextInputEditText etTechnicalReviewDate = dialogView.findViewById(R.id.et_technical_review_date);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Date Picker para fecha de revisión técnica
        etTechnicalReviewDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    VehiclesActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        etTechnicalReviewDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String id = etVehicleId.getText().toString().trim();
            String plate = etVehiclePlate.getText().toString().trim();
            String brand = etVehicleBrand.getText().toString().trim();
            String model = etVehicleModel.getText().toString().trim();
            String yearStr = etVehicleYear.getText().toString().trim();
            String reviewDate = etTechnicalReviewDate.getText().toString().trim();

            if (validateVehicleData(id, plate, brand, model, yearStr, reviewDate)) {
                int year = Integer.parseInt(yearStr);

                // Crear vehículo con userId
                Vehicle vehicle = new Vehicle(id, plate, brand, model, year, reviewDate);
                vehicle.setUserId(auth.getCurrentUser().getUid());

                // Guardar en Firestore
                saveVehicleToFirestore(vehicle, dialog);
            }
        });

        dialog.show();
    }

    private void saveVehicleToFirestore(Vehicle vehicle, AlertDialog dialog) {
        db.collection("vehicles")
                .document(vehicle.getId())
                .set(vehicle)
                .addOnSuccessListener(unused -> {
                    loadVehiclesFromFirestore(); // Recargar la lista
                    Toast.makeText(this, R.string.vehicle_saved, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving vehicle", e);
                    Toast.makeText(this, "Error al guardar vehículo", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateVehicleData(String id, String plate, String brand, String model, String yearStr, String reviewDate) {
        if (id.isEmpty() || plate.isEmpty() || brand.isEmpty() || model.isEmpty() || yearStr.isEmpty() || reviewDate.isEmpty()) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int year = Integer.parseInt(yearStr);
            if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR) + 1) {
                Toast.makeText(this, R.string.invalid_year, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showEditVehicleDialog(Vehicle vehicle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_vehicle, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText etVehicleId = dialogView.findViewById(R.id.et_vehicle_id);
        TextInputEditText etVehiclePlate = dialogView.findViewById(R.id.et_vehicle_plate);
        TextInputEditText etVehicleBrand = dialogView.findViewById(R.id.et_vehicle_brand);
        TextInputEditText etVehicleModel = dialogView.findViewById(R.id.et_vehicle_model);
        TextInputEditText etVehicleYear = dialogView.findViewById(R.id.et_vehicle_year);
        TextInputEditText etTechnicalReviewDate = dialogView.findViewById(R.id.et_technical_review_date);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);


        etVehicleId.setText(vehicle.getId());
        etVehicleId.setEnabled(false); // No permitir cambiar el ID
        etVehiclePlate.setText(vehicle.getPlate());
        etVehicleBrand.setText(vehicle.getBrand());
        etVehicleModel.setText(vehicle.getModel());
        etVehicleYear.setText(String.valueOf(vehicle.getYear()));
        etTechnicalReviewDate.setText(vehicle.getTechnicalReviewDate());

        // Date Picker para fecha de revisión técnica
        etTechnicalReviewDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    VehiclesActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        etTechnicalReviewDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String id = etVehicleId.getText().toString().trim();
            String plate = etVehiclePlate.getText().toString().trim();
            String brand = etVehicleBrand.getText().toString().trim();
            String model = etVehicleModel.getText().toString().trim();
            String yearStr = etVehicleYear.getText().toString().trim();
            String reviewDate = etTechnicalReviewDate.getText().toString().trim();

            if (validateVehicleData(id, plate, brand, model, yearStr, reviewDate)) {
                int year = Integer.parseInt(yearStr);

                // Actualizar vehículo
                vehicle.setPlate(plate);
                vehicle.setBrand(brand);
                vehicle.setModel(model);
                vehicle.setYear(year);
                vehicle.setTechnicalReviewDate(reviewDate);

                // Actualizar en Firestore
                updateVehicleInFirestore(vehicle, dialog);
            }
        });

        dialog.show();
    }

    private void updateVehicleInFirestore(Vehicle vehicle, AlertDialog dialog) {
        db.collection("vehicles")
                .document(vehicle.getId())
                .set(vehicle)
                .addOnSuccessListener(unused -> {
                    loadVehiclesFromFirestore(); // Recargar la lista
                    Toast.makeText(this, "Vehículo actualizado", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating vehicle", e);
                    Toast.makeText(this, "Error al actualizar vehículo", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmationDialog(Vehicle vehicle) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Vehículo")
                .setMessage("¿Estás seguro de que deseas eliminar el vehículo " + vehicle.getId() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteVehicleFromFirestore(vehicle))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteVehicleFromFirestore(Vehicle vehicle) {
        db.collection("vehicles")
                .document(vehicle.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    loadVehiclesFromFirestore();
                    Toast.makeText(this, "Vehículo eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting vehicle", e);
                    Toast.makeText(this, "Error al eliminar vehículo", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (vehicleList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerVehicles.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerVehicles.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargaar datos cuando la actividad se reanude
        loadVehiclesFromFirestore();
    }
}