package com.example.sgb.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sgb.model.Genero;
import com.example.sgb.repository.GeneroRepository;

@Service
public class GeneroService {

    @Autowired
    private GeneroRepository generoRepository;

    public List<Genero> listarTodos() {
        return generoRepository.findAll();
    }
}
