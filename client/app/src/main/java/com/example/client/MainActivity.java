package com.example.client;

import android.arch.lifecycle.Observer;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.client.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    private EditText txtIP;
    private EditText txtPorta;
    private EditText txtNomeCliente;
    private EditText txtMensagem;

    private Button btnConectar;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mainViewModel = new MainViewModel();
        mainViewModel.toastMessage.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        mainViewModel.conectado.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean conectado) {
                refresh(conectado);
            }
        });


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(mainViewModel);
        binding.executePendingBindings();

        initialize();

    }

    private void initialize() {
        txtIP = findViewById(R.id.txtIp);
        txtPorta = findViewById(R.id.txtPorta);
        txtNomeCliente = findViewById(R.id.txtNomeCliente);
        txtMensagem = findViewById(R.id.txtMensagem);
        txtMensagem.setEnabled(false);

        btnConectar = findViewById(R.id.btnConectar);

        btnEnviar = findViewById(R.id.enviarMensagem);
        btnEnviar.setEnabled(false);
    }

    private void refresh(@Nullable Boolean conectado) {

        txtPorta.setEnabled(!conectado);
        txtIP.setEnabled(!conectado);
        txtNomeCliente.setEnabled(!conectado);

        txtMensagem.setEnabled(conectado);
        txtMensagem.setText("");

        btnEnviar.setEnabled(conectado);

        btnConectar.setText((conectado) ? "Desconectar" : "Conectar");
    }

    public void onConectarClick(View view) {
        binding.getViewModel().onConectarClick();
    }

    public void onEnviarClick(View view) {
        binding.getViewModel().onEnviarClick();
        txtMensagem.setText("");
    }

}