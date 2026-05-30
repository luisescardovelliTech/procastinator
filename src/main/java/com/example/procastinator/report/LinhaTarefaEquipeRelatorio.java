package com.example.procastinator.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinhaTarefaEquipeRelatorio {
    private String titulo;
    private String status;
    private String prazo;
    private String responsavel;
    private String alerta;
}
