package theboo.mods.customrecipes.handlers;

import java.util.EnumSet;

import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

/**
 * CustomRecipes CRKeyHandler
 * 
 * <br> Handles the keybindings.
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
 * @author TheBoo
 *   
 */
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
