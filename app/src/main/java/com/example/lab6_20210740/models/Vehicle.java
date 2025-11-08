package com.example.lab6_20210740.models;

import java.io.Serializable;

public class Vehicle implements Serializable {
    private String id;
    private String plate;
    private String brand;
    private String model;
    private int year;
    private String technicalReviewDate;

    private String userId;

    //Constructor para el Firebase
    public Vehicle() {
    }

    public Vehicle(String id, String plate, String brand, String model, int year, String technicalReviewDate) {
        this.id = id;
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.technicalReviewDate = technicalReviewDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTechnicalReviewDate() {
        return technicalReviewDate;
    }

    public void setTechnicalReviewDate(String technicalReviewDate) {
        this.technicalReviewDate = technicalReviewDate;
    }

    @Override
    public String toString() {
        return id + " - " + brand + " " + model;
    }
}

