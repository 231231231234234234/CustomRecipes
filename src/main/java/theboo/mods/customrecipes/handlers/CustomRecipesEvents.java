package theboo.mods.customrecipes.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import org.apache.logging.log4j.Level;

import theboo.mods.customrecipes.CustomRecipes;
import theboo.mods.customrecipes.RecipeLoader;
import theboo.mods.customrecipes.lib.Reference;
import theboo.mods.customrecipes.logger.Logger;
import theboo.mods.util.core.handler.VersionChecker;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CustomRecipesEvents {
	
	private boolean checked = false;
	
	@SubscribeEvent
	public void worldLoad(EntityJoinWorldEvent event) {
		if(!(event.entity instanceof EntityPlayer)) return;
		if(event.world.isRemote) return;
		if(checked) return;
		
		EntityPlayer player = (EntityPlayer) event.entity;
		
		if(VersionChecker.checkIfOutdated(CustomRecipes.getUpdateURL(), Reference.CURRENT_VERSION_DOUBLE, Reference.DEBUG)) {
			if(event.world.provider.dimensionId == 0) {
				player.addChatMessage(new ChatComponentText("Custom Recipes v" + Reference.CURRENT_VERSION_DOUBLE + " is out of date! Update at http://www.minecraftforum.net/topic/1504359-") );
				Logger.log(Level.WARN, "Custom Recipes v" + Reference.CURRENT_VERSION_DOUBLE + " is out of date! Update at http://www.minecraftforum.net/topic/1504359-");
			}
		} else {
			Logger.log(Level.INFO, "Custom Recipes is up to date.");
		}
				
		checked = true;
		
		Logger.log(Level.INFO, "Reloading recipes...");
		RecipeLoader.instance.loadRecipes();
	}
}
