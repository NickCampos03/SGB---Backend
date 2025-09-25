package com.example.sgb.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.sgb.model.enums.Disponibilidade;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Disponibilidade disponibilidade;

    @Transient
    private Integer diasEmAtraso;

    @Transient
    private BigDecimal saldoDevedor;

    @Transient
    private Boolean emAtraso;

    @Transient
    private String nomeLivro;

    @Transient
    private String nomeUsuario;

}
