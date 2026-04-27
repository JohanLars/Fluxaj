package com.fluxaj.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "suscripciones")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del proveedor no puede estar vacío")
    @Column(nullable = false)
    private String proveedor;

    @NotNull(message = "El monto es obligatorio")
    @Min(value = 0, message = "El monto no puede ser negativo")
    @Column(nullable = false)
    private Double monto;

    @NotNull(message = "La fecha de cobro es obligatoria")
    @Column(name = "fecha_cobro", nullable = false)
    private LocalDate fechaCobro;

    @Column(nullable = false)
    private Boolean activa = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column
    private String categoria;           // "Streaming", "Trabajo", "Salud", "Gaming", "Otros"

    @Column(name = "origen_escaneo")
    private boolean origenEscaneo;      // true = detectado por Gmail, false = manual

    // --- Getters y Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public LocalDate getFechaCobro() { return fechaCobro; }
    public void setFechaCobro(LocalDate fechaCobro) { this.fechaCobro = fechaCobro; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    // Métodos que faltaban para GmailScannerService
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public boolean isOrigenEscaneo() { return origenEscaneo; }
    public void setOrigenEscaneo(boolean origenEscaneo) { this.origenEscaneo = origenEscaneo; }
}