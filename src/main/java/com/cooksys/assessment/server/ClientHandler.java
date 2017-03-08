package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private Server server;

	public ClientHandler(Socket socket, Server server) {
		super();
		this.socket = socket;
		this.server = server;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				switch (message.getCommand()) {
					case "connect":
						server.addHandler(this);
						log.info("user <{}> connected", message.getUsername());
						message.setContents("user <"+message.getUsername()+"> connected" );
						server.broadcastSend(message);
						break;
					case "disconnect":
						server.removeHandler(this);
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						server.broadcastSend(message);
						break;
					case "@":
						log.info("not implemented yet");
						break;
					case "users":
						log.info("not implemented yet");
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	public void broadcastRecieve(Message m) throws IOException {
		ObjectMapper br = new ObjectMapper();
		PrintWriter broad = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		String response = br.writeValueAsString(m);
		log.info(response);
		broad.write(response);
		broad.flush();
	}

}
