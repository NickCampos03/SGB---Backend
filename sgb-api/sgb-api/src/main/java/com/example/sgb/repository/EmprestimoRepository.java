package com.example.sgb.repository;

import com.example.sgb.model.Emprestimo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer>, JpaSpecificationExecutor<Emprestimo> {
    List<Emprestimo> findByUsuario_CodigoLogin(Integer codigoLogin);   
    List<Emprestimo> findByLivro_CodigoLivro(Integer codigoLivro);

}
