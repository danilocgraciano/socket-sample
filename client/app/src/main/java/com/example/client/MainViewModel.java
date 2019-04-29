package com.example.client;

import android.databinding.ObservableField;

import com.example.model.Mensagem;
import com.example.util.SingleLiveEvent;
import com.example.util.TCPClient;
import com.example.util.TCPClientTask;

import java.io.IOException;
import java.util.Date;

public class MainViewModel {

    private TCPClientTask tcpClientTask;

    public ObservableField<String> ip = new ObservableField<>();
    public ObservableField<String> porta = new ObservableField<>();
    public ObservableField<String> nomeCliente = new ObservableField<>();
    public ObservableField<String> mensagem = new ObservableField<>();

    public SingleLiveEvent<String> toastMessage = new SingleLiveEvent<>();
    public SingleLiveEvent<Boolean> conectado = new SingleLiveEvent<>();

    public void onEnviarClick() {

        String cliente = nomeCliente.get().trim();
        String conteudo = mensagem.get().trim();

        if (conteudo.isEmpty()) {
            toastMessage.setValue("Mensagem inválida");
            return;
        }

        Mensagem mensagem = new Mensagem(cliente, new Date(), conteudo);
        try {
            tcpClientTask.sendMessage(mensagem.toString());
        } catch (IOException e) {
            toastMessage.setValue(e.getMessage());
            try {
                tcpClientTask.closeConnection();
                conectado.setValue(false);
            } catch (IOException e1) {
                toastMessage.setValue(e1.getMessage());
            }
        }

    }

    public void onConectarClick() {

        if (conectado.getValue() == null || !conectado.getValue().booleanValue())
            conectar();
        else
            desconectar();

    }

    private void desconectar() {
        try {
            tcpClientTask.desconectar();
        } catch (IOException e) {
            toastMessage.setValue(e.getMessage());
        }
    }

    private void conectar() {

        String strIp = ip.get();
        if (strIp == null || strIp.trim().isEmpty()) {
            toastMessage.setValue("IP não informado");
            return;
        }

        String strPorta = porta.get();
        int intPorta = 0;
        try {
            intPorta = Integer.parseInt(strPorta);
        } catch (NumberFormatException e) {
            toastMessage.setValue("Porta inválida/não informada");
            return;
        }

        String strNomeCliente = nomeCliente.get();
        if (strNomeCliente == null || strNomeCliente.isEmpty()) {
            toastMessage.setValue("Nome do cliente não informado");
            return;
        }

        conectado.setValue(true);

        tcpClientTask = new TCPClientTask(strIp, intPorta, new TCPClient.OnProgressListener() {
            @Override
            public void onProgressUpdate(String... values) {
                switch (values[0]) {
                    case TCPClient.END:
                        toastMessage.setValue("Conexão finalizada");
                        conectado.setValue(false);
                        try {
                            tcpClientTask.closeConnection();
                        } catch (IOException e) {
                            toastMessage.setValue(e.getMessage());
                        }
                        break;
                    case TCPClient.ACK:
                        toastMessage.setValue("Mensagem recebida");
                        break;
                    case TCPClient.ERROR:
                        toastMessage.setValue("Ocorreu um erro na comunicação");
                        conectado.setValue(false);
                        break;
                    default:
                        toastMessage.setValue(values[0]);
                        conectado.setValue(false);
                        try {
                            tcpClientTask.closeConnection();
                        } catch (IOException e) {
                            toastMessage.setValue(e.getMessage());
                        }
                        break;
                }
            }
        });

        tcpClientTask.execute();

    }
}