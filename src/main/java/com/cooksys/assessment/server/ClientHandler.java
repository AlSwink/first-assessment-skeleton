package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private Server server;
	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

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
						
						if(server.nameCheck(message.getUsername())){
							message.setContents("rejected");
							String reject = mapper.writeValueAsString(message);
							writer.write(reject);
							writer.flush();
							this.socket.close();
						} else {
						server.addHandler(this);
						setUsername(message.getUsername());
						createTimestamp(message);
						log.info("user <{}> connected", message.getUsername());
						server.broadcastSend(message);
						}
						break;
					case "disconnect":
						server.removeHandler(this);
						createTimestamp(message);
						server.broadcastSend(message);
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						createTimestamp(message);
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						createTimestamp(message);
						server.broadcastSend(message);
						break;
					case "users":
						log.info("user <{}> requested user list", message.getUsername());
						message.setContents(server.getUsers());
						createTimestamp(message);
						String users = mapper.writeValueAsString(message);
						writer.write(users);
						writer.flush();
						break;
					default:
						if(message.getCommand().matches("@(.*)")){
							createTimestamp(message);
							String c = message.getCommand().replaceAll("[^\\w\\s]", "");
							log.info("user <{}> directly messaged user <{}> with message <{}>", message.getUsername(), c, message.getContents());
							server.directSend(message, c);
							String self = mapper.writeValueAsString(message);
							writer.write(self);
							writer.flush();
						}
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

	public void recieveMessage(Message m) throws IOException {
		ObjectMapper br = new ObjectMapper();
		PrintWriter broad = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		String response = br.writeValueAsString(m);
		broad.write(response);
		broad.flush();
	}
	public void createTimestamp(Message m){
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		m.setTimestamp(timeStamp);
	}
	
}
