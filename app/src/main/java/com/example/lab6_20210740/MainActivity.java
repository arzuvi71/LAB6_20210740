package com.example.lab6_20210740;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardVehicles;
    private MaterialCardView cardFuelRecords;
    private MaterialCardView cardSummary;
    private Button btnLogout;
    private TextView tvWelcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            // Verificacion  de autenticado
            verifyAuthentication();

            initViews();
            setupCardClicks();
            setupLogoutButton();
            updateWelcomeMessage();
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error en onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar la aplicación: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void verifyAuthentication() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // De no estar autenticado redirige all Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initViews() {
        try {
            cardVehicles = findViewById(R.id.card_explore_tours);
            cardFuelRecords = findViewById(R.id.card_my_reservations);
            cardSummary = findViewById(R.id.card_chats);
            btnLogout = findViewById(R.id.btn_logout);
            tvWelcomeMessage = findViewById(R.id.tv_welcome_message);

            // Verificar que todas las vistas se encontraron
            if (cardVehicles == null || cardFuelRecords == null || cardSummary == null ||
                btnLogout == null || tvWelcomeMessage == null) {
                android.util.Log.e("MainActivity", "Error: Algunas vistas no se encontraron en el layout");
                throw new RuntimeException("Error al inicializar las vistas");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error en initViews: " + e.getMessage());
            throw e;
        }
    }

    private void setupCardClicks() {
        cardVehicles.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VehiclesActivity.class);
            startActivity(intent);
        });

        cardFuelRecords.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FuelRecordsActivity.class);
            startActivity(intent);
        });

        cardSummary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
            startActivity(intent);
        });
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    // Mensaje de bienvenida
    private void updateWelcomeMessage() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String welcomeMessage = "¡Hola!";

                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    welcomeMessage = "¡Hola, " + currentUser.getDisplayName() + "!";
                } else if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                    // Extraer el nombre del email
                    String emailName = currentUser.getEmail().split("@")[0];
                    welcomeMessage = "¡Hola, " + emailName + "!";
                }

                tvWelcomeMessage.setText(welcomeMessage);
                android.util.Log.d("MainActivity", "Mensaje de bienvenida actualizado: " + welcomeMessage);
            } else {
                android.util.Log.e("MainActivity", "Usuario actual es null");
                tvWelcomeMessage.setText("¡Hola!");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error al actualizar mensaje de bienvenida: " + e.getMessage());
            tvWelcomeMessage.setText("¡Hola!");
        }
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualizar mensaje de bienvenida
        updateWelcomeMessage();
    }
}