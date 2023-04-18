package com.cursos.reactor.app.models;

public class UsuarioConComentario {


    private Usuario usuario;

    private  Comentario comentario;


    public UsuarioConComentario(Usuario usuario, Comentario comentario) {
        this.usuario = usuario;
        this.comentario = comentario;
    }


    @Override
    public String toString() {
        return "UsuarioConComentario{" +
                "usuario=" + usuario +
                ", comentario=" + comentario +
                '}';
    }
}
