package com.example.procastinator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recompensa")
public class Recompensa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recompensa")
    private Integer id;

    @Column(length = 100)
    private String titulo;

    @Column
    private String descricao;

    private Integer pontos;

    @Column(name = "data_conquista")
    private LocalDateTime dataConquista;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarefa")
    private Tarefa tarefa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}


