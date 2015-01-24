package theboo.mods.customrecipes.lib;


/**
 * AdvancedBrewing Reference
 * 
 * <br> Holds basic constant info.
 * 
 * @license 
    Copyright (C) 2013 TheBoo

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author TheBoo
 *   
 */
public class Reference {
	
    public static final String MOD_ID = "customrecipes";
    public static final String MOD_NAME = "Custom Recipes";
	public static final String BUILD_NUMBER = "2";
    public static final String VERSION_NUMBER = "4.5"+BUILD_NUMBER;
    public static final double CURRENT_VERSION_DOUBLE = 4.5;
    public static final String CHANNEL_NAME = MOD_ID;
    public static final int SECOND_IN_TICKS = 20;
    public static final int SHIFTED_ID_RANGE_CORRECTION = 256;
    public static final String SERVER_PROXY_CLASS = "theboo.mods.customrecipes.proxy.CommonProxy";
    public static final String CLIENT_PROXY_CLASS = "theboo.mods.customrecipes.proxy.ClientProxy";
    public static final String DEPENDENCIES = "required-after:Forge@[9.11.0.884,)";
	public static boolean DEBUG = false;
	
}
