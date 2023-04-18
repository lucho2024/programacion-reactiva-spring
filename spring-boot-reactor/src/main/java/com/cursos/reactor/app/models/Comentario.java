package com.cursos.reactor.app.models;

import java.util.ArrayList;
import java.util.List;

public class Comentario {


    private List<String> comentarios;


    public Comentario() {
        this.comentarios = new ArrayList<>();
    }


    public List<String> getComentarios() {
        return comentarios;
    }

    public void addComentarios(String comentario) {
        comentarios.add(comentario);
    }

    @Override
    public String toString() {
        return "Comentario{" +
                "comentarios=" + comentarios +
                '}';
    }
}
