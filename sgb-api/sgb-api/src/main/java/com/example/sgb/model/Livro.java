package com.example.sgb.model;

import com.example.sgb.model.enums.Disponibilidade;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "livro")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigolivro;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 100, nullable = false)
    private String autor;

    @ManyToOne
    @JoinColumn(name = "genero_id", nullable = false)
    private Genero genero;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Disponibilidade disponibilidade;
}
