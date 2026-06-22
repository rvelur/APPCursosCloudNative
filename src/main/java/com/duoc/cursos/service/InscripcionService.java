package com.duoc.cursos.service;

import com.duoc.cursos.dto.InscripcionRequest;
import com.duoc.cursos.model.Curso;
import com.duoc.cursos.model.Inscripcion;
import com.duoc.cursos.repository.CursoRepository;
import com.duoc.cursos.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InscripcionService {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public List<Curso> obtenerTodosLosCursos() {
        return cursoRepository.findAll();
    }

    public Curso guardarCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    public Inscripcion registrarInscripcion(InscripcionRequest request) {
        List<Curso> cursos = cursoRepository.findAllById(request.getCursosIds());
        
        if (cursos.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un curso válido.");
        }

        Double total = cursos.stream()
                             .mapToDouble(Curso::getCosto)
                             .sum();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudianteId(request.getEstudianteId());
        inscripcion.setFechaInscripcion(LocalDateTime.now());
        inscripcion.setCursosSeleccionados(cursos);
        inscripcion.setTotalPagar(total);

        return inscripcionRepository.save(inscripcion);
    }
}