package com.cursos.reactor.webflux.models.dao;

import com.cursos.reactor.webflux.models.documents.Categoria;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria,String> {
}
