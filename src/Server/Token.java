package Server;

import java.util.ArrayList;
import java.util.Collections;

public class Token
{

	static ArrayList<Integer> tokens;
	
	static int range = 10000;
	static int index = 0;
	static
	{
		tokens = new ArrayList<Integer>();
		for (int i=0;i<range;i++){
			tokens.add(i);
		}
		Collections.shuffle(tokens);
	}
	public static int getToken()
	{
		if (index > tokens.size() -1) index = 0;
		return tokens.get(index++);
	}
}
