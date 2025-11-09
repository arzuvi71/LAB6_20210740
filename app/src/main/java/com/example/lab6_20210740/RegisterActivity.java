package com.example.lab6_20210740;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombres, etApellidos, etNumeroDocumento;
    private TextInputEditText etFechaNacimiento, etCorreo, etTelefono, etPassword, etConfirmPassword;
    private AutoCompleteTextView etDocumento;
    private MaterialButton btnSiguiente;
    private TextView tvRegresar;
    private CountryCodePicker ccp;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupDocumentTypeSpinner();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        etNombres = findViewById(R.id.etNombres);
        etApellidos = findViewById(R.id.etApellidos);
        etDocumento = findViewById(R.id.etDocumento);
        etNumeroDocumento = findViewById(R.id.etNumeroDocumento);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etCorreo = findViewById(R.id.etCorreo);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etTelefono = findViewById(R.id.etTelefono);
        btnSiguiente = findViewById(R.id.btnSiguiente);
        tvRegresar = findViewById(R.id.tvRegresar);
        ccp = findViewById(R.id.ccp);

        // Limpiar datos
        etNombres.setText("");
        etApellidos.setText("");
        etFechaNacimiento.setText("");
        etTelefono.setText("");
    }

    private void setupDocumentTypeSpinner() {
        String[] documentTypes = {"DNI", "Carnet de Extranjería", "Pasaporte"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, documentTypes);
        etDocumento.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etFechaNacimiento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        etFechaNacimiento.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        tvRegresar.setOnClickListener(v -> finish());

        btnSiguiente.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String nombres = etNombres.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String tipoDocumento = etDocumento.getText().toString().trim();
        String numeroDocumento = etNumeroDocumento.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String codigoPais = ccp.getSelectedCountryCodeWithPlus();

        // Validaciones
        if (TextUtils.isEmpty(nombres)) {
            etNombres.setError("Ingresa tus nombres");
            return;
        }

        if (TextUtils.isEmpty(apellidos)) {
            etApellidos.setError("Ingresa tus apellidos");
            return;
        }

        if (TextUtils.isEmpty(tipoDocumento)) {
            etDocumento.setError("Selecciona un tipo de documento");
            return;
        }

        if (TextUtils.isEmpty(numeroDocumento)) {
            etNumeroDocumento.setError("Ingresa tu número de documento");
            return;
        }

        if (TextUtils.isEmpty(fechaNacimiento)) {
            etFechaNacimiento.setError("Selecciona tu fecha de nacimiento");
            return;
        }

        if (TextUtils.isEmpty(correo)) {
            etCorreo.setError("Ingresa tu correo electrónico");
            return;
        }

        if (TextUtils.isEmpty(telefono)) {
            etTelefono.setError("Ingresa tu número de teléfono");
            return;
        }

        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingresa una contraseña");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }


        btnSiguiente.setEnabled(false);
        btnSiguiente.setText("Registrando...");

        // Crear usuario en Firebase
        auth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Register", "Usuario creado exitosamente");
                        FirebaseUser user = auth.getCurrentUser();

                        // Guardar datis en Firestore
                        saveUserDataToFirestore(user.getUid(), nombres, apellidos, tipoDocumento,
                                numeroDocumento, fechaNacimiento, correo, codigoPais + telefono);
                    } else {
                        btnSiguiente.setEnabled(true);
                        btnSiguiente.setText("Siguiente");
                        Log.e("Register", "Error al crear usuario", task.getException());
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String nombres, String apellidos,
                                         String tipoDocumento, String numeroDocumento,
                                         String fechaNacimiento, String correo, String telefono) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombres", nombres);
        userData.put("apellidos", apellidos);
        userData.put("tipoDocumento", tipoDocumento);
        userData.put("numeroDocumento", numeroDocumento);
        userData.put("fechaNacimiento", fechaNacimiento);
        userData.put("correo", correo);
        userData.put("telefono", telefono);
        userData.put("userId", userId);

        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Log.d("Register", "Datos de usuario guardados en Firestore");
                    Toast.makeText(this, "Registro exitoso. Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show();


                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Ingresar al registrarse

                })
                .addOnFailureListener(e -> {
                    btnSiguiente.setEnabled(true);
                    btnSiguiente.setText("Siguiente");
                    Log.e("Register", "Error al guardar datos en Firestore", e);
                    Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

