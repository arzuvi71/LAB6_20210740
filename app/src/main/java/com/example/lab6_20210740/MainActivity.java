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
        setContentView(R.layout.activity_main);

        // Verificacion  de autenticado
        verifyAuthentication();

        initViews();
        setupCardClicks();
        setupLogoutButton();
        updateWelcomeMessage();
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
        cardVehicles = findViewById(R.id.card_explore_tours);
        cardFuelRecords = findViewById(R.id.card_my_reservations);
        cardSummary = findViewById(R.id.card_chats);
        btnLogout = findViewById(R.id.button);
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);
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

    // Mensjae debienvenida
    private void updateWelcomeMessage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            String welcomeMessage = "¡Hola, " + currentUser.getDisplayName() + "!";
            tvWelcomeMessage.setText(welcomeMessage);
        } else if (currentUser != null && currentUser.getEmail() != null) {
            String welcomeMessage = "¡Hola, " + currentUser.getEmail() + "!";
            tvWelcomeMessage.setText(welcomeMessage);
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