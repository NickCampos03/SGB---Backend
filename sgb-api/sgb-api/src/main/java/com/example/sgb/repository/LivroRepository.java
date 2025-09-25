package com.example.sgb.repository;

import com.example.sgb.model.Livro;
import com.example.sgb.model.enums.Disponibilidade;
import com.example.sgb.model.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Integer> {

    List<Livro> findByGenero(Genero genero);

    List<Livro> findByDisponibilidade(Disponibilidade disponibilidade);

    List<Livro> findByGeneroAndDisponibilidade(Genero genero, Disponibilidade disponibilidade);
}
