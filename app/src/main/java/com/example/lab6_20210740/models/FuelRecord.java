package com.example.lab6_20210740.models;

import java.io.Serializable;

public class FuelRecord implements Serializable {
    private String recordId;
    private String vehicleId;
    private String date;
    private double liters;
    private double currentKilometers;
    private double totalPrice;
    private String fuelType; // Gasolina, GLP, GNV

    private String userId;

    //Constructor con el Firebase
    public FuelRecord() {
    }

    public FuelRecord(String recordId, String vehicleId, String date, double liters, 
                      double currentKilometers, double totalPrice, String fuelType) {
        this.recordId = recordId;
        this.vehicleId = vehicleId;
        this.date = date;
        this.liters = liters;
        this.currentKilometers = currentKilometers;
        this.totalPrice = totalPrice;
        this.fuelType = fuelType;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLiters() {
        return liters;
    }

    public void setLiters(double liters) {
        this.liters = liters;
    }

    public double getCurrentKilometers() {
        return currentKilometers;
    }

    public void setCurrentKilometers(double currentKilometers) {
        this.currentKilometers = currentKilometers;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }
}

