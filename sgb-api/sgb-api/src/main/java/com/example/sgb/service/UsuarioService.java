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
        // validação do perfil
        if (usuario.getPerfil() == null) {
            usuario.setPerfil(Perfil.USUARIO);
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        // restrição de bibliotecário
        if (isBibliotecario(authentication) && usuario.getPerfil() != Perfil.USUARIO) {
            throw new SecurityException("BIBLIOTECARIO só pode criar usuários com perfil USUARIO.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(Integer id, Usuario usuario, Authentication authentication) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // restrição de bibliotecário
        if (isBibliotecario(authentication) && existente.getPerfil() != Perfil.USUARIO) {
            throw new SecurityException("BIBLIOTECARIO só pode editar usuários com perfil USUARIO.");
        }
        String emailLogado = authentication.getName();
        Usuario logado = usuarioRepository.findByEmail(emailLogado)
                .orElseThrow(() -> new SecurityException("Usuário autenticado não encontrado."));

        // regra: somente ADMIN pode alterar o perfil
        if (logado.getPerfil() != Perfil.ADMIN) {
            // mantém o perfil antigo
            usuario.setPerfil(existente.getPerfil());
        }
        // mantém senha antiga se não vier nova
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            usuario.setSenha(existente.getSenha());
        } else {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }

        existente.setNome(usuario.getNome());
        existente.setEmail(usuario.getEmail());
        existente.setTelefone(usuario.getTelefone());
        existente.setIdade(usuario.getIdade());
        existente.setPerfil(usuario.getPerfil());

        return usuarioRepository.save(existente);
    }

    public void deletar(Integer id, Authentication authentication) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (authentication != null && usuario.getEmail().equals(authentication.getName())) {
            throw new SecurityException("Você não pode excluir a si mesmo.");
        }

        // restrição de bibliotecário
        if (isBibliotecario(authentication) && usuario.getPerfil() != Perfil.USUARIO) {
            throw new SecurityException("BIBLIOTECARIO só pode excluir usuários com perfil USUARIO.");
        }

        usuarioRepository.deleteById(id);
    }

    private boolean isBibliotecario(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIBLIOTECARIO"));
    }
}
