package com.example.sgb.model;

import jakarta.persistence.*;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigoemprestimo")
    private Integer codigoemprestimo;

    @ManyToOne
    @JoinColumn(name = "livro_id", referencedColumnName = "codigolivro", nullable = false)
    private Livro livro;

    @ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "codigologin", nullable = false)
    private Usuario usuario;

    @Column(name = "datadeentrega")
    private LocalDate datadeentrega;

    @Column(name = "dataderetirada")
    private LocalDate dataderetirada;

    @Column(name="dataprevista", nullable=false)
    private LocalDate dataPrevista;

    @Column(name = "ematraso")
    private Boolean emAtraso;
}
