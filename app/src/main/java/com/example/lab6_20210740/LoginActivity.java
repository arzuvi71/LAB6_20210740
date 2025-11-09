package com.example.lab6_20210740;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnRegister;
    private Button btnGoogleSignIn;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar idioma español
        setLocaleToSpanish();

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Verificar si ya está autenticado
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // Si ya esta logueado ir al MainActivity
            goToMainActivity();
        } else {
            // Mostrar pantalla de login
            setContentView(R.layout.activity_login);
            initViews();
            setupListeners();
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnGoogleSignIn = findViewById(R.id.btn_google_signin);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnRegister.setOnClickListener(v -> goToRegister());
        btnGoogleSignIn.setOnClickListener(v -> loginWithGoogle());
    }

    private void loginWithGoogle() {
        // Usar FirebaseUI solo para Google Sign-In
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.fuelmonitor)
                .setTheme(R.style.Theme_LAB6_20210740)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingresa tu correo electrónico");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingresa tu contraseña");
            return;
        }


        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando sesión...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login);

                    if (task.isSuccessful()) {
                        Log.d("Auth", "Login exitoso con email");
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(this, "Bienvenido " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        goToMainActivity();
                    } else {
                        Log.e("Auth", "Error en login", task.getException());
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void setLocaleToSpanish() {
        Locale locale = new Locale("es");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            // Login exitoso con Google
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                Log.d("Auth", "Usuario logueado exitosamente con Google");
                Toast.makeText(this, "Bienvenido " + user.getEmail(), Toast.LENGTH_SHORT).show();
                goToMainActivity();
            }
        } else {
            // Login fallido
            if (response != null && response.getError() != null) {
                Log.e("Auth", "Error en login con Google: " + response.getError().getMessage());
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goToMainActivity() {
        try {
            Log.d("Auth", "Navegando a MainActivity...");
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Log.d("Auth", "Navegación completada");
        } catch (Exception e) {
            Log.e("Auth", "Error al navegar a MainActivity: " + e.getMessage());
            Toast.makeText(this, "Error al abrir la aplicación: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}