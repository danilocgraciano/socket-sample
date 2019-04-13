package com.server.model;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

	public Cliente() {
		setNome("DESCONHECIDO");
	}

	private String nome;

	private List<Mensagem> mensagens = new ArrayList<>();

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<Mensagem> getMensagens() {
		return mensagens;
	}

	public void setMensagens(List<Mensagem> mensagens) {
		this.mensagens = mensagens;
	}

}
