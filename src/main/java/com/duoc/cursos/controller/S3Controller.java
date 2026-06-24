package com.duoc.cursos.controller;

import com.duoc.cursos.dto.S3ObjectInfo;
import com.duoc.cursos.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;

import java.util.List;

@RestController
@RequestMapping("/s3/cloud-storage-bucket-duoc")
@CrossOrigin(origins = "*")
public class S3Controller {

    @Autowired
    private InscripcionService inscripcionService;

    // Endpoint Extra 1: DESCARGAR ARCHIVO DESDE S3 (GET)
    // URL: GET http://localhost:8080/s3/cloud-storage-bucket-duoc/objects/{id}
    @GetMapping("/objects/{id}")
    public ResponseEntity<Resource> descargarArchivoS3(@PathVariable Long id) {
        try {
            InputStream inputStream = inscripcionService.descargarResumenS3(id);

            // 1️⃣ Fix para el primer Warning: Validar que el stream no sea nulo
            if (inputStream == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            InputStreamResource resource = new InputStreamResource(inputStream);

            // 2️⃣ Fix para el segundo Warning: Usar el objeto estático directo en
            // contentType
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resumen_" + id + ".txt\"")
                    .contentType(MediaType.TEXT_PLAIN) // VS Code a veces prefiere
                                                       // .contentType(MediaType.parseMediaType("text/plain")) si alega
                                                       // por la anotación @NonNull
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Endpoint Extra 2: MODIFICAR ARCHIVO EN S3 (PUT)
    // URL: PUT http://localhost:8080/s3/cloud-storage-bucket-duoc/objects/{id}
    @PutMapping("/objects/{id}")
    public ResponseEntity<String> modificarArchivoS3(@PathVariable Long id, @RequestBody String nuevoContenido) {
        try {
            inscripcionService.modificarResumenS3(id, nuevoContenido);
            return ResponseEntity.ok("Archivo modificado de forma exitosa en S3 para el ID: " + id);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al modificar el archivo en el bucket: " + e.getMessage());
        }
    }

    // Endpoint Extra 3: ELIMINAR ARCHIVO EN S3 (DELETE)
    // URL: DELETE http://localhost:8080/s3/cloud-storage-bucket-duoc/objects/{id}
    @DeleteMapping("/objects/{id}")
    public ResponseEntity<String> eliminarArchivoS3(@PathVariable Long id) {
        try {
            inscripcionService.eliminarResumenS3(id);
            return ResponseEntity.ok("Archivo eliminado correctamente del bucket S3 para el ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al intentar eliminar el archivo: " + e.getMessage());
        }
    }

    @GetMapping("/objects")
    public ResponseEntity<List<S3ObjectInfo>> verObjetosDelBucket() {
        List<S3ObjectInfo> archivos = inscripcionService.listarTodosLosObjetosDetallados();
        return ResponseEntity.ok(archivos);
    }
}