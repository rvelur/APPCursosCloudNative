package com.duoc.cursos.controller;

import com.duoc.cursos.dto.InscripcionRequest;
import com.duoc.cursos.model.Curso;
import com.duoc.cursos.model.Inscripcion;
import com.duoc.cursos.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EducacionController {

    @Autowired
    private InscripcionService inscripcionService;

    // Endpoint 1: Consultar lista de cursos disponibles
    @GetMapping("/cursos")
    public ResponseEntity<List<Curso>> listarCursos() {
        List<Curso> cursos = inscripcionService.obtenerTodosLosCursos();
        return new ResponseEntity<>(cursos, HttpStatus.OK);
    }

    // Endpoint 2: Agregar nuevos cursos a la oferta educativa
    @PostMapping("/cursos")
    public ResponseEntity<Curso> crearCurso(@RequestBody Curso curso) {
        Curso nuevoCurso = inscripcionService.guardarCurso(curso);
        return new ResponseEntity<>(nuevoCurso, HttpStatus.CREATED);
    }

    // Endpoint 3: Inscripción de estudiantes con resumen y total calculado
    @PostMapping("/inscripciones")
    public ResponseEntity<Inscripcion> inscribirEstudiante(@RequestBody InscripcionRequest request) {
        try {
            Inscripcion nuevaInscripcion = inscripcionService.registrarInscripcion(request);
            return new ResponseEntity<>(nuevaInscripcion, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}