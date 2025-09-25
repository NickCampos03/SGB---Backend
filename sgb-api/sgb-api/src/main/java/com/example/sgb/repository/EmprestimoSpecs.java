package com.example.sgb.repository;

import com.example.sgb.model.Emprestimo;
import com.example.sgb.model.enums.Disponibilidade;

import org.springframework.data.jpa.domain.Specification;


public class EmprestimoSpecs {

    public static Specification<Emprestimo> comCodUsuario(Integer codUsuario) {
        return (root, query, cb) -> codUsuario == null ? null : cb.equal(root.get("usuario").get("codigologin"), codUsuario);
    }

    public static Specification<Emprestimo> comCodLivro(Integer codLivro) {
        return (root, query, cb) -> codLivro == null ? null : cb.equal(root.get("livro").get("codigolivro"), codLivro);
    }

    public static Specification<Emprestimo> comDisponibilidade(Disponibilidade disponibilidade) {
        return (root, query, cb) -> disponibilidade == null ? null : cb.equal(root.get("disponibilidade"), disponibilidade);
    }

    public static Specification<Emprestimo> entregue(Boolean entregue) {
        return (root, query, cb) -> {
            if (entregue == null) return null;
            if (entregue) {
                return cb.isNotNull(root.get("datadeentrega"));
            } else {
                return cb.isNull(root.get("datadeentrega"));
            }
        };
    }

    public static Specification<Emprestimo> emAtraso(Boolean emAtraso) {
        return (root, query, cb) -> {
            if (emAtraso == null) return null;
            if (emAtraso) {
                return cb.isTrue(root.get("emAtraso"));
            } else {
                return cb.isFalse(root.get("emAtraso"));
            }
        };
    }
}
