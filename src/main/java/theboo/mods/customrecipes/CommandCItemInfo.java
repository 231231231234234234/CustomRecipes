package theboo.mods.customrecipes;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

public class CommandCItemInfo extends ModCommandBase implements ICommand {
	@Override public int getUsageType() { return 1; }
	
	@Override
	public void executeCommandPlayer(MinecraftServer server, EntityPlayer player, String[] args) {
		int hand = 1;
		if (args.length == 1) {
			try {
				int newHand = Integer.parseInt(args[0]);
				if (newHand == 1 || newHand == 2) hand = newHand;
			} catch (NumberFormatException e) {}
		}
		ItemStack stack = null;
		if (hand == 1) stack = player.getHeldItemMainhand();
		if (hand == 2) stack = player.getHeldItemOffhand();
		if (stack != null) {
			if (stack.getItem() != null) {
				Item held = stack.getItem();
				ResourceLocation heldInfo = Item.REGISTRY.getNameForObject(held);
				String modName = heldInfo.getResourceDomain();
				String itemName = heldInfo.getResourcePath();
				String meta = String.valueOf(stack.getMetadata());
				outputMessage(player, "Define this item as: *<alias>=" + modName + ":" + itemName + "," + meta, false, false);
			}
		}
	}
}