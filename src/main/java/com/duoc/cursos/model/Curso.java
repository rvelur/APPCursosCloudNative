package com.duoc.cursos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cursos")
@Data

public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private String duracion; 
    private Double costo;
}