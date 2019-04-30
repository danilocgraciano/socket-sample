package com.example.client;

import android.arch.lifecycle.Observer;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.client.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

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
        binding.txtMensagem.setEnabled(false);
        binding.btnEnviar.setEnabled(false);
    }

    private void refresh(@Nullable Boolean conectado) {

        binding.txtPorta.setEnabled(!conectado);
        binding.txtIp.setEnabled(!conectado);
        binding.txtNomeCliente.setEnabled(!conectado);

        binding.txtMensagem.setEnabled(conectado);
        binding.txtMensagem.setText("");

        binding.btnEnviar.setEnabled(conectado);

        binding.btnConectar.setText((conectado) ? "Desconectar" : "Conectar");
    }

    public void onConectarClick(View view) {
        binding.getViewModel().onConectarClick();
    }

    public void onEnviarClick(View view) {
        binding.getViewModel().onEnviarClick();
        binding.txtMensagem.setText("");
    }

}