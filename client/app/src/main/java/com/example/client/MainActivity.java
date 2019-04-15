package com.example.client;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.model.Mensagem;
import com.example.util.TCPClient;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    enum UIType {
        CONNECT, DISCONNECT
    }

    private TCPClient tcpClient;

    private EditText txtIP;
    private EditText txtPorta;
    private EditText txtNomeCliente;
    private EditText txtMensagem;

    private Button btnConectar;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtIP = findViewById(R.id.txtIp);
        txtPorta = findViewById(R.id.txtPorta);
        txtNomeCliente = findViewById(R.id.txtNomeCliente);
        txtMensagem = findViewById(R.id.txtMensagem);
        txtMensagem.setEnabled(false);

        btnConectar = findViewById(R.id.btnConectar);
        btnConectar.setText("Conectar");

        btnEnviar = findViewById(R.id.enviarMensagem);
        btnEnviar.setEnabled(false);

        btnConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btnConectar.getText().toString().equalsIgnoreCase("conectar")) {
                    conectar();
                } else {
                    if (btnConectar.getText().toString().equalsIgnoreCase("desconectar")) {
                        desconectar();
                    }
                }

            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cliente = txtNomeCliente.getText().toString().trim();
                String conteudo = txtMensagem.getText().toString().trim();

                if (conteudo.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Mensagem inválida", Toast.LENGTH_SHORT).show();
                    return;
                }

                Mensagem mensagem = new Mensagem(cliente, new Date(), conteudo);
                try {
                    tcpClient.sendMessage(mensagem.toString());
                    txtMensagem.setText("");
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    try {
                        closeConnection();
                        prepareUIFor(UIType.CONNECT);
                    } catch (IOException e1) {
                        Toast.makeText(MainActivity.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    private void desconectar() {
        try {
            tcpClient.sendMessage(TCPClient.END);
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
        } finally {
            prepareUIFor(UIType.CONNECT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (tcpClient != null && tcpClient.isRunning())
            desconectar();

    }

    private void prepareUIFor(UIType type) {

        if (type.equals(UIType.DISCONNECT)) {
            txtPorta.setEnabled(false);
            txtIP.setEnabled(false);
            txtNomeCliente.setEnabled(false);

            txtMensagem.setEnabled(true);
            txtMensagem.setText("");

            btnEnviar.setEnabled(true);

            btnConectar.setText("Desconectar");
        }
        if (type.equals(UIType.CONNECT)) {
            txtPorta.setEnabled(true);
            txtIP.setEnabled(true);
            txtNomeCliente.setEnabled(true);

            txtMensagem.setEnabled(false);
            txtMensagem.setText("");
            btnEnviar.setEnabled(false);

            btnConectar.setText("Conectar");
        }


    }

    private void conectar() {
        String ip = txtIP.getText().toString().trim();

        if (ip.isEmpty()) {
            Toast.makeText(MainActivity.this, "IP não informado", Toast.LENGTH_SHORT).show();
            return;
        }

        int port = 0;
        try {
            port = Integer.parseInt(txtPorta.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Porta inválida/não informada", Toast.LENGTH_SHORT).show();
            return;
        }

        String nomeCliente = txtNomeCliente.getText().toString().trim();
        if (nomeCliente.isEmpty()) {
            Toast.makeText(MainActivity.this, "Nome do cliente não informado", Toast.LENGTH_SHORT).show();
            return;
        }

        new TCPClientTask(ip, port).execute();

        prepareUIFor(UIType.DISCONNECT);
    }

    public class TCPClientTask extends AsyncTask<Void, String, TCPClient> {

        private String ip;
        private int port;

        public TCPClientTask(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        protected TCPClient doInBackground(Void... voids) {
            tcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
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

            switch (values[0]) {
                case TCPClient.END:
                    Toast.makeText(MainActivity.this, "Conexão finalizada", Toast.LENGTH_SHORT).show();
                    try {
                        closeConnection();
                        prepareUIFor(UIType.CONNECT);
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case TCPClient.ACK:
                    Toast.makeText(MainActivity.this, "Mensagem recebida", Toast.LENGTH_SHORT).show();
                    break;
                case TCPClient.ERROR:
                    Toast.makeText(MainActivity.this, "Ocorreu um erro na comunicação", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, values[0], Toast.LENGTH_SHORT).show();
                    try {
                        closeConnection();
                        prepareUIFor(UIType.CONNECT);
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

        }

    }

    private void closeConnection() throws IOException {
        if (tcpClient != null && tcpClient.isRunning())
            tcpClient.stop();
    }

}
