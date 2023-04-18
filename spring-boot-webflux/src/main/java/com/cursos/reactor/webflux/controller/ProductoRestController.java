package com.cursos.reactor.webflux.controller;

import com.cursos.reactor.webflux.models.dao.ProductoDao;
import com.cursos.reactor.webflux.models.documents.Producto;
import com.cursos.reactor.webflux.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api/productos")
public class ProductoRestController {

    @Autowired
    private ProductoService productoService;


    @GetMapping
    public Flux<Producto> index() {


        return productoService.findAllConNombreUpperCase();
    }

    @GetMapping("/{id}")
    public Mono<Producto> showDetail(@PathVariable("id") String id) {

        // Mono<Producto> producto= productoDao.findById(id);

        Flux<Producto> productos = productoService.findAll();

        Mono<Producto> producto = productos.filter(p -> p.getId().equals(id)).next();//next retonar un mono

        return producto;
    }

}
