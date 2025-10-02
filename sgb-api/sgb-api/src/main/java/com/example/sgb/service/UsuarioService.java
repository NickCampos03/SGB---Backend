package com.example.sgb.service;

import com.example.sgb.model.Usuario;
import com.example.sgb.model.enums.Perfil;
import com.example.sgb.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> listarTodos(Perfil perfil) {
        if (perfil != null) {
            return usuarioRepository.findByPerfil(perfil);
        }
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    public Usuario criar(Usuario usuario, Authentication authentication) {
        if (!isPerfilValido(usuario.getPerfil())) {
            throw new IllegalArgumentException("Perfil inválido. Valores aceitos: USUARIO, BIBLIOTECARIO");
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado em outro usuário.");
        }
        if (temRestricaoBibliotecario(authentication, usuario.getPerfil())) {
            throw new SecurityException("BIBLIOTECARIO só pode criar usuários com perfil USUARIO");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(Integer id, Usuario usuario, Authentication authentication) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        if (!isPerfilValido(usuario.getPerfil())) {
            throw new IllegalArgumentException("Perfil inválido. Valores aceitos: USUARIO, BIBLIOTECARIO");
        }

        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioExistente.isPresent() && !usuarioExistente.get().getCodigologin().equals(id)) {
            throw new IllegalArgumentException("E-mail já cadastrado em outro usuário.");
        }
        if (temRestricaoBibliotecario(authentication, usuario.getPerfil())) {
            throw new SecurityException("BIBLIOTECARIO só pode editar usuários com perfil USUARIO");
        }

        usuario.setCodigologin(id);
        return usuarioRepository.save(usuario);
    }

    public void deletar(Integer id, Authentication authentication) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        if (authentication != null && usuario.getEmail().equals(authentication.getName())) {
            throw new SecurityException("Você não pode excluir a si mesmo.");
        }
        if (temRestricaoBibliotecario(authentication, usuario.getPerfil())) {
            throw new SecurityException("BIBLIOTECARIO só pode excluir usuários com perfil USUARIO");
        }

        usuarioRepository.deleteById(id);
    }

    private boolean temRestricaoBibliotecario(Authentication authentication, Perfil perfilDoAlvo) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIBLIOTECARIO"))
                && !"USUARIO".equals(perfilDoAlvo);
    }

    private boolean isPerfilValido(Perfil perfil) {
        for (Perfil p : Perfil.values()) {
            if (p.name().equals(perfil)) return true;
        }
        return false;
    }
}
