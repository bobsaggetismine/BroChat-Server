package Server;

import java.security.Security;

public class ServerMain {
	public static void main(String[] args) {
		Security.setProperty("crypto.policy", "unlimited");
		new Server(args);
	}
}