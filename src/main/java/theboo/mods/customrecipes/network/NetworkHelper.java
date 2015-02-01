package theboo.mods.customrecipes.network;

import java.io.File;
import java.util.Scanner;

import org.apache.logging.log4j.Level;

import theboo.mods.customrecipes.CustomRecipes;
import theboo.mods.customrecipes.logger.Logger;

public class NetworkHelper {
	
	
	public static String getRecipes() {
		StringBuilder contents = new StringBuilder();

		try {
			File dir = new File(CustomRecipes.instance.getWorkingFolder() + "/mods/customrecipes");
			
			for (File file : dir.listFiles()) {
				Scanner s = new Scanner(file);
				contents.append(s.nextLine());
				s.close();
			}
			
		} catch(Exception ex) {
			Logger.log(Level.FATAL, "Lol failed to sync noob");
		}
		
		return contents.toString();
	}
	
	
}
