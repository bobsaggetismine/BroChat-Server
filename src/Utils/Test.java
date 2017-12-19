package Utils;

import java.sql.SQLException;

import Server.SQLChecker;

public class Test {
public static void main(String[] args) {
	try {
		SQLChecker.changePriv("nig", "user");
	}catch(Exception e){
		System.out.println("nopeeeeeeeeee");
	}
}
}
