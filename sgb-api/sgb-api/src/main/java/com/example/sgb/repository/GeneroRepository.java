package com.example.sgb.repository;

import com.example.sgb.model.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Integer> {
    Optional<Genero> findByNome(String nome);
}
