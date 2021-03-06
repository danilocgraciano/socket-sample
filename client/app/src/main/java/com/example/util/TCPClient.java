package com.example.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;

    private boolean running;

    public boolean isRunning() {
        return this.running;
    }

    public static final String PING = "PING";
    public static final String END = "END";
    public static final String ACK = "ACK";
    public static final String ERROR = "ERROR";

    private OnMessageListener listener;

    public TCPClient(OnMessageListener listener) {
        this.listener = listener;
    }

    public void start(String ip, int port) throws IOException {

        InetAddress serverAddr = InetAddress.getByName(ip);
        clientSocket = new Socket(serverAddr, port);

        running = true;

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

        String inputLine;
        while (running) {

            ping();

            if (in.ready()) {
                if ((inputLine = in.readLine()) != null) {

                    if (END.equalsIgnoreCase(inputLine))
                        running = false;

                    this.listener.messageReceived(inputLine);

                }
            }

        }

        stop();

    }

    private void ping() {
        try {
            Thread.sleep(500);
            sendMessage(PING);
        } catch (IOException e) {
            Log.e("ERROR", e.getMessage());
        } catch (InterruptedException e) {
            Log.e("ERROR", e.getMessage());
        }
    }

    public void sendMessage(String message) throws IOException {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        } else {
            throw new IOException("Mensagem não enviada");
        }
    }

    public void stop() throws IOException {

        running = false;

        if (in != null)
            in.close();

        if (out != null)
            out.close();

        if (clientSocket != null && !clientSocket.isClosed())
            clientSocket.close();

    }

    public interface OnMessageListener {

        void messageReceived(String message);

    }

    public interface OnProgressListener {

        void onProgressUpdate(String... values);

    }

}