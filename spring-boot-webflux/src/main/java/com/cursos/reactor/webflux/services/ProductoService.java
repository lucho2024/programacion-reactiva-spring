package com.cursos.reactor.webflux.services;

import com.cursos.reactor.webflux.models.documents.Categoria;
import com.cursos.reactor.webflux.models.documents.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {


    public Flux<Producto> findAll();

    public Flux<Producto> findAllConNombreUpperCase();

    public Flux<Producto> findAllConNombreUpperCaseRepeat();

    public Mono<Producto> findById(String id);

    public Mono<Producto> save(Producto producto);

    public Mono<Void> delete(Producto producto);

    Flux<Categoria> findAllCategoria();

    Mono<Categoria>  findCategoriaById(String id);

    Mono<Categoria> saveCategoria(Categoria categoria);
}
