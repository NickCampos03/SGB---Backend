package com.example.sgb.model;

import com.example.sgb.model.enums.Perfil;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigologin")
    private Integer codigoLogin;

    @Column(name = "nome", length = 100)
    private String nome;

    @Column(name = "idade")
    private Integer idade;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "senha")
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil")
    private Perfil perfil;
}
