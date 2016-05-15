package theboo.mods.customrecipes;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class ModCommandBase extends CommandBase {
	
	/* Variables */
	
	public static MinecraftServer serverInstance = FMLCommonHandler.instance().getMinecraftServerInstance();
	public static PlayerList playerList = serverInstance.getPlayerList();
	public static ICommandManager commandManager = serverInstance.getCommandManager();
	
	/* One Liners */
	
	public String getLocalBase() { return "command." + getCommandName().toLowerCase() + "."; }
	
	public void executeCommandPlayer(MinecraftServer server, EntityPlayer player, String[] args) {}
	
	public abstract int getUsageType(); // 0 = command.<Command Name>.usage || 1 = /<Command Name>
	
	@Override public String getCommandName() { return this.getClass().getSimpleName().replace("Command", "").toLowerCase(); }
	@Override public boolean isUsernameIndex(String[] par1ArrayOfStr, int par1) { return true; }
	
	
	/* Functions */
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		if (getUsageType() == 0) { return getLocalBase() + "usage"; }
		else { return "/" + getCommandName(); }
	}
	
	@Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		if (sender instanceof EntityPlayer) executeCommandPlayer(server, (EntityPlayer) sender, args);
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (sender instanceof EntityPlayer) return true;
		return false;
	}
	
	@Override public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		return null;
	}
	
	public void outputMessage(ICommandSender sender, String message, boolean translatable, boolean appendBase, Object...formatargs) {
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			if (translatable) {
				outputMessageLocal(sender, message, appendBase, formatargs);
			} else {
				player.addChatComponentMessage(new TextComponentString((appendBase ? getLocalBase() : "" ) + message));
			}
		} else {
			sender.addChatMessage(new TextComponentString((appendBase ? getLocalBase() : "" ) + message));
		}
	}
	
	public void outputMessageLocal(ICommandSender sender, String message, boolean appendBase, Object...formatargs) {
		if (sender instanceof EntityPlayer) {
			((EntityPlayer) sender).addChatComponentMessage(new TextComponentTranslation((appendBase ? getLocalBase() : "" ) + message, formatargs));
		}
	}
	
	public void outputUsage(ICommandSender sender, Boolean translatable) {
		outputMessage(sender, getCommandUsage(sender), translatable, false);
	}
}