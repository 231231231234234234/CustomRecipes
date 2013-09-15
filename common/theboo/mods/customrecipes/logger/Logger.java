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
 * @license Copyright 2013 TheBoo

   <br> Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   <p>
       http://www.apache.org/licenses/LICENSE-2.0
   <p>
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
