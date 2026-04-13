package com.example.procastinator.dao;

import java.util.List;

public interface GenericDAO<T, ID> {
    void salvar(T obj);
    T buscarPorId(ID id);
    List<T> listarTodos();
    void atualizar(T obj);
    void deletar(ID id);
}
