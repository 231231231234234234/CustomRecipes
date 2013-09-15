package theboo.mods.customrecipes.handlers;

import java.util.EnumSet;
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import theboo.mods.customrecipes.RecipeLoader;
import theboo.mods.customrecipes.logger.Logger;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

/**
 * Custom Recipes CRTickHandler
 * 
 * <br> Tick handler.
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
 *   
 */
public class CRTickHandler implements ITickHandler {

	private final EnumSet<TickType> ticksToGet;
	
	public CRTickHandler(EnumSet<TickType> ticksToGet)	{
		this.ticksToGet = ticksToGet;
	}
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
        playerTick((EntityPlayer)tickData[0]);
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
        return ticksToGet;
	}

	@Override
	public String getLabel() {
		return null;
	}
	
	public static void playerTick(EntityPlayer player){
		if(CRKeyHandler.keyPressed){
			Logger.log(Level.INFO, "Reloading recipes...");
			RecipeLoader.instance.loadRecipes();
			player.addChatMessage("Recipes has been reloaded.");
		}
	}
	
	

}
