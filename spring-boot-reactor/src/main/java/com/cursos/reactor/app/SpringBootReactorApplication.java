package com.cursos.reactor.app;

import com.cursos.reactor.app.models.Comentario;
import com.cursos.reactor.app.models.Usuario;
import com.cursos.reactor.app.models.UsuarioConComentario;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        ejemploDelayElements();
    }

    public void ejemploIterable() throws Exception {

        List<String> usuariosList = Arrays.asList("andres soto", "camila obama", "luis hernandp", "luis montoya", "luis muñoz");
        Flux<String> nombres = Flux.fromIterable(usuariosList);
        // Flux<String> nombres = Flux.just("andres soto", "camila obama", "luis hernandp", "luis montoya", "luis muñoz");

        Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("luis"))
                .doOnNext(e -> {
                    if (e.getNombre().isEmpty()) {
                        throw new RuntimeException("Nombres no pueden ser vacios");
                    }
                    System.out.println(e.getNombre() + " " + e.getApellido());

                }).map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                });


        usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()),
                new Runnable() {
                    @Override
                    public void run() {
                        log.info("Ha finalizado la ejecucion del observable con exito");
                    }
                });
    }

    public void ejemploFlatMap() throws Exception {

        List<String> usuariosList = Arrays.asList("andres soto", "camila obama", "luis hernandp",
                "luis montoya", "luis muñoz");
        Flux.fromIterable(usuariosList).map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),
                        nombre.split(" ")[1].toUpperCase()))
                .flatMap(usuario -> {
                    if (usuario.getNombre().equalsIgnoreCase("luis")) {
                        return Mono.just(usuario);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                }).subscribe(u -> log.info(u.toString()));
    }

    public void ejemploToString() throws Exception {

        List<Usuario> usuariosList = Arrays.asList(new Usuario("andres", "soto"), new Usuario("camila", "obama")
                , new Usuario("luis", "hernando"),
                new Usuario("luis", "montoya"), new Usuario("luis", "muñoz"));
        Flux.fromIterable(usuariosList).map(usuario -> usuario.getNombre().concat(" ").concat(usuario.getApellido()))
                .flatMap(nombre -> {
                    if (nombre.contains("luis")) {
                        return Mono.just(nombre);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(nombre -> {

                    return nombre;
                }).subscribe(u -> log.info(u.toString()));
    }

    public void ejemploCollectList() throws Exception {

        List<Usuario> usuariosList = Arrays.asList(new Usuario("andres", "soto"), new Usuario("camila", "obama")
                , new Usuario("luis", "hernando"),
                new Usuario("luis", "montoya"), new Usuario("luis", "muñoz"));

        Flux.fromIterable(usuariosList)
                .collectList()
                .subscribe(lista -> lista.forEach(i -> log.info(i.toString())));
    }


    public Usuario crearUsuario() {
        return new Usuario("luis", "montoya");
    }

    public void ejemploUsuarioComentariosFlatMap() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());

        Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(()
                -> {
            Comentario comentario = new Comentario();
            comentario.addComentarios("hola");
            comentario.addComentarios("feo");
            comentario.addComentarios("bonito");
            comentario.addComentarios("chao");
            comentario.addComentarios("bye");
            return comentario;
        });

        usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioConComentario(u, c)))
                .subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploUsuarioComentariosZipWith() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());

        Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(()
                -> {
            Comentario comentario = new Comentario();
            comentario.addComentarios("hola");
            comentario.addComentarios("feo");
            comentario.addComentarios("bonito");
            comentario.addComentarios("chao");
            comentario.addComentarios("bye");
            return comentario;
        });

        usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario) ->
                        new UsuarioConComentario(usuario, comentariosUsuario))
                .subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploUsuarioComentariosZipWithForma2() {
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());

        Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(()
                -> {
            Comentario comentario = new Comentario();
            comentario.addComentarios("hola");
            comentario.addComentarios("feo");
            comentario.addComentarios("bonito");
            comentario.addComentarios("chao");
            comentario.addComentarios("bye");
            return comentario;
        });

        usuarioMono.zipWith(comentariosUsuarioMono)
                .map(tuple -> {
                    Usuario u = tuple.getT1();
                    Comentario c = tuple.getT2();
                    return new UsuarioConComentario(u, c);
                })
                .subscribe(uc -> log.info(uc.toString()));
    }

    public void ejemploUsuarioComentariosZipWithRangos() {
        Flux.just(1, 2, 3, 4)
                .map(i -> (i * 2))
                .zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux:" +
                        "%d, Segundo flux %d ", uno, dos))
                .subscribe(texto -> log.info(texto));
    }

    public void ejemploInterval() {
        Flux<Integer> rango = Flux.range(1, 12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso, (ra, re) -> ra)
                .doOnNext(i -> log.info(i.toString()))
                .blockLast();
    }

    public void ejemploDelayElements() {
        Flux<Integer> rango = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));

        rango.subscribe();
    }

    public void ejemploIntervalorInfinito() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(() -> latch.countDown())
                .flatMap(i -> {
                    if (i >= 5) {
                        return Flux.error(new InterruptedException("Solo, hasta 5!"));
                    }
                    return Flux.just(i);
                })
                .map(i -> "Hola" + i)
                .retry(2)
                .subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

        latch.await();
    }

    public void ejemploIntervalDesdeCreate() {
        Flux.create(emitter -> {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {

                        private Integer contador = 0;

                        @Override
                        public void run() {
                            emitter.next(++contador);
                            if (contador == 10) {
                                timer.cancel();
                                emitter.complete();
                            }
                        }
                    }, 1000, 1000);
                }).doOnNext(next -> log.info(next.toString()))
                .doOnComplete(() -> log.info("hemos terminado"))
                .subscribe();
    }

    public void ejemploContraPresion() {
        Flux.range(1, 10)
                .log()
                .subscribe(new Subscriber<Integer>() {

                    private Subscription s;
                    private Integer limite = 5;
                    private Integer consumido = 0;


                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.s = s;
                        s.request(limite);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        log.info(integer.toString());
                        consumido++;
                        if (consumido == limite) {
                            consumido = 0;
                            s.request(limite);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
