package com.cursos.reactor.webflux.models.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "productos")
@Getter
@Setter
@Builder
public class Producto {


    @Id
    private String id;

    @NotEmpty
    private  String nombre;

    @NotNull
    private  Double precio;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;

    @Valid
    private Categoria categoria;

    private String foto;


}
