package com.cursos.reactor.webflux.models.dao;

import com.cursos.reactor.webflux.models.documents.Producto;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoDao extends ReactiveMongoRepository<Producto,String> {





}
