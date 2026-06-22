package com.duoc.cursos.dto;

import lombok.Data;
import java.util.List;

@Data
public class InscripcionRequest {
    private Long estudianteId;
    private List<Long> cursosIds;
}