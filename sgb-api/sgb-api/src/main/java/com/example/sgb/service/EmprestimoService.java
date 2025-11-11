package com.example.sgb.service;

import com.example.sgb.model.Emprestimo;
import com.example.sgb.model.Livro;
import com.example.sgb.model.Usuario;
import com.example.sgb.model.enums.Disponibilidade;
import com.example.sgb.repository.EmprestimoRepository;
import com.example.sgb.repository.LivroRepository;
import com.example.sgb.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        List<Emprestimo> emprestimos;

        if (isAdminOuBibliotecario) {
            emprestimos = emprestimoRepository.findAll();
        } else {
            Integer codUsuario = usuarioRepository.findByEmail(emailUsuario)
                    .map(Usuario::getCodigoLogin)
                    .orElse(-1);
            emprestimos = emprestimoRepository.findByUsuario_CodigoLogin(codUsuario);
        }

        // ✅ Calcula atraso e saldo devedor antes de retornar
        emprestimos.forEach(this::calcularAtrasoEMulta);

        emprestimoRepository.saveAll(emprestimos);
        
        return emprestimos;
    }

    public ResponseEntity<?> criarEmprestimo(Emprestimo emprestimo, boolean isUsuario, String email) {

        // Validação das datas
        if (emprestimo.getDataDeRetirada() != null && emprestimo.getDataDeRetirada().isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body("A data de retirada não pode ser no futuro.");
        }
        if (emprestimo.getDataPrevista() != null && emprestimo.getDataDeRetirada() != null &&
                emprestimo.getDataPrevista().isBefore(emprestimo.getDataDeRetirada())) {
            return ResponseEntity.badRequest().body("A data prevista não pode ser anterior à retirada.");
        }

        // Verifica livro
        if (emprestimo.getLivro() == null || emprestimo.getLivro().getCodigoLivro() == null) {
            return ResponseEntity.badRequest().body("Livro não informado.");
        }
        Optional<Livro> livroOpt = livroRepository.findById(emprestimo.getLivro().getCodigoLivro());
        if (livroOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Livro não encontrado.");
        }
        Livro livro = livroOpt.get();

        if (livro.getDisponibilidade() == Disponibilidade.INDISPONIVEL) {
            return ResponseEntity.badRequest().body("O livro selecionado está indisponível para empréstimo.");
        }

        // Verifica usuário
        if (emprestimo.getUsuario() == null || emprestimo.getUsuario().getCodigoLogin() == null) {
            return ResponseEntity.badRequest().body("Usuário não informado.");
        }
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(emprestimo.getUsuario().getCodigoLogin());
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        if (isUsuario) {
            Integer codUsuarioLogado = usuarioRepository.findByEmail(email)
                    .map(Usuario::getCodigoLogin)
                    .orElse(-1);
            if (!usuario.getCodigoLogin().equals(codUsuarioLogado)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Usuário só pode criar empréstimo para si mesmo.");
            }
        }

        // ✅ Verifica se o usuário está devendo ou atingiu o limite
        List<Emprestimo> emprestimosUsuario = emprestimoRepository.findByUsuario_CodigoLogin(usuario.getCodigoLogin());
        boolean temAtraso = emprestimosUsuario.stream().anyMatch(Emprestimo::isEmAtraso);
        if (temAtraso) {
            return ResponseEntity.badRequest().body("Usuário possui empréstimos em atraso e não pode realizar novos.");
        }

        long emprestimosAtivos = emprestimosUsuario.stream()
                .filter(e -> e.getDataDeEntrega() == null)
                .count();

        if (emprestimosAtivos >= 3) {
            return ResponseEntity.badRequest().body("Usuário já possui 3 empréstimos ativos. Limite atingido.");
        }

        // Marca o livro como indisponível
        livro.setDisponibilidade(Disponibilidade.INDISPONIVEL);
        livroRepository.save(livro);

        emprestimo.setLivro(livro);
        emprestimo.setUsuario(usuario);
        emprestimo.setDataDeRetirada(LocalDate.now());
        emprestimo.setDataPrevista(LocalDate.now().plusDays(14));

        Emprestimo salvo = emprestimoRepository.save(emprestimo);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    public ResponseEntity<?> atualizarEmprestimo(Integer id, Emprestimo emprestimo) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(id);
        if (emprestimoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empréstimo não encontrado.");
        }

        Emprestimo existente = emprestimoOpt.get();
        LocalDate novaDataEntrega = emprestimo.getDataDeEntrega();

        if (novaDataEntrega != null && existente.getDataDeRetirada() != null &&
                novaDataEntrega.isBefore(existente.getDataDeRetirada())) {
            return ResponseEntity.badRequest().body("Data de entrega não pode ser anterior à retirada.");
        }

        existente.setDataDeEntrega(novaDataEntrega);

        // ✅ Recalcula multa/atraso ao atualizar
        calcularAtrasoEMulta(existente);

        Emprestimo salvo = emprestimoRepository.save(existente);

        if (novaDataEntrega != null) {
            Livro livro = existente.getLivro();
            existente.setEmAtraso(false);
            existente.setValorDevendo(0.0);
            livro.setDisponibilidade(Disponibilidade.DISPONIVEL);
            livroRepository.save(livro);
        }

        return ResponseEntity.ok(salvo);
    }

    public ResponseEntity<?> deletarEmprestimo(Integer id) {
        Optional<Emprestimo> emprestimoOpt = emprestimoRepository.findById(id);
        if (emprestimoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Emprestimo emprestimo = emprestimoOpt.get();

        // Garante que o livro fique disponível ao excluir o empréstimo
        Livro livro = emprestimo.getLivro();
        if (livro != null) {
            livro.setDisponibilidade(Disponibilidade.DISPONIVEL);
            livroRepository.save(livro);
        }

        emprestimoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void calcularAtrasoEMulta(Emprestimo emprestimo) {
        if (emprestimo.getDataPrevista() == null)
            return;

        long diasAtraso = ChronoUnit.DAYS.between(emprestimo.getDataPrevista(), LocalDate.now());
        if (diasAtraso > 0) {
            emprestimo.setEmAtraso(true);
            emprestimo.setValorDevendo(diasAtraso * 0.5); // 0.5 = multa diária
        } else {
            emprestimo.setEmAtraso(false);
            emprestimo.setValorDevendo(0.0);
        }
    }
    public List<Emprestimo> buscarFiltrado(boolean isAdminOuBib, String username,
                                        Integer codigoLivro, Integer usuario,
                                        Boolean emAtraso, Boolean entregue) {

        if (!isAdminOuBib) {
            Usuario u = usuarioRepository.findByEmail(username).orElseThrow();
            usuario = u.getCodigoLogin();
        }

        List<Emprestimo> emprestimos;

        if (usuario != null) {
            emprestimos = emprestimoRepository.findByUsuario_CodigoLogin(usuario);
        } else {
            emprestimos = emprestimoRepository.findAll();
        }

        if (codigoLivro != null) {
            emprestimos = emprestimos.stream()
                    .filter(e -> e.getLivro().getCodigoLivro().equals(codigoLivro))
                    .toList();
        }

        // ✅ Recalcula o atraso e multa antes de aplicar filtro de atraso
        emprestimos.forEach(this::calcularAtrasoEMulta);

        if (emAtraso != null) {
            emprestimos = emprestimos.stream()
                    .filter(e -> e.isEmAtraso() == emAtraso)
                    .toList();
        }

        if (Boolean.TRUE.equals(entregue)) {
            emprestimos = emprestimos.stream()
                    .filter(e -> e.getDataDeEntrega() != null)
                    .toList();
        }

        return emprestimos;
    }

}
