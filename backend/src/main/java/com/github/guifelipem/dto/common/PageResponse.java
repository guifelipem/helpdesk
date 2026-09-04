package com.github.guifelipem.dto.common;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Página de resultados")
public record PageResponse<T>(
        @Schema(description = "Itens da página atual") List<T> content,
        @Schema(description = "Índice da página atual, começando em zero", example = "0") int page,
        @Schema(description = "Tamanho da página", example = "10") int size,
        @Schema(description = "Total de itens encontrados", example = "24") long totalElements,
        @Schema(description = "Total de páginas", example = "3") int totalPages,
        @Schema(description = "Indica se esta é a última página", example = "false") boolean last
) {}
