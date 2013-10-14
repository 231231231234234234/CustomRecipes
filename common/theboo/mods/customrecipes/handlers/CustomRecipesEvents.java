package theboo.mods.customrecipes.handlers;

import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import theboo.mods.customrecipes.CustomRecipes;
import theboo.mods.customrecipes.RecipeLoader;
import theboo.mods.customrecipes.lib.Reference;
import theboo.mods.customrecipes.logger.Logger;
import theboo.mods.util.core.handler.VersionChecker;
import cpw.mods.fml.common.FMLLog;

public class CustomRecipesEvents {
	
	private boolean checked = false;
	
	@ForgeSubscribe
	public void worldLoad(EntityJoinWorldEvent event) {
		if(!(event.entity instanceof EntityPlayer)) return;
		if(event.world.isRemote) return;
		if(checked) return;
		
		EntityPlayer player = (EntityPlayer) event.entity;
		
		if(VersionChecker.checkIfOutdated(CustomRecipes.getUpdateURL(), Reference.CURRENT_VERSION_DOUBLE, Reference.DEBUG)) {
			if(event.world.provider.dimensionId == 0) {
				player.addChatMessage("Custom Recipes v" + Reference.CURRENT_VERSION_DOUBLE + " is out of date! Update at http://www.minecraftforum.net/topic/1504359-" );
				Logger.log(Level.WARNING, "Custom Recipes v" + Reference.CURRENT_VERSION_DOUBLE + " is out of date! Update at http://www.minecraftforum.net/topic/1504359-");
			}
		} else {
			Logger.log(Level.INFO, "Custom Recipes is up to date.");
		}
				
		checked = true;
		
		Logger.log(Level.INFO, "Reloading recipes...");
		RecipeLoader.instance.loadRecipes();
	}
}
