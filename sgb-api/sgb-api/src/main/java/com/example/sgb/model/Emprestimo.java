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
    @JoinColumn(name = "cod_livro", referencedColumnName = "codigolivro", nullable = false)
    private Livro livro;

    @ManyToOne
    @JoinColumn(name = "cod_usuario", referencedColumnName = "codigologin", nullable = false)
    private Usuario usuario;

    @Column
    private LocalDate datadeentrega;

    @Column
    private LocalDate dataderetirada;

    @Column
    private LocalDate dataPrevista;

    @Column
    private Boolean emAtraso;

}
