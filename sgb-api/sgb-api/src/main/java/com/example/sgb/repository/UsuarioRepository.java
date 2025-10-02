package com.example.sgb.repository;

import com.example.sgb.model.Usuario;
import com.example.sgb.model.enums.Perfil;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByPerfil(Perfil perfil);
}
