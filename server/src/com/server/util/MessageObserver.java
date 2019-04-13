package com.server.util;

import com.server.model.MensagemDTO;

public interface MessageObserver {

	void onChange(MensagemDTO mensagem);
}
