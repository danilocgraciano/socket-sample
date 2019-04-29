package com.example.util;

import android.os.AsyncTask;

import java.io.IOException;

public class TCPClientTask extends AsyncTask<Void, String, TCPClient> {

    private TCPClient tcpClient;
    private TCPClient.OnProgressListener listener;
    private String ip;
    private int port;

    public TCPClientTask(String ip, int port, TCPClient.OnProgressListener listener) {
        this.ip = ip;
        this.port = port;
        this.listener = listener;
    }

    @Override
    protected TCPClient doInBackground(Void... voids) {
        tcpClient = new TCPClient(new TCPClient.OnMessageListener() {
            @Override
            public void messageReceived(String message) {
                publishProgress(message);
            }
        });

        try {
            tcpClient.start(ip, port);
        } catch (IOException e) {
            publishProgress(e.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        listener.onProgressUpdate(values[0]);
    }

    public void desconectar() throws IOException {
        this.sendMessage(TCPClient.END);
    }

    public void sendMessage(String message) throws IOException {
        tcpClient.sendMessage(message);
    }

    public void closeConnection() throws IOException {
        if (tcpClient != null && tcpClient.isRunning())
            tcpClient.stop();
    }
}
