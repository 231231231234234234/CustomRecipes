package theboo.mods.customrecipes.proxy;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import theboo.mods.customrecipes.handlers.CRKeyHandler;
import cpw.mods.fml.client.registry.KeyBindingRegistry;

/**
 * Custom Recipes ClientProxy
 * 
 * <br> Handles the client-side code.
 * 
 * @license Copyright 2013 TheBoo

   <br> Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
   <p>
       http://www.apache.org/licenses/LICENSE-2.0
   <p>
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 * 
 * @author TheBoo *   
 */
public class ClientProxy extends CommonProxy {

	public void addKeybindings() {
		KeyBinding[] key = {new KeyBinding("Reload Custom Recipes recipes", Keyboard.KEY_R)};
        boolean[] repeat = {false};
        KeyBindingRegistry.registerKeyBinding(new CRKeyHandler(key, repeat));  
	}
}
