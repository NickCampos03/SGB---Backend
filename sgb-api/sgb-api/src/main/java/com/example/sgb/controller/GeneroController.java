package com.example.sgb.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sgb.model.Genero;
import com.example.sgb.service.GeneroService;

@RestController
@RequestMapping("/generos")
public class GeneroController {

    @Autowired
    private GeneroService generoService;

    @GetMapping
    public List<Genero> listarTodos() {
        return generoService.listarTodos();
    }
}
