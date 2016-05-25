package theboo.mods.customrecipes.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Level;

import theboo.mods.customrecipes.RecipeLoader;
import theboo.mods.customrecipes.logger.Logger;

public class CustomRecipesEvents {
	
	private boolean checked = false;
	
	@SubscribeEvent
	public void worldLoad(EntityJoinWorldEvent event) {
		if(!(event.getEntity() instanceof EntityPlayer)) return;
		if(event.getWorld().isRemote) return;
		if(checked) return;	
		checked = true;
		Logger.log(Level.INFO, "Reloading recipes...");
		RecipeLoader.instance.loadRecipes();
	}
}
