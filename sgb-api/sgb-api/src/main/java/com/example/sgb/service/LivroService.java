package com.example.sgb.service;

import com.example.sgb.model.Livro;
import com.example.sgb.model.enums.Disponibilidade;
import com.example.sgb.model.Genero;
import com.example.sgb.repository.LivroRepository;
import com.example.sgb.repository.GeneroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LivroService {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private GeneroRepository generoRepository;

    // Lista livros com filtros opcionais por gênero e disponibilidade
    public List<Livro> listarTodos(Integer generoId, Disponibilidade disponibilidade) {
        Genero genero = null;
        if (generoId != null) {
            genero = generoRepository.findById(generoId)
                    .orElseThrow(() -> new IllegalArgumentException("Gênero não encontrado"));
        }

        if (genero != null && disponibilidade != null) {
            return livroRepository.findByGeneroAndDisponibilidade(genero, disponibilidade);
        } else if (genero != null) {
            return livroRepository.findByGenero(genero);
        } else if (disponibilidade != null) {
            return livroRepository.findByDisponibilidade(disponibilidade);
        } else {
            return livroRepository.findAll();
        }
    }

    public Optional<Livro> buscarPorId(Integer id) {
        return livroRepository.findById(id);
    }

    public Livro salvar(Livro livro) {
        validarGenero(livro);
        if (livro.getDisponibilidade() == null) {
            livro.setDisponibilidade(Disponibilidade.DISPONIVEL); // padrão
        }
        return livroRepository.save(livro);
    }

    public Livro atualizar(Integer id, Livro livro) {
        if (!livroRepository.existsById(id)) {
            throw new RuntimeException("Livro não encontrado!");
        }
        validarGenero(livro);
        livro.setCodigolivro(id);
        return livroRepository.save(livro);
    }

    public void deletar(Integer id) {
        livroRepository.deleteById(id);
    }

    // Valida se o gênero do livro existe na tabela Genero
    private void validarGenero(Livro livro) {
        if (livro.getGenero() == null || livro.getGenero().getId() == null) {
            throw new IllegalArgumentException("Gênero inválido!");
        }
        if (!generoRepository.existsById(livro.getGenero().getId())) {
            throw new IllegalArgumentException("Gênero não cadastrado!");
        }
    }
}
