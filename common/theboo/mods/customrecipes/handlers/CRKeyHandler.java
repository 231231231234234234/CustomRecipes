package theboo.mods.customrecipes.handlers;

import java.util.EnumSet;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class CRKeyHandler extends KeyHandler {

    private EnumSet tickTypes = EnumSet.of(TickType.CLIENT);
    public static boolean keyPressed = false;
    
    public CRKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
            super(keyBindings, repeatings);
    }
    
	@Override
	public String getLabel() {
		return "";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,boolean tickEnd, boolean isRepeat) {
		keyPressed = true;
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		keyPressed = false;
	}

	@Override
	public EnumSet<TickType> ticks() {
        return tickTypes;
	}

}
