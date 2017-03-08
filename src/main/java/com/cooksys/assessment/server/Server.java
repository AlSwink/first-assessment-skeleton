package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	
	private int port;
	private ExecutorService executor;
	private final Set<ClientHandler> connected = Collections.synchronizedSet(new HashSet<ClientHandler>());
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public synchronized Set<ClientHandler> getConnected() {
		return connected;
	}
	public synchronized void addHandler(ClientHandler t){
		connected.add(t);
	}
	public synchronized void removeHandler(ClientHandler t){
		connected.remove(t);
	}
	public void broadcastSend(Message m) throws IOException{
		for(ClientHandler c : connected){
			c.broadcastRecieve(m);
		}
	}
	
	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket, this);
				executor.execute(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
