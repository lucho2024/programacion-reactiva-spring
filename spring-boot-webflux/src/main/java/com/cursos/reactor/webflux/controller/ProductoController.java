package com.cursos.reactor.webflux.controller;

import com.cursos.reactor.webflux.models.dao.ProductoDao;
import com.cursos.reactor.webflux.models.documents.Categoria;
import com.cursos.reactor.webflux.models.documents.Producto;
import com.cursos.reactor.webflux.services.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Controller
@Slf4j
@SessionAttributes("producto")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Value("${config.uploads.path}")
    private String path;

    @ModelAttribute("categorias")//asi se traen las categoria y se mapean al attributo categoria para que se muestre
    public Flux<Categoria> categorias() {
        return productoService.findAllCategoria();
    }

    @GetMapping({"/listar", "/"})
    public String listar(Model model) {


        Flux<Producto> productos = productoService.findAllConNombreUpperCase();

        productos.subscribe(producto -> log.info(producto.getNombre()));

        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "listado de productos");


        return "listar";
    }

    @GetMapping("/listar-dataDriver")
    public String listarDataDriver(Model model) {


        Flux<Producto> productos = productoService.findAllConNombreUpperCase()
                .delayElements(Duration.ofSeconds(1));

        productos.subscribe(producto -> log.info(producto.getNombre()));

        model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
        model.addAttribute("titulo", "listado de productos");

        return "listar";
    }

    @GetMapping("/listar-full")
    public String listarFull(Model model) {


        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();


        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "listado de productos");


        return "listar";
    }

    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {


        Flux<Producto> productos = productoService.findAllConNombreUpperCaseRepeat();


        model.addAttribute("productos", productos);
        model.addAttribute("titulo", "listado de productos");


        return "listar-chunked";
    }

    @GetMapping("/form")
    public Mono<String> crear(Model model) {
        model.addAttribute("producto", Producto.builder().build());
        model.addAttribute("titulo", "Formulario de producto");
        return Mono.just("form");
    }

    @GetMapping("/form/{id}")
    public Mono<String> editar(@PathVariable String id, Model model) {

        Mono<Producto> productoMono = productoService.findById(id)
                .doOnNext(p -> {
                    log.info("Producto : " + p.getNombre());
                }).defaultIfEmpty(Producto.builder().build());

        model.addAttribute("titulo", "Editar producto");
        model.addAttribute("producto", productoMono);
        return Mono.just("form");
    }

    @GetMapping("/form/-v2{id}")
    public Mono<String> editarV2(@PathVariable String id, Model model) {

        return productoService.findById(id)
                .doOnNext(p -> {
                    log.info("Producto : " + p.getNombre());
                    model.addAttribute("titulo", "Editar producto");
                    model.addAttribute("producto", p);

                }).defaultIfEmpty(Producto.builder().build())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto"));
                    }
                    return Mono.just(p);
                })
                .thenReturn("listar")
                .onErrorResume(
                        ex -> Mono.just("redirect:/listar?error=no+existe+el+producto")
                );


    }


    @PostMapping("/form")
    public Mono<String> guardar(@Valid Producto producto, BindingResult bindingResult,
                                Model model,
                                @RequestPart FilePart file,
                                SessionStatus status
    ) {


        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "errores en el formulario producto");
            model.addAttribute("boton", "Guardar");
            return Mono.just("form");
        } else {
            status.setComplete();


            Mono<Categoria> categoriaMono = productoService.findCategoriaById(producto.getCategoria().getId());

            return categoriaMono.flatMap(c -> {
                        if (producto.getCreateAt() == null) {
                            producto.setCreateAt(new Date());
                        }
                        if (!file.filename().isEmpty()) {
                            producto.setFoto(UUID.randomUUID().toString().concat("")
                                    .concat(file.filename())
                                    .replace(" ", "").replace(":", "")
                                    .replace("\\", ""));
                        }
                        producto.setCategoria(c);
                        return productoService.save(producto);
                    })

                    .doOnNext(p -> log.info("Producto guardado : " + p.getNombre() + " id :" + p.getId()))
                    .flatMap(p -> {
                                if (!file.filename().isEmpty()) {

                                    return file.transferTo(
                                            new File(path
                                                    + p.getNombre()));
                                }
                                return Mono.just(p);
                            }
                    )
                    .thenReturn("redirect:/listar?success=producto+guardado+exitosamente");
        }


    }

    @GetMapping("eliminar/{id}")
    public Mono<String> eliminar(@PathVariable String id) {

        return productoService.findById(id)
                .defaultIfEmpty(Producto.builder().build())
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                    }
                    return Mono.just(p);
                })
                .flatMap(p -> {
                    log.info("eliminado producto : " + p.getNombre());
                    log.info("eliminado producto id : " + p.getId());
                    return productoService.delete(p);
                }).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
                .onErrorResume(
                        ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar")
                );

    }

    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable String id) {

        return productoService.findById(id)
                .doOnNext(p -> {
                            model.addAttribute("producto", p);
                            model.addAttribute("Titulo", "Detalle del producto");
                        }
                ).switchIfEmpty(Mono.just(Producto.builder().build()))
                .flatMap(p -> {
                    if (p.getId() == null) {
                        return Mono.error(new InterruptedException("No exites el producto"));
                    }
                    return Mono.just(p);
                }).then(Mono.just("ver"))
                .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

    @GetMapping("/uploads/img/{nombreFoto:.+")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto)
            throws MalformedURLException {
        Path ruta = Paths.get(path).resolve(nombreFoto).toAbsolutePath();

        Resource imagen = new UrlResource(String.valueOf(ruta));

        return Mono.just(
                ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                                + imagen.getFilename() + "\"")
                        .body(imagen)
        );


    }

}
