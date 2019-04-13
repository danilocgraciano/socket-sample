package com.example.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;

    private boolean connected;

    public boolean isConnected() {
        return this.connected;
    }

    public static final String END = "END";
    public static final String ACK = "ACK";
    public static final String ERROR = "ERROR";

    private OnMessageReceived listener;

    public TCPClient(OnMessageReceived listener) {
        this.listener = listener;
    }

    public void start(String ip, int port) throws IOException {

        InetAddress serverAddr = InetAddress.getByName(ip);
        clientSocket = new Socket(serverAddr, port);
        connected = true;

        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        char[] buff = new char[1024];
        int read;
        while ((read = in.read(buff)) != -1) {

            StringBuilder response = new StringBuilder();
            response.append(buff, 0, read);

            String inputLine = response.toString();
            this.listener.messageReceived(retiraCaracteresEspeciais(inputLine));
        }

    }

    public interface OnMessageReceived {

        void messageReceived(String message);
    }

    public void sendMessage(String message) throws IOException {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        } else {
            connected = false;
            throw new IOException("Mensagem não enviada");
        }
    }

    public void stop() throws IOException {
        if (isConnected()) {
            sendMessage(END);
            this.connected = false;
            if (in != null)
                in.close();

            if (out != null)
                out.close();

            if (clientSocket != null && !clientSocket.isClosed())
                clientSocket.close();
        }

    }

    private String retiraCaracteresEspeciais(String inputLine) {
        inputLine = inputLine.replace("\r", "").replace("\n", "").replace("\r\n", "");
        return inputLine;
    }

}