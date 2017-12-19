package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileIO {

	public static void writeToFile(String path,String toWrite){
		try {
			Files.write(Paths.get(path), toWrite.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static String readFile(String path){
		File file = new File(path);
		String message = "";
		try {
			Scanner in = new Scanner(file);
			while (in.hasNextLine()){
				message += in.nextLine();
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return message;
	}
	public static void log(String logPath, String string) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(logPath,true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			fw.append(string);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
