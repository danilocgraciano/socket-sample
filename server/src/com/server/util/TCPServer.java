package com.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.server.model.Cliente;
import com.server.model.Mensagem;

public class TCPServer {

	public static final String PING = "PING";
	public static final String END = "END";
	public static final String ACK = "ACK";
	public static final String ERROR = "ERROR";

	private ServerSocket serverSocket;

	private List<TCPClientHandler> activeConnections = new ArrayList<>();

	public List<Cliente> getClientesConectados() {
		List<Cliente> clientes = new ArrayList<>();

		for (TCPClientHandler handler : activeConnections) {
			if (handler.isAlive())
				clientes.add(handler.getCliente());
		}

		return clientes;

	}

	public void start(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		while (true)
			if (!serverSocket.isClosed()) {
				new TCPClientHandler(activeConnections, serverSocket.accept()).start();
			}
	}

	public void stop() throws IOException {
		if (serverSocket != null && !serverSocket.isClosed())
			serverSocket.close();

		activeConnections.forEach((handler) -> {
			try {
				handler.finalizar();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		activeConnections.clear();
	}

	private static class TCPClientHandler extends Thread {

		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;

		private boolean running;

		private static final int NOME = 0;
		private static final int DATA_HORA = 1;
		private static final int MENSAGEM = 2;

		private LocalDateTime lastPing = LocalDateTime.now();

		private Cliente cliente = new Cliente();

		public Cliente getCliente() {
			return this.cliente;
		}

		public TCPClientHandler(List<TCPClientHandler> activeConnections, Socket socket) {
			this.clientSocket = socket;
			activeConnections.add(this);
		}

		public void run() {
			try {

				running = true;

				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				String inputLine;
				while (running) {

					if (!isConnected())
						finalizar();

					if (in.ready()) {
						if ((inputLine = in.readLine()) != null) {

							inputLine = retiraCaracteresEspeciais(inputLine);

							if (PING.startsWith(retiraCaracteresEspeciais(inputLine))) {
								refresh();
								continue;
							}

							if (END.startsWith(retiraCaracteresEspeciais(inputLine))) {
								finalizar();
								continue;
							}

							readData(inputLine);

							refresh();
						}
					}
					Thread.sleep(500);
				}

				out.println(END);
				Thread.sleep(500);

				in.close();
				out.close();
				clientSocket.close();

			} catch (SocketException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

		private void readData(String inputLine) {
			String[] data = inputLine.split("[|]");
			try {
				String nome = data[NOME];
				cliente.setNome(nome);
				cliente.getMensagens().add(new Mensagem(data[DATA_HORA], data[MENSAGEM]));
				out.println(ACK);
			} catch (ArrayIndexOutOfBoundsException e) {
				out.println(ERROR);
			}
		}

		private void refresh() {
			lastPing = LocalDateTime.now();
		}

		private boolean isConnected() {
			long minutes = lastPing.until(LocalDateTime.now(), ChronoUnit.MINUTES);
			return minutes < 1;
		}

		private String retiraCaracteresEspeciais(String inputLine) {
			inputLine = inputLine.replace("\r", "").replace("\n", "").replace("\r\n", "");
			return inputLine;
		}

		private void finalizar() throws IOException {
			running = false;
		}
	}
}
