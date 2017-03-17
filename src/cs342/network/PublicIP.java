package cs342.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class PublicIP {
	
	private static String IP;
	
	public static String get() {
		if (IP == null) {
			try {
				URL iCanHazIp = new URL("http://icanhazip.com/");
				BufferedReader in = new BufferedReader(new InputStreamReader(
									iCanHazIp.openStream()));
				IP = in.readLine();
			} catch (Exception e) {
				IP = "IP REQUEST FAILED";
			}
		}
		return IP;
	}
}
