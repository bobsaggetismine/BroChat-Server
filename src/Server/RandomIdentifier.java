package Server;

import java.util.ArrayList;
import java.util.Collections;

public class RandomIdentifier {
	private RandomIdentifier(){}
	private static ArrayList<Integer> ids = new ArrayList<Integer>();
	private final static int range = 10000;
	private static int index = 0;
	static {
		for (int i=0;i<range;i++){
			ids.add(i);
		}
		Collections.shuffle(ids);
	}
	public static int getId(){
		if (index > ids.size() -1) index = 0;
		return ids.get(index++);
	}
}