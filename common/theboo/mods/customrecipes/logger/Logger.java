package theboo.mods.customrecipes.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import theboo.mods.customrecipes.CustomRecipes;

import cpw.mods.fml.common.FMLCommonHandler;

/**
 * Custom Recipes Logger
 * 
 * <br> Used to log info to the logging file as well as to the minecraftforge logging file.
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
 * @author MightyPork
 *   
 */
public class Logger {
	private static FileWriter fstream = null;
	private static BufferedWriter log = null;
    public static String logPath = (CustomRecipes.instance.getWorkingFolder()+"/CustomRecipes.log");

	public static void log(Level level, String info){
		try{
			if(fstream == null || log == null){
				fstream = new FileWriter(logPath);
				log = new BufferedWriter(fstream);
			}
			log.write(info+"\n");
			log.flush();
			FMLCommonHandler.instance().getFMLLogger().log(level, info);
		}catch(IOException e){}
	}

	public static void logClose(){
		try{
			if(fstream == null || log == null){
				log = new BufferedWriter(fstream);
			}
			log.close();
		}catch(IOException e){}
	}
}
