package Server;

import java.net.InetAddress;

public class ServerClient {
	InetAddress ip;
	int port;
	String name;
	final int id;
	public ServerClient(String name,InetAddress ip, int port,int id){
		this.ip = ip;
		this.name = name;
		this.port = port;
		this.id = id;
	}
}