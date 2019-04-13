package com.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import com.server.model.Cliente;
import com.server.model.Mensagem;

public class TCPServer {

	public static final String END_OF_TRANSACTION = "END";

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
			handler.finalize();
		});

		activeConnections.clear();
	}

	private static class TCPClientHandler extends Thread {

		private Socket clientSocket;
		private PrintWriter out;
		private BufferedReader in;

		private boolean started;

		private static final int NOME = 0;
		private static final int DATA_HORA = 1;
		private static final int MENSAGEM = 2;

		private Cliente cliente = new Cliente();

		public Cliente getCliente() {
			return this.cliente;
		}

		public TCPClientHandler(List<TCPClientHandler> activeConnections, Socket socket) {
			this.clientSocket = socket;
			started = true;
			activeConnections.add(this);
		}

		public void run() {
			try {

				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				char[] buff = new char[1024];
				int read;
				while (started && (read = in.read(buff)) != -1) {

					StringBuilder response = new StringBuilder();
					response.append(buff, 0, read);

					String inputLine = response.toString();
					inputLine = retiraCaracteresEspeciais(inputLine);

					if (END_OF_TRANSACTION.startsWith(retiraCaracteresEspeciais(inputLine))) {
						out.println("Conexão finalizada!");
						break;
					}

					String[] data = inputLine.split("[|]");
					try {
						String nome = data[NOME];
						cliente.setNome(nome);
						cliente.getMensagens().add(new Mensagem(data[DATA_HORA], data[MENSAGEM]));
						out.println("Mensagem recebida!");
					} catch (ArrayIndexOutOfBoundsException e) {
						out.println("Mensagem fora do padrão especificado!");
					}

				}

				in.close();
				out.close();
				clientSocket.close();
			} catch (SocketException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		private String retiraCaracteresEspeciais(String inputLine) {
			inputLine = inputLine.replace("\r", "").replace("\n", "").replace("\r\n", "");
			return inputLine;
		}

		public void finalize() {
			started = false;
		}
	}
}
