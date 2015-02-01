package theboo.mods.customrecipes;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Level;

import theboo.mods.customrecipes.handlers.CustomRecipesEvents;
import theboo.mods.customrecipes.lib.Reference;
import theboo.mods.customrecipes.logger.Logger;
import theboo.mods.customrecipes.network.proxy.CommonProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * CustomRecipes CustomRecipes
 * 
 * <br> The main class of Custom Recipes
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
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION_NUMBER, dependencies = Reference.DEPENDENCIES)
public class CustomRecipes {
	
    public static net.minecraftforge.common.config.Configuration config;
        
    public RecipeLoader loader = new RecipeLoader();

	
	@Instance("customrecipes")
	public static CustomRecipes instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS) 
	public static CommonProxy proxy;

    public File getWorkingFolder(){
        File toBeReturned;
        try{
            if (FMLCommonHandler.instance().getSide().isClient()){
                toBeReturned = Minecraft.getMinecraft().mcDataDir;
            }
            else{
                toBeReturned = MinecraftServer.getServer().getFile("");
            }
            return toBeReturned;
            
        }
        catch(Exception ex){
            Logger.log(Level.FATAL, "Couldn't get the path to the mod directory.");
        }
        return null;
    }
	
    @EventHandler
	public void preInit(final FMLPreInitializationEvent fml){		
	    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	        	System.out.println("Closing Custom Recipes log...");
	        	Logger.logClose();
	        }
	    }, "Shutdown-hook-thread"));
	    
		config = new net.minecraftforge.common.config.Configuration(fml.getSuggestedConfigurationFile());
		loadConfig(config);
		
		MinecraftForge.EVENT_BUS.register(new CustomRecipesEvents());
	}
	
	@EventHandler
	public void load(FMLPostInitializationEvent fml){
		System.out.println();
		System.out.println();
		
		Logger.log(Level.INFO, "=== CustomRecipes ===\n *** Created by MightyPork *** \n *** Developed by TheBoo ***\n\n"+(new Date()).toString()+"\n\nSave your recipe files into .minecraft/mods/customrecipes.\n");
		
		GameRegistry.registerFuelHandler(loader);
		
		loader.loadRecipes();
	}
	
	private void loadConfig(net.minecraftforge.common.config.Configuration c) {
		c.load();
		Reference.DEBUG = c.get(c.CATEGORY_GENERAL, "Enable Extensive Log Messages [Debug Mode]", false).getBoolean(false);
    	c.save();
	}
	
	public static URL getUpdateURL() {
		try {
			return new URL("http://pastebin.com/raw.php?i=T3afBrCS");
		}
		catch(MalformedURLException ex) {
			System.out.println("Woops, URL was wrong!");
			return null;
		}
	}
}
