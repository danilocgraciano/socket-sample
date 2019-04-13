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
                        try {
                            desconectar();
                        } catch (IOException e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String cliente = txtNomeCliente.getText().toString().trim();
                String conteudo = txtMensagem.getText().toString().trim();

                Mensagem mensagem = new Mensagem(cliente, new Date(), conteudo);
                try {
                    tcpClient.sendMessage(mensagem.toString());
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void desconectar() throws IOException {

        if (tcpClient.isConnected())
            tcpClient.stop();

        txtPorta.setEnabled(true);
        txtIP.setEnabled(true);
        txtNomeCliente.setEnabled(true);

        txtMensagem.setText("");
        txtMensagem.setEnabled(false);
        btnEnviar.setEnabled(false);
        btnConectar.setText("Conectar");

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

        txtPorta.setEnabled(false);
        txtIP.setEnabled(false);
        txtNomeCliente.setEnabled(false);

        txtMensagem.setEnabled(true);
        btnEnviar.setEnabled(true);

        btnConectar.setText("Desconectar");
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

            if (!tcpClient.isConnected()) {
                try {
                    desconectar();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            if (values != null) {
                Toast.makeText(MainActivity.this, values[0], Toast.LENGTH_SHORT).show();
            }

        }
    }

}
