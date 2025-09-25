package com.example.sgb.repository;

import com.example.sgb.model.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Integer> {

    // Busca um gênero pelo nome (opcional)
    Optional<Genero> findByNome(String nome);

    // Verifica se existe um gênero pelo nome
    boolean existsByNome(String nome);
}
