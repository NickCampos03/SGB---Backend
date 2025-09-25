package com.example.sgb.controller;

import com.example.sgb.model.Emprestimo;
import com.example.sgb.service.EmprestimoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    @Autowired
    private EmprestimoService emprestimoService;

    @GetMapping
    public List<Emprestimo> listarTodos(Authentication authentication) {
        boolean isAdminOuBib = hasRole(authentication, "ADMIN") || hasRole(authentication, "BIBLIOTECARIO");
        return emprestimoService.listarTodos(isAdminOuBib, authentication.getName());
    }

    @PostMapping
    public ResponseEntity<?> criarEmprestimo(@RequestBody Emprestimo emprestimo, Authentication authentication) {
        boolean isUsuario = hasRole(authentication, "USUARIO");
        return emprestimoService.criarEmprestimo(emprestimo, isUsuario, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<?> atualizarEmprestimo(@PathVariable Integer id, @RequestBody Emprestimo emprestimo) {
        return emprestimoService.atualizarEmprestimo(id, emprestimo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<?> deletarEmprestimo(@PathVariable Integer id) {
        return emprestimoService.deletarEmprestimo(id);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_" + role));
    }
}
