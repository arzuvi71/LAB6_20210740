package com.example.lab6_20210740;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20210740.adapters.FuelRecordAdapter;
import com.example.lab6_20210740.models.FuelRecord;
import com.example.lab6_20210740.models.Vehicle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class FuelRecordsActivity extends AppCompatActivity {

    private RecyclerView recyclerFuelRecords;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddRecord;
    private FuelRecordAdapter fuelRecordAdapter;
    private List<FuelRecord> fuelRecordList;
    private List<Vehicle> vehicleList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_records);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        loadVehiclesFromFirestore();
        loadFuelRecordsFromFirestore();
    }

    private void initViews() {
        recyclerFuelRecords = findViewById(R.id.recycler_fuel_records);
        emptyState = findViewById(R.id.empty_state);
        fabAddRecord = findViewById(R.id.fab_add_record);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        fuelRecordList = new ArrayList<>(); //firebase
        fuelRecordAdapter = new FuelRecordAdapter(fuelRecordList);

        fuelRecordAdapter.setOnEditClickListener(record -> showEditFuelRecordDialog(record));

        fuelRecordAdapter.setOnDeleteClickListener(record -> showDeleteRecordConfirmationDialog(record));

        recyclerFuelRecords.setLayoutManager(new LinearLayoutManager(this));
        recyclerFuelRecords.setAdapter(fuelRecordAdapter);
    }

    private void setupFab() {
        fabAddRecord.setOnClickListener(v -> {
            if (vehicleList == null || vehicleList.isEmpty()) {
                Toast.makeText(this, "Primero debes agregar un vehículo", Toast.LENGTH_SHORT).show();
            } else {
                showAddFuelRecordDialog();
            }
        });
    }

    private void loadVehiclesFromFirestore() {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("vehicles")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vehicleList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Vehicle vehicle = document.toObject(Vehicle.class);
                        vehicleList.add(vehicle);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading vehicles", e);
                });
    }

    private void loadFuelRecordsFromFirestore() {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("fuelRecords")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fuelRecordList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FuelRecord record = document.toObject(FuelRecord.class);
                        fuelRecordList.add(record);
                    }
                    fuelRecordAdapter.notifyDataSetChanged();
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading fuel records", e);
                });
    }
    private void showAddFuelRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_fuel_record, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText etRecordId = dialogView.findViewById(R.id.et_record_id);
        AutoCompleteTextView spinnerVehicle = dialogView.findViewById(R.id.spinner_vehicle);
        TextInputEditText etDate = dialogView.findViewById(R.id.et_date);
        TextInputEditText etLiters = dialogView.findViewById(R.id.et_liters);
        TextInputEditText etKilometers = dialogView.findViewById(R.id.et_kilometers);
        TextInputEditText etPrice = dialogView.findViewById(R.id.et_price);
        AutoCompleteTextView spinnerFuelType = dialogView.findViewById(R.id.spinner_fuel_type);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Generar ID aleatorio de 5 dígitos
        String recordId = String.format("%05d", new Random().nextInt(100000));
        etRecordId.setText(recordId);

        // Configurar spinner de vehículos
        List<String> vehicleNames = new ArrayList<>();
        for (Vehicle vehicle : vehicleList) {
            vehicleNames.add(vehicle.toString());
        }
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleNames);
        spinnerVehicle.setAdapter(vehicleAdapter);

        // Tipo de combustible
        String[] fuelTypes = {getString(R.string.gasoline), getString(R.string.glp), getString(R.string.gnv)};
        ArrayAdapter<String> fuelTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fuelTypes);
        spinnerFuelType.setAdapter(fuelTypeAdapter);

        // Date Picker para fecha
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    FuelRecordsActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        etDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String id = etRecordId.getText().toString().trim();
            String vehicleName = spinnerVehicle.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String litersStr = etLiters.getText().toString().trim();
            String kilometersStr = etKilometers.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String fuelType = spinnerFuelType.getText().toString().trim();

            if (validateRecordData(vehicleName, date, litersStr, kilometersStr, priceStr, fuelType)) {
                double liters = Double.parseDouble(litersStr);
                double kilometers = Double.parseDouble(kilometersStr);
                double price = Double.parseDouble(priceStr);

                //Obtiene el id real del vehiculo
                String vehicleId = getVehicleIdFromName(vehicleName);

                //Registro
                FuelRecord record = new FuelRecord(id, vehicleId, date, liters, kilometers, price, fuelType);

                record.setUserId(auth.getCurrentUser().getUid());

                saveFuelRecordToFirestore(record, dialog);
                Toast.makeText(this, R.string.record_saved, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //Obtener el id del vehiculo
    private String getVehicleIdFromName(String vehicleName) {
        for (Vehicle vehicle : vehicleList) {
            if (vehicle.toString().equals(vehicleName)) {
                return vehicle.getId();
            }
        }
        return "";
    }

    //Guardado en Firestore
    private void saveFuelRecordToFirestore(FuelRecord record, AlertDialog dialog) {
        db.collection("fuelRecords")
                .document(record.getRecordId())
                .set(record)
                .addOnSuccessListener(unused -> {
                    loadFuelRecordsFromFirestore(); // Recargar la lista
                    Toast.makeText(this, R.string.record_saved, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving fuel record", e);
                    Toast.makeText(this, "Error al guardar registro", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateRecordData(String vehicleId, String date, String litersStr, 
                                       String kilometersStr, String priceStr, String fuelType) {
        if (vehicleId.isEmpty() || date.isEmpty() || litersStr.isEmpty() || 
            kilometersStr.isEmpty() || priceStr.isEmpty() || fuelType.isEmpty()) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            double liters = Double.parseDouble(litersStr);
            double kilometers = Double.parseDouble(kilometersStr);
            double price = Double.parseDouble(priceStr);

            if (liters <= 0 || kilometers <= 0 || price <= 0) {
                Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showEditFuelRecordDialog(FuelRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_fuel_record, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextInputEditText etRecordId = dialogView.findViewById(R.id.et_record_id);
        AutoCompleteTextView spinnerVehicle = dialogView.findViewById(R.id.spinner_vehicle);
        TextInputEditText etDate = dialogView.findViewById(R.id.et_date);
        TextInputEditText etLiters = dialogView.findViewById(R.id.et_liters);
        TextInputEditText etKilometers = dialogView.findViewById(R.id.et_kilometers);
        TextInputEditText etPrice = dialogView.findViewById(R.id.et_price);
        AutoCompleteTextView spinnerFuelType = dialogView.findViewById(R.id.spinner_fuel_type);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);


        etRecordId.setText(record.getRecordId());
        etRecordId.setEnabled(false); // Id no modificable
        etDate.setText(record.getDate());
        etLiters.setText(String.valueOf(record.getLiters()));
        etKilometers.setText(String.valueOf(record.getCurrentKilometers()));
        etPrice.setText(String.valueOf(record.getTotalPrice()));
        spinnerFuelType.setText(record.getFuelType(), false);


        String[] vehicleNames = new String[vehicleList.size()];
        for (int i = 0; i < vehicleList.size(); i++) {
            vehicleNames[i] = vehicleList.get(i).toString();
        }
        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vehicleNames);
        spinnerVehicle.setAdapter(vehicleAdapter);

        // Seleccionar el vehículo actual
        for (int i = 0; i < vehicleList.size(); i++) {
            if (vehicleList.get(i).getId().equals(record.getVehicleId())) {
                spinnerVehicle.setText(vehicleList.get(i).toString(), false);
                break;
            }
        }

        // Configurar spinner de tipo de combustible
        String[] fuelTypes = {"Gasolina 90", "Gasolina 95", "Gasolina 97", "Diesel", "GLP", "GNV"};
        ArrayAdapter<String> fuelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fuelTypes);
        spinnerFuelType.setAdapter(fuelAdapter);

        // Date Picker
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    FuelRecordsActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        etDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String id = etRecordId.getText().toString().trim();
            String vehicleName = spinnerVehicle.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String litersStr = etLiters.getText().toString().trim();
            String kilometersStr = etKilometers.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String fuelType = spinnerFuelType.getText().toString().trim();

            if (validateRecordData(vehicleName, date, litersStr, kilometersStr, priceStr, fuelType)) {
                double liters = Double.parseDouble(litersStr);
                double kilometers = Double.parseDouble(kilometersStr);
                double price = Double.parseDouble(priceStr);

                String vehicleId = getVehicleIdFromName(vehicleName);

                // Actualizar registro
                record.setVehicleId(vehicleId);
                record.setDate(date);
                record.setLiters(liters);
                record.setCurrentKilometers(kilometers);
                record.setTotalPrice(price);
                record.setFuelType(fuelType);

                // Actualizar en Firestore
                updateFuelRecordInFirestore(record, dialog);
            }
        });

        dialog.show();
    }

    private void updateFuelRecordInFirestore(FuelRecord record, AlertDialog dialog) {
        db.collection("fuelRecords")
                .document(record.getRecordId())
                .set(record)
                .addOnSuccessListener(unused -> {
                    loadFuelRecordsFromFirestore(); // Recargar la lista
                    Toast.makeText(this, "Registro actualizado", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating fuel record", e);
                    Toast.makeText(this, "Error al actualizar registro", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteRecordConfirmationDialog(FuelRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Registro")
                .setMessage("¿Estás seguro de que deseas eliminar este registro de combustible?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteFuelRecordFromFirestore(record))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteFuelRecordFromFirestore(FuelRecord record) {
        db.collection("fuelRecords")
                .document(record.getRecordId())
                .delete()
                .addOnSuccessListener(unused -> {
                    loadFuelRecordsFromFirestore(); // Recargar la lista
                    Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting fuel record", e);
                    Toast.makeText(this, "Error al eliminar registro", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (fuelRecordList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerFuelRecords.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerFuelRecords.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos al reanudar la activisad
        loadFuelRecordsFromFirestore();
        loadVehiclesFromFirestore();
    }
}

