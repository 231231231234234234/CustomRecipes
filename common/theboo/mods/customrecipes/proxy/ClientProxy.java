package theboo.mods.customrecipes.proxy;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import theboo.mods.customrecipes.handlers.CRKeyHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;

public class ClientProxy extends CommonProxy {

	public void addKeybindings() {
		KeyBinding[] key = {new KeyBinding("Reload Custom Recipes recipes", Keyboard.KEY_R)};
        boolean[] repeat = {false};
        KeyBindingRegistry.registerKeyBinding(new CRKeyHandler(key, repeat));  
	}
}
