package com.duoc.cursos.service;

import com.duoc.cursos.dto.InscripcionRequest;
import com.duoc.cursos.model.Curso;
import com.duoc.cursos.model.Inscripcion;
import com.duoc.cursos.repository.InscripcionRepository;
import com.duoc.cursos.repository.CursoRepository;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private S3Template s3Template;

    // 1. Consultar lista de cursos disponibles
    public List<Curso> obtenerTodosLosCursos() {
        return cursoRepository.findAll();
    }

    // 2. Agregar nuevos cursos a la oferta educativa
    public Curso guardarCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    // 3. Inscripción de estudiantes (Guarda en Oracle Cloud + Sube archivo físico a AWS S3)
    public Inscripcion registrarInscripcion(InscripcionRequest request) {
        
        Inscripcion nuevaInscripcion = new Inscripcion();
        nuevaInscripcion.setEstudianteId(request.getEstudianteId());
        nuevaInscripcion.setFechaInscripcion(LocalDateTime.now());
        
        double sumaTotal = 0.0;
        List<Curso> cursosALinkear = new ArrayList<>();
        
        // CORREGIDO: getCursosIds() con 's'
        for (Long cursoId : request.getCursosIds()) {
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado con ID: " + cursoId));
            
            // CORREGIDO: getCosto() en vez de getPrecio()
            sumaTotal += curso.getCosto();
            cursosALinkear.add(curso);
        }
        
        nuevaInscripcion.setTotalPagar(sumaTotal);
        nuevaInscripcion.setCursosSeleccionados(cursosALinkear);

        // PERSISTENCIA 1: Guardado relacional en Oracle Cloud
        Inscripcion inscripcionGuardada = inscripcionRepository.save(nuevaInscripcion);

        // PERSISTENCIA 2: Generación de archivo físico y subida automática a AWS S3
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== RESUMEN DE INSCRIPCIÓN VIRTUAL ===\n");
            sb.append("Número de Inscripción: ").append(inscripcionGuardada.getId()).append("\n");
            sb.append("ID del Estudiante: ").append(inscripcionGuardada.getEstudianteId()).append("\n");
            sb.append("Fecha del Sistema: ").append(inscripcionGuardada.getFechaInscripcion()).append("\n");
            sb.append("--------------------------------------\n");
            sb.append("Cursos Inscritos:\n");
            for (Curso c : inscripcionGuardada.getCursosSeleccionados()) {
                // CORREGIDO: getCosto() aquí también
                sb.append("- ").append(c.getNombre()).append(" ($").append(c.getCosto()).append(")\n");
            }
            sb.append("--------------------------------------\n");
            sb.append("TOTAL PAGADO: $").append(inscripcionGuardada.getTotalPagar()).append("\n");
            
            this.generarYSubirResumenS3(inscripcionGuardada.getId(), sb.toString());
            
        } catch (IOException e) {
            System.err.println("Advertencia S3: No se pudo respaldar el archivo físico: " + e.getMessage());
        }

        return inscripcionGuardada;
    }

    // Método privado auxiliar para interactuar con AWS S3
    private void generarYSubirResumenS3(Long inscripcionId, String contenidoResumen) throws IOException {
        String nombreArchivo = "resumen_" + inscripcionId + ".txt";
        File archivoLocal = new File(nombreArchivo);
        
        try (FileWriter writer = new FileWriter(archivoLocal)) {
            writer.write(contenidoResumen);
        }

        String s3Key = inscripcionId + "/" + nombreArchivo;

        try (InputStream inputStream = new FileInputStream(archivoLocal)) {
            s3Template.upload(bucketName, s3Key, inputStream);
        }
    }

    // =========================================================================
    // MÉTODOS COMPLEMENTARIOS PARA LOS 3 ENDPOINTS EXTRAS (CRUD S3)
    // =========================================================================

    public InputStream descargarResumenS3(Long inscripcionId) throws IOException {
        String s3Key = inscripcionId + "/resumen_" + inscripcionId + ".txt";
        return s3Template.download(bucketName, s3Key).getInputStream();
    }

    public void modificarResumenS3(Long inscripcionId, String nuevoContenido) throws IOException {
        String nombreArchivo = "resumen_" + inscripcionId + ".txt";
        File archivoLocal = new File(nombreArchivo);
        
        try (FileWriter writer = new FileWriter(archivoLocal)) {
            writer.write(nuevoContenido);
        }

        String s3Key = inscripcionId + "/" + nombreArchivo;
        try (InputStream inputStream = new FileInputStream(archivoLocal)) {
            s3Template.upload(bucketName, s3Key, inputStream);
        }
    }

    public void eliminarResumenS3(Long inscripcionId) {
        String s3Key = inscripcionId + "/resumen_" + inscripcionId + ".txt";
        s3Template.deleteObject(bucketName, s3Key);
    }
}