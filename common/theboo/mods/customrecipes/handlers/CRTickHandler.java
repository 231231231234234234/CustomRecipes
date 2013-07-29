package theboo.mods.customrecipes.handlers;

import java.util.EnumSet;
import java.util.logging.Level;

import theboo.mods.customrecipes.CustomRecipes;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

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
			CustomRecipes.instance.log(Level.INFO, "Reloading recipes...");
			CustomRecipes.instance.loadRecipes();
			player.addChatMessage("Recipes has been reloaded.");
		}
	}
	
	

}
