package com.server.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.server.model.Mensagem;
import com.server.model.MensagemDTO;
import com.server.util.TCPServer;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class MensagemListController implements Initializable {

	@FXML
	private TableView<MensagemDTO> tabela;

	@FXML
	private TableColumn<MensagemDTO, String> colCliente;

	@FXML
	private TableColumn<MensagemDTO, String> colDataHora;

	@FXML
	private TableColumn<MensagemDTO, String> colMensagem;

	@FXML
	private Button btnStartStop;

	@FXML
	private Button btnListar;

	@FXML
	private TextField txtPorta;

	private boolean started;

	private TCPServer tcpServer;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
		colDataHora.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
		colMensagem.setCellValueFactory(new PropertyValueFactory<>("texto"));

		txtPorta.setText("80");
		btnListar.setDisable(true);

		btnStartStop.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				String porta = getPorta();

				if (!campoValido(porta)) {
					mostraErroPortaInvalida();
				} else {
					if (servidorIniciado()) {
						terminaServidor();
					} else {
						iniciaServidor();
					}
				}

			}

		});

		btnListar.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				if (started) {

					tabela.getItems().clear();

					tcpServer.getClientesConectados().forEach((cliente) -> {

						List<Mensagem> mensagens = cliente.getMensagens();

						for (Mensagem mensagem : mensagens)
							tabela.getItems().add(
									new MensagemDTO(cliente.getNome(), mensagem.getDataHora(), mensagem.getTexto()));

					});
				}

			}

		});

	}

	private String getPorta() {
		return txtPorta.getText().trim();
	}

	private boolean campoValido(String porta) {

		if (porta.trim().isEmpty())
			return false;

		return porta.replaceAll("[0-9]", "").trim().isEmpty();
	}

	private void mostraErroPortaInvalida() {
		Alert dialogoErro = new Alert(Alert.AlertType.ERROR);
		dialogoErro.setTitle("Erro");
		dialogoErro.setHeaderText("Atenção");
		dialogoErro.setContentText("O Campo porta deve conter apenas números!");
		dialogoErro.showAndWait();
	}

	private boolean servidorIniciado() {
		return started;
	}

	private void terminaServidor() {
		started = false;
		txtPorta.setEditable(true);
		btnStartStop.setText("Iniciar Servidor");
		btnListar.setDisable(true);
		txtPorta.requestFocus();

		try {
			tcpServer.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void iniciaServidor() {

		started = true;
		txtPorta.setEditable(false);
		btnStartStop.setText("Parar Servidor");
		btnListar.setDisable(false);

		new Thread(new Runnable() {
			public void run() {
				try {
					tcpServer = new TCPServer();
					tcpServer.start(Integer.parseInt(getPorta()));
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}).start();

	}

}
