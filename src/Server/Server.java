
package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {
	
	
	
	private final static int DEFAULT_PORT = 8192;
	private final static String LOGIN_SERVER_ADDRESS = "localhost";
	
	// server instance objects/variables
	private DatagramSocket socket;
	private DatagramSocket l_socket;
	
	
	private int port;
	private Thread recieve, send;
	
	private Thread l_snd;
	private Thread l_rec;
	
	private List<ServerClient> clients = new ArrayList<ServerClient>();

	//ArratList<Integer> waitingRequests;
	
	// server instance methods
	public Server(final String[] args) {
		if (args.length == 0) {
			port = Server.DEFAULT_PORT;
		} else {
			port = Integer.parseInt(args[0]);
		}
		try {
			socket = new DatagramSocket(port);
			System.out.println("Open on port: " + port);
			l_socket = new DatagramSocket(8194);
		} catch (SocketException e) {
			System.out.println("Failed to open on port" + port + " because: ");
			//e.printStackTrace();
		}
		recieve();
	}

	public void recieve() {
		recieve = new Thread("S recieve") {
			public void run() {
				while (true) {
					
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (IOException e) {
						//e.printStackTrace();
					}
					try {
						process(packet);
					} catch (Exception e) {
						
						//e.printStackTrace();
					}
				}
			}
		};
		recieve.start();
	}
	public void makeLoginRequest(String username, String password)
	{
		l_snd = new Thread()
		{
			public void run()
			{
				String loginReq = "/c/"+Token.getToken()+"/n/"+username+"/n/"+password+"/e/";
				byte[] data =new byte[1024];
				data = loginReq.getBytes();
				DatagramPacket packet;
				try {
					packet = new DatagramPacket(data, data.length,InetAddress.getByName(LOGIN_SERVER_ADDRESS),8193);
					l_socket.send(packet);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		};
		l_snd.start();
	}
	static boolean flag;
	public boolean CheckLoginStatus()
	{
		l_rec = new Thread(){
			public void run()
			{
				byte[] data = new byte[1024];
				
				DatagramPacket packet = new DatagramPacket(data, data.length);
				try {
					l_socket.receive(packet);
				} catch (IOException e) {
					//e.printStackTrace();
				}
				String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
				if (msg.startsWith("/a/"))
				{
					flag = true;
				}
				if (msg.startsWith("/d/"))
				{
					flag = false;
				}
				
			}
		};
		l_rec.start();
		while (l_rec.isAlive());
		return flag;
	}
	public void process(final DatagramPacket packet) throws SQLException, IOException {
		String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
		if (msg.startsWith("/c/")) {
			String password = msg.split("/c/|/n/")[2];
			String username =msg.split("/c/|/n/")[1];
			makeLoginRequest(username, password);
			if (CheckLoginStatus())
			{
				clients.add(new ServerClient(username, packet.getAddress(), packet.getPort(), RandomIdentifier.getId()));
				sendToAll("Client: " + username + " has joined the room");
				send("/c/" + clients.get(clients.size() - 1).id + "/e/", clients.get(clients.size() - 1));
				System.out.println("client: " + username + " Added.");
			}
			else
			{
				sendToNotConnectedUser("Access Denied. You are either banned or have the wrong password for your account.",packet.getAddress(), packet.getPort());
				return;
			}
		}else if (msg.startsWith("/d/")) {
			int id = -1;
			try {
				id = Integer.parseInt(msg.split("/d/|/e/")[1]);
			} catch (Exception e) {
				//e.printStackTrace();
				return;
			}
			disconnectClient(id);
		} else if (msg.startsWith("/u/")) {
			int id = -1;
			try {
				id = Integer.parseInt(msg.split("/u/|/e/")[1]);
			} catch (Exception e) {
				//e.printStackTrace();
				return;
			}
			sendUsers(id);
		} else if (msg.startsWith("/m/")) {
			String message = msg.split("/m/|/n/|/n/|/e/")[1];
			String name = msg.split("/m/|/n/|/n/|/e/")[3];
			int id;
			id = Integer.parseInt(msg.split("/m/|/n/|/n/|/e/")[2]);
			for (ServerClient client: clients){
				if (client.id == id){
					if (SQLChecker.getPriv(name) == 3)name = "[admin]" + name;
					else if (SQLChecker.getPriv(name)==2)name = "[moderator]" + name;
					else name = "[user]"+name;
				}
			}
			for (ServerClient client : clients) {
				if (packet.getAddress().equals(client.ip)) {
					sendToAll("/m/"+name+"/n/"+message+"/e/");
					return;
				}
			}
		} else if (msg.startsWith("/cu/")) {
			System.out.println("here");
			String[] hold = msg.split("/cu/|/n/");
			System.out.println(msg);
			makeAccountCreateRequest(hold[1],hold[2]);
			String x = checkAccountCreateStatus();
				sendToNotConnectedUser(x, packet.getAddress(), packet.getPort());
			
		}else if (msg.startsWith("/k/")){
			String[] hold = msg.split("/k/|/n/|/n/|/e/");
			try {
				kickUser(hold[1],hold[2],hold[3]);
			}catch(Exception e){
			    //e.printStackTrace();
			}
		} else if (msg.startsWith("/b/")){
			String[] hold = msg.split("/b/|/n/|/n/|/e/");
			banUser(hold[1],hold[2],hold[3]);
		}else if (msg.startsWith("/p/")){
			String[]hold = msg.split("/p/|/n/|/n/|/e/");
			promoteUser(hold[1],hold[2],hold[3]);
		}else if (msg.startsWith("/l/")){
			String[]hold = msg.split("/l/|/n/|/n/|/e/");
			demoteUser(hold[1],hold[2],hold[3]);
		}
	}
	static String dunnoHowToDoThisBetterAndThisNameDefinitelyWontCreateConflicts = "";
	private String checkAccountCreateStatus()
	{
		l_rec = new Thread(){
			public void run()
			{
				byte[] data = new byte[1024];
				
				DatagramPacket packet = new DatagramPacket(data, data.length);
				try {
					l_socket.receive(packet);
				} catch (IOException e) {
					//e.printStackTrace();
				}
				String msg = new String(packet.getData(),packet.getOffset(),packet.getLength());
				dunnoHowToDoThisBetterAndThisNameDefinitelyWontCreateConflicts = msg;
				
			}
		};
		l_rec.start();
		while (l_rec.isAlive());
		return dunnoHowToDoThisBetterAndThisNameDefinitelyWontCreateConflicts;
	}

	private void makeAccountCreateRequest(String username, String password) {
		l_snd = new Thread()
		{
			public void run()
			{
				String loginReq = "/ca/"+username+"/n/"+password;
				byte[] data =new byte[1024];
				data = loginReq.getBytes();
				DatagramPacket packet;
				try {
					packet = new DatagramPacket(data, data.length,InetAddress.getByName(LOGIN_SERVER_ADDRESS),8193);
					l_socket.send(packet);
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		};
		l_snd.start();
		
	}

	private void demoteUser(String toDemote,String username,String password) throws SQLException{
		if (!SQLChecker.CheckPassSQL(username, password) || SQLChecker.getPriv(toDemote) >= SQLChecker.getPriv(username)){
			return;
		}else{
			SQLChecker.changePriv(toDemote, ""+(SQLChecker.getPriv(toDemote)-1));
			sendToAll("Server: "+toDemote + " has been demoted to user.");
		}
	}
	private void promoteUser(String toPromote, String username, String password) throws SQLException{
		if (!SQLChecker.CheckPassSQL(username, password) || (SQLChecker.getPriv(toPromote)+1) >= SQLChecker.getPriv(username)){
			return;
		}else{
			SQLChecker.changePriv(toPromote, ""+(SQLChecker.getPriv(toPromote)+1));
			sendToAll("Server: "+toPromote + " has been promoted.");
		}
	}
	private void kickUser(final String userToKick, final String kicker,final String password) throws SQLException {
		
		if (!SQLChecker.CheckPassSQL(kicker, password) || SQLChecker.getPriv(userToKick) >= SQLChecker.getPriv(kicker)){
			return;
		}else {
			Iterator<ServerClient> it = clients.iterator();
			while (it.hasNext()){
				ServerClient client = it.next();
				if (client.name.equals(userToKick)){
					System.out.println(kicker+" has kicked: "+userToKick+".");
					sendToAll(kicker+" has kicked: "+userToKick+".");
					it.remove();
				}
			}
		}
		
	}
	private void banUser(final String userToBan,final String banner, final String password) throws SQLException{
		if (!SQLChecker.CheckPassSQL(banner, password) || SQLChecker.getPriv(userToBan) >= SQLChecker.getPriv(banner)){
			return;
		}else{
			Iterator<ServerClient> it = clients.iterator();
			while(it.hasNext()){
				ServerClient client = it.next();
				if (client.name.equals(userToBan)){
					sendToAll(banner+" has banned: "+userToBan);
					kickUser(userToBan,banner,password);
					SQLChecker.addToBanList(client.ip.toString());
				}
			}
		}
	}
	private void sendUsers(final int id) {
		String message = "";
		for (ServerClient client : clients) {
			if (client.id == id) {
				for (ServerClient client2 : clients) {
					message += client2.name + "\n";
				}
				send(message, client);
				return;
			}
		}
	}
	
	private void disconnectClient(final int id) {
		if (clients.size() < 1) {
			return;
		}
		if (clients.size() < 2) {
			
			if (clients.get(0).id == id){
			System.out.println("Last Client Removed");
			clients.remove(0);
			return;
			}
		}
		Iterator<ServerClient> it = clients.iterator();
		while(it.hasNext()) {
			ServerClient client = it.next();
			if (client.id == id) {
				System.out.println(client.name + " disconnected");
				sendToAll(client.name + " has left the room");
				it.remove();
				break;
			}
		}
	}
	
	private void sendToNotConnectedUser(final String message,final InetAddress ip, final int port){
		send = new Thread("Server send") {
			public void run() {
				byte[] data = new byte[1024];
				data = message.getBytes();
				DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		};
		send.start();
	}
	
	private void send(final String msg, final ServerClient client) {
		send = new Thread("Server send") {
			public void run() {
				byte[] data = new byte[1024];
				data = msg.getBytes();
				DatagramPacket packet = new DatagramPacket(data, data.length, client.ip, client.port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		};
		send.start();
	}

	private void sendToAll(final String msg) {
		Iterator<ServerClient> it = clients.iterator();
		while(it.hasNext()){
			ServerClient client = it.next();
			send(msg, client);
		}
	}
}