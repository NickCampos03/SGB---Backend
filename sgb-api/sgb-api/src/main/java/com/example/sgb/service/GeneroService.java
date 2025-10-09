package com.example.sgb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sgb.model.Genero;
import com.example.sgb.repository.GeneroRepository;

@Service
public class GeneroService {

    @Autowired
    private GeneroRepository generoRepository;

    // Listar todos
    public List<Genero> listarTodos() {
        return generoRepository.findAll();
    }

    // Buscar por ID
    public Optional<Genero> buscarPorId(Integer id) {
        return generoRepository.findById(id);
    }

    // Salvar
    public Genero salvar(Genero genero) {
        if(generoRepository.findByNome(genero.getNome()).isPresent()){
            throw new IllegalArgumentException("Genero já cadastrado.");
        }
        return generoRepository.save(genero);
    }

public Genero atualizar(Genero genero) {
    Optional<Genero> generoExistente = generoRepository.findById(genero.getId());
    if (!generoExistente.isPresent()) {
        throw new RuntimeException("Gênero não encontrado com id: " + genero.getId());
    }

    Genero generoAtualizado = generoExistente.get();
    generoAtualizado.setNome(genero.getNome());

    return generoRepository.save(generoAtualizado);
}

    // Deletar por ID
    public void deletar(Integer id) {
        if (generoRepository.existsById(id)) {
            generoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Gênero não encontrado com id: " + id);
        }
    }
}
