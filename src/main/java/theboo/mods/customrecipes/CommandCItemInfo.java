package theboo.mods.customrecipes;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommandCItemInfo extends ModCommandBase implements ICommand {
	@Override public boolean canConsoleUseCommand() { return false; }
	@Override public boolean isOpOnly() { return false; }
	@Override public boolean TabCompletesOnlinePlayers() { return false; }
	@Override public int getUsageType() { return 1; }
	
	@Override
	public void processCommandPlayer(EntityPlayer player, String[] args) {
		if (player.getCurrentEquippedItem() != null) {
			if (player.getCurrentEquippedItem().getItem() != null) {
				Item held = player.getCurrentEquippedItem().getItem();
				String modName = GameRegistry.findUniqueIdentifierFor(held).modId;
				String itemName = GameRegistry.findUniqueIdentifierFor(held).name;
				String meta = String.valueOf(player.getCurrentEquippedItem().getMetadata());
				outputMessage(player, "Define this item as: *<alias>=" + modName + ":" + itemName + "," + meta, false, false);
			}
		}
	}
}