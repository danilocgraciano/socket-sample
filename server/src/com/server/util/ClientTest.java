package com.server.util;

import com.server.dto.MensagemDTO;

public class ClientTest {

	public static void main(String[] args) throws Exception {
		TCPClient client = new TCPClient();
		client.startConnection("127.0.0.1", 80);

		String msg1 = client.sendMessage(new MensagemDTO("usario3", "2019-04-11", "mensagem 1").toString());
		String msg2 = client.sendMessage(new MensagemDTO("usario3", "2019-04-12", "mensagem 2").toString());
		String terminate = client.sendMessage(TCPServer.END);

		System.out.println(msg1);
		System.out.println(msg2);
		System.out.println(terminate);
	}

}
