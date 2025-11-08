package com.example.lab6_20210740;

public class UsuarioDTO {
    private String nombre;
    private String correo;
    private String dni;

    // Constructor vacío (OBLIGATORIO para Firebase)
    public UsuarioDTO() {}

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
}
