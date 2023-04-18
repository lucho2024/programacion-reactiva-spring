package com.cursos.reactor.webflux;

import com.cursos.reactor.webflux.models.dao.CategoriaDao;
import com.cursos.reactor.webflux.models.dao.ProductoDao;
import com.cursos.reactor.webflux.models.documents.Categoria;
import com.cursos.reactor.webflux.models.documents.Producto;
import com.cursos.reactor.webflux.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.util.Date;

@SpringBootApplication
@Slf4j
public class SpringBootWebfluxApplication implements CommandLineRunner {

    @Autowired
    private ProductoDao productoDao;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


        mongoTemplate.dropCollection("productos").subscribe(); //borra coleccion
        mongoTemplate.dropCollection("categorias").subscribe(); //borra coleccion

        Categoria categoria1 = new Categoria("electronicos");
        Categoria categoria2 = new Categoria("hogar");
        Categoria categoria3 = new Categoria("exteriores");
        Categoria categoria4 = new Categoria("interiores");

        Flux.just(categoria1, categoria2, categoria3, categoria4)
                .flatMap(categoria -> productoService.saveCategoria(categoria))
                .doOnNext(categoriaMono -> {
                    log.info("Insert : " + categoriaMono.getId() + " , "
                            + categoriaMono.getNombre());
                }).thenMany(Flux.just(Producto.builder().nombre("tarro").categoria(categoria1).precio(200.0).build(),
                                Producto.builder().nombre("pc").precio(500.0).categoria(categoria1).build(),
                                Producto.builder().nombre("mesa").precio(100.0).categoria(categoria2).build(),
                                Producto.builder().nombre("silla").precio(220.0).categoria(categoria3).build())

                        .flatMap(producto -> {
                            producto.setCreateAt(new Date());
                            return productoDao.save(producto);
                        }))
                .subscribe(productoMono -> log.info("Insert : " + productoMono.getId() + " , " + productoMono.getNombre() + " , "
                        + productoMono.getPrecio() + " , " + productoMono.getCreateAt()));


    }
}
