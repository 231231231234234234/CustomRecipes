package theboo.mods.customrecipes.lib;

public class Reference {
    public static final String MOD_ID = "customrecipes";
    public static final String MOD_NAME = "Custom Recipes";
	public static final String BUILD_NUMBER = "0";
    public static final String VERSION_NUMBER = "5.1" + BUILD_NUMBER;
    public static final double CURRENT_VERSION_DOUBLE = 5.1;
    public static final String CHANNEL_NAME = MOD_ID;
    public static final int SECOND_IN_TICKS = 20;
    public static final String SERVER_PROXY_CLASS = "theboo.mods.customrecipes.network.proxy.CommonProxy";
    public static final String CLIENT_PROXY_CLASS = "theboo.mods.customrecipes.network.proxy.ClientProxy";
    public static final String DEPENDENCIES = "required-after:Forge@[12.17.0.1920,)";
	public static boolean DEBUG = false;
}