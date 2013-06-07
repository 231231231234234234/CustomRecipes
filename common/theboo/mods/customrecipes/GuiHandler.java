package theboo.mods.customrecipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{

    public boolean displayWorkbench;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if(displayWorkbench)
        {
            return new ContainerWorkbench(player.inventory, world, x, y, z);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world,int x, int y, int z)
    {
        if(displayWorkbench)
        {
            return new ContainerWorkbench(player.inventory, world, x, y, z);
        }
        return null;
    }
}