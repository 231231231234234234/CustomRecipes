package theboo.mods.util.core.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionChecker {
	
	/**
	 * @return True if outdated, false if not.
	 */
	public static boolean checkIfOutdated(URL versionFile, double CURRENT_VERSION, boolean debugMode) {
		BufferedReader br = null;
		
		try {
			if(debugMode) {
				System.out.println("We're in DEBUG mode, version check skipped!");
			}
			
			if(versionFile == null) {
				System.out.println("Null URL file...");
				return false;
			}
			
			InputStream stream = versionFile.openStream();
			
			InputStreamReader fr = new InputStreamReader(stream);
			br = new BufferedReader(fr);
			StringBuilder content=new StringBuilder(1024);
			String s;
			while((s=br.readLine())!=null) {
			    content.append(s);
			} 
			
			double ONLINE_VERSION = 0;
			try {
				ONLINE_VERSION = Double.valueOf(content.toString());
			} catch(NumberFormatException ex) {
				ex.printStackTrace();
			} finally {
				if(ONLINE_VERSION == 0) {
					System.out.println("Online version isn't a number?");
					return false;
				}
			}
					
			if(CURRENT_VERSION < ONLINE_VERSION) {
				System.out.println(ONLINE_VERSION + " vs " + CURRENT_VERSION);
				br.close();
				return true;
			}
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(br == null) return false;
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
		
}
