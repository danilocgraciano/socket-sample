package com.example.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Mensagem {

    private String cliente;

    private Date data;

    private String conteudo;

    private SimpleDateFormat sdf;

    public Mensagem(String cliente, Date data, String conteudo) {
        this.cliente = cliente;
        this.data = data;
        this.conteudo = conteudo;

        sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    }

    @Override
    public String toString() {

        return this.cliente + "|" + sdf.format(this.data) + "|" + this.conteudo;
    }
}
