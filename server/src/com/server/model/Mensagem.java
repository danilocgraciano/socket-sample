package com.server.model;

public class Mensagem {

	public Mensagem(String dataHora, String texto) {
		this.dataHora = dataHora;
		this.texto = texto;
	}

	private String dataHora;

	private String texto;

	public String getDataHora() {
		return dataHora;
	}

	public void setDataHora(String dataHora) {
		this.dataHora = dataHora;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

}
