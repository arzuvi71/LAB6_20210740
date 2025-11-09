package com.example.lab6_20210740;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import java.util.Locale;

public class FuelMonitorApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Configuración de idioma para signin
        setLocaleToSpanish();

        Log.d("FuelMonitorApp", "Aplicación iniciada con idioma español");
    }

    private void setLocaleToSpanish() {
        Locale locale = new Locale("es");
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Preconfiguración para español
        setLocaleToSpanish();
    }
}

