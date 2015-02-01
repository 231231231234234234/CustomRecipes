package theboo.mods.customrecipes.proxy;

import java.io.File;
import java.util.Scanner;

import org.apache.logging.log4j.Level;

import theboo.mods.customrecipes.CustomRecipes;
import theboo.mods.customrecipes.logger.Logger;

/**
 * Custom Recipes CommonProxy
 * 
 * <br> Handles the common code.
 * 
 * @license 
    Copyright (C) 2013 TheBoo

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author TheBoo
 *   
 */
public class CommonProxy {

	
	
	public void addKeybindings() {
	}
	
	public String getRecipes() {
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
