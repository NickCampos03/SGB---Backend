package com.example.sgb.service;

import com.example.sgb.model.Emprestimo;
import com.example.sgb.model.Livro;
import com.example.sgb.model.Usuario;
import com.example.sgb.repository.EmprestimoRepository;
import com.example.sgb.repository.LivroRepository;
import com.example.sgb.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmprestimoService {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LivroRepository livroRepository;

    public List<Emprestimo> listarTodos(boolean isAdminOuBibliotecario, String emailUsuario) {
        if (isAdminOuBibliotecario) {
            return emprestimoRepository.findAll();
        } else {
            Integer codUsuario = usuarioRepository.findByEmail(emailUsuario)
                    .map(Usuario::getCodigologin)
                    .orElse(-1);
            return emprestimoRepository.findByUsuario_Codigologin(codUsuario);
        }
    }

    public ResponseEntity<?> criarEmprestimo(Emprestimo emprestimo, boolean isUsuario, String email) {
        // validação datas
        if (emprestimo.getDataderetirada() != null && emprestimo.getDataderetirada().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body("A data de retirada não pode ser no futuro.");
        }
        if (emprestimo.getDataPrevista() != null && emprestimo.getDataderetirada() != null &&
                emprestimo.getDataPrevista().isBefore(emprestimo.getDataderetirada())) {
            return ResponseEntity.badRequest().body("A data prevista não pode ser anterior à retirada.");
        }

        // livro
        if (emprestimo.getLivro() == null || emprestimo.getLivro().getCodigolivro() == null) {
            return ResponseEntity.badRequest().body("Livro não informado.");
        }
        Optional<Livro> livroOpt = livroRepository.findById(emprestimo.getLivro().getCodigolivro());
        if (livroOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Livro não encontrado.");
        }

        // usuário
        if (emprestimo.getUsuario() == null || emprestimo.getUsuario().getCodigologin() == null) {
            return ResponseEntity.badRequest().body("Usuário não informado.");
        }
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(emprestimo.getUsuario().getCodigologin());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        if (isUsuario) {
            Integer codUsuarioLogado = usuarioRepository.findByEmail(email).map(Usuario::getCodigologin).orElse(-1);
            if (!usuario.getCodigologin().equals(codUsuarioLogado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário só pode criar empréstimo para si mesmo.");
            }
        }

        emprestimo.setLivro(livroOpt.get());
        emprestimo.setUsuario(usuario);
        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    public ResponseEntity<?> atualizarEmprestimo(Integer id, Emprestimo emprestimo) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(id);
        if (emprestimoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empréstimo não encontrado.");
        }

        Emprestimo existente = emprestimoOpt.get();
        LocalDate novaDataEntrega = emprestimo.getDatadeentrega();

        if (novaDataEntrega != null && existente.getDataderetirada() != null &&
                novaDataEntrega.isBefore(existente.getDataderetirada())) {
            return ResponseEntity.badRequest().body("Data de entrega não pode ser anterior à retirada.");
        }

        existente.setDatadeentrega(novaDataEntrega);
        Emprestimo salvo = emprestimoRepository.save(existente);

        return ResponseEntity.ok(salvo);
    }

    public ResponseEntity<?> deletarEmprestimo(Integer id) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(id);
        if (emprestimoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        emprestimoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
