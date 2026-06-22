package com.duoc.cursos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inscripciones")
@Data

public class Inscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long estudianteId;
    private LocalDateTime fechaInscripcion;
    private Double totalPagar;

    @ManyToMany
    @JoinTable(
        name = "inscripcion_detalles",
        joinColumns = @JoinColumn(name = "inscripcion_id"),
        inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private List<Curso> cursosSeleccionados;
}