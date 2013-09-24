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
