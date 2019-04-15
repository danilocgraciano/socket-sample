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
import java.util.Collections;
import java.util.List;

import com.server.model.Cliente;
import com.server.model.Mensagem;

public class TCPServer {

	public static final String PING = "PING";
	public static final String END = "END";
	public static final String ACK = "ACK";
	public static final String ERROR = "ERROR";

	private ServerSocket serverSocket;

	private final List<TCPClientHandler> activeConnections = Collections.synchronizedList(new ArrayList<>());

	private MessageListener listener;

	public TCPServer(MessageListener listener) {
		this.listener = listener;

	}

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
				new TCPClientHandler(activeConnections, serverSocket.accept(), this.listener).start();
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
		private MessageListener listener;

		private List<TCPClientHandler> activeConnections;

		public Cliente getCliente() {
			return this.cliente;
		}

		public TCPClientHandler(List<TCPClientHandler> activeConnections, Socket socket, MessageListener listener) {
			this.clientSocket = socket;
			this.listener = listener;
			this.activeConnections = activeConnections;
		}

		public void run() {
			try {

				running = true;
				activeConnections.add(this);

				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

				String inputLine;
				while (running) {

					if (!isConnected())
						finalizar();

					if (in.ready()) {
						if ((inputLine = in.readLine()) != null) {

							if (PING.equalsIgnoreCase(inputLine)) {
								refresh();
								continue;
							}

							if (END.equalsIgnoreCase(inputLine)) {
								finalizar();
								notificar();
								continue;
							}

							readData(inputLine);

							notificar();

							refresh();

						}
					}
					Thread.sleep(500);
				}

				activeConnections.remove(this);
				notificar();

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

		private void finalizar() throws IOException {
			running = false;
		}

		private void notificar() {
			if (listener != null)
				listener.onMessage();
		}
	}
}
