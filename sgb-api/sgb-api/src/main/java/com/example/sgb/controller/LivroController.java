package com.example.sgb.controller;

import com.example.sgb.model.Livro;
import com.example.sgb.model.enums.Disponibilidade;
import com.example.sgb.service.LivroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivroController {

    @Autowired
    private LivroService livroService;

    // Lista livros com filtros opcionais por gênero e disponibilidade
    @GetMapping
    public List<Livro> listarTodos(@RequestParam(required = false) Integer generoId,
                                   @RequestParam(required = false) Disponibilidade disponibilidade) {
        return livroService.listarTodos(generoId, disponibilidade);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable Integer id) {
        return livroService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    @PostMapping
    public ResponseEntity<Livro> criar(@RequestBody Livro livro) {
        try {
            Livro salvo = livroService.salvar(livro);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizar(@PathVariable Integer id, @RequestBody Livro livro) {
        try {
            Livro atualizado = livroService.atualizar(id, livro);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        try {
            livroService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
