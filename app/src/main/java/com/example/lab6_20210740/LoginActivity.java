package com.example.lab6_20210740;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar idioma español
        setLocaleToSpanish();

        // Verificar si ya está autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Si ya esta logueado ir al MainActivity
            goToMainActivity();
        } else {
            // Sino mostrar pantalla de login
            showLoginScreen();
        }
    }

    private void setLocaleToSpanish() {
        Locale locale = new Locale("es");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void showLoginScreen() {
        // Provedores de autenticació
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Intent de login
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.fuelmonitor)
                .setTheme(R.style.Theme_LAB6_20210740)
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            // Login exitoso
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Log.d("Auth", "Usuario logueado exitosamente");
                Log.d("Auth", "Email: " + user.getEmail());
                Log.d("Auth", "Display Name: " + user.getDisplayName());
                Log.d("Auth", "UID: " + user.getUid());

                String welcomeMessage = "Bienvenido";
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    welcomeMessage = "Bienvenido " + user.getDisplayName();
                } else if (user.getEmail() != null) {
                    welcomeMessage = "Bienvenido " + user.getEmail();
                }

                Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();

                new android.os.Handler().postDelayed(() -> {
                    goToMainActivity();
                }, 500);
            } else {
                Log.e("Auth", "Usuario es null después del login exitoso");
                Toast.makeText(this, "Error: No se pudo obtener información del usuario", Toast.LENGTH_LONG).show();
            }
        } else {
            // Login fallido
            if (response != null && response.getError() != null) {
                int errorCode = response.getError().getErrorCode();
                String errorMsg = response.getError().getMessage();

                Log.e("Auth", "Error en login - Código: " + errorCode);
                Log.e("Auth", "Error en login - Mensaje: " + errorMsg);

                String errorMessage = "Error en el inicio de sesión";

                // Manejo de errores
                if (errorCode == 10) {
                    // Error de configuración de Google Sign-In
                    errorMessage = "Error de de la app";
                    showGoogleSignInConfigError();
                } else if (errorCode == 4) {
                    errorMessage = "Error de red o configuración.\n" +
                            "Verifica tu conexión a Internet y la configuración de Firebase.";
                } else if (errorMsg != null && !errorMsg.isEmpty()) {
                    errorMessage = errorMsg;
                }

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            } else {
                Log.d("Auth", "Login cancelado por el usuario");
                Toast.makeText(this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
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

    private void showGoogleSignInConfigError() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Error de Configuración")
                .setMessage("Google Sign-In no está configurado correctamente.\n\n" +
                        "SOLUCIÓN:\n\n" +
                        "1. Ejecuta en terminal:\n" +
                        "   gradlew signingReport\n\n" +
                        "2. Copia el SHA-1 que aparece\n\n" +
                        "3. Ve a Firebase Console:\n" +
                        "   - Project Settings\n" +
                        "   - Your apps\n" +
                        "   - Add fingerprint\n\n" +
                        "4. Pega el SHA-1 y guarda\n\n" +
                        "5. Descarga el nuevo google-services.json\n\n" +
                        "6. Reemplaza el archivo en app/")
                .setPositiveButton("Entendido", null)
                .setNegativeButton("Usar Email", (dialog, which) -> {
                    // Volver a mostrar la pantalla de login
                    showLoginScreen();
                })
                .show();
    }
}