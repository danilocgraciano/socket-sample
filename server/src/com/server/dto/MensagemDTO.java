package com.server.dto;

import javafx.beans.property.SimpleStringProperty;

public class MensagemDTO {

	private SimpleStringProperty cliente;

	private SimpleStringProperty dataHora;

	private SimpleStringProperty texto;

	public MensagemDTO(String cliente, String dataHora, String texto) {
		this.cliente = new SimpleStringProperty(cliente);
		this.dataHora = new SimpleStringProperty(dataHora);
		this.texto = new SimpleStringProperty(texto);
	}

	public SimpleStringProperty clienteProperty() {
		return cliente;
	}

	public String getCliente() {
		return cliente.get();
	}

	public void setCliente(String cliente) {
		this.cliente.set(cliente);
	}

	public SimpleStringProperty dataHoraProperty() {
		return dataHora;
	}

	public String getDataHora() {
		return dataHora.get();
	}

	public void setDataHora(String dataHora) {
		this.dataHora.set(dataHora);
	}

	public SimpleStringProperty textoProperty() {
		return this.texto;
	}

	public void setTexto(String texto) {
		this.texto.set(texto);
	}

	public String getTexto() {
		return this.texto.get();
	}

	@Override
	public String toString() {
		return this.getCliente() + "|" + this.getDataHora() + "|" + this.getTexto();
	}

}
