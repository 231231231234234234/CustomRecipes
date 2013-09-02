package theboo.mods.customrecipes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.Configuration;

import org.lwjgl.input.Keyboard;

import theboo.mods.customrecipes.handlers.CRKeyHandler;
import theboo.mods.customrecipes.handlers.CRTickHandler;
import theboo.mods.customrecipes.proxy.CommonProxy;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = "customrecipes", name = "Custom Recipes", version = "4.2.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class CustomRecipes implements IFuelHandler {
	
	private Hashtable<String,ItemStack> dict = new Hashtable<String,ItemStack>();
	private Hashtable<String,Integer> fuels = new Hashtable<String,Integer>();
	private final int DICT_VERSION_CURRENT = 37;
	private int DICT_VERSION = 0;
	private boolean keybindings;
    public static Configuration config;
    
    public String logPath = (getWorkingFolder()+"/CustomRecipes.log");

	
	@Instance("customrecipes")
	public static CustomRecipes instance;
	
	@SidedProxy(clientSide = "theboo.mods.customrecipes.proxy.ClientProxy", serverSide = "theboo.mods.customrecipes.proxy.CommonProxy") 
	public static CommonProxy proxy;
	
	public String getPriorities()
	{
		return "after:*";
	}
	
    /**
     * tries to get a file using the path from either minecraft or minecraft server
     * 
     * @return the working minecraft path
     */
    public File getWorkingFolder(){
        File toBeReturned;
        try{
            if (FMLCommonHandler.instance().getSide().isClient()){
                toBeReturned = ModLoader.getMinecraftInstance().getMinecraft().mcDataDir;
            }
            else{
                toBeReturned = ModLoader.getMinecraftServerInstance().getFile("");
            }
            return toBeReturned;
            
        }
        catch(Exception ex){
            log(Level.SEVERE, "Couldn't get the path to the mod directory.");
        }
        return null;
    }

	public int getFuel(int i, int j)
	{
		String identifier = Integer.toString(i) + "." + Integer.toString(j);
		if(fuels.get(identifier)==null){return 0;}
		return (Integer)fuels.get(identifier);
	}

	// for the log
	private FileWriter fstream = null;
	private BufferedWriter log = null;
	
	public void log(Level level, String foo){
		try{
			if(fstream == null || log == null){
				fstream = new FileWriter(logPath);
				log = new BufferedWriter(fstream);
			}
			log.write(foo+"\n");
			log.flush();
			FMLCommonHandler.instance().getFMLLogger().log(level, foo);
		}catch(IOException e){}
	}

	private void logClose(){
		try{
			if(fstream == null || log == null){
				log = new BufferedWriter(fstream);
			}
			log.close();
		}catch(IOException e){}
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent fml){		
	    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
	        public void run() {
	        	System.out.println("Closing Custom Recipes log...");
	        	logClose();
	        }
	    }, "Shutdown-thread"));
	    
		config = new Configuration(fml.getSuggestedConfigurationFile());
		loadConfig(config);
	}
	
	@EventHandler
	public void load(FMLPostInitializationEvent fml){
		System.out.println();
		System.out.println();
		
		log(Level.INFO, "=== CustomRecipes ===\n *** Created by MightyPork *** \n *** Developed by TheBoo ***\n\n"+(new Date()).toString()+"\n\nSave your recipe files into .minecraft/mods/customrecipes.\n");
		
		GameRegistry.registerFuelHandler(this);
		
		loadRecipes();
		
		if(keybindings) {
			proxy.addKeybindings();
			addTickhandler();
		}
	}
	
	private void addTickhandler() {   
        TickRegistry.registerTickHandler(new CRTickHandler(EnumSet.of(TickType.PLAYER)), Side.SERVER);
	}
	
	private void loadConfig(Configuration c) {
		c.load();
		keybindings = c.get(c.CATEGORY_GENERAL, "Enable reloading keybind", true).getBoolean(true);
    	c.save();
	}

	public void loadRecipes()	{
        boolean fail=!(new File(getWorkingFolder()+"/mods/customrecipes/dictionary.txt")).exists();
        
        if(fail){
            (new File(getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            regenerateDictionary();
            fail=false;
            log(Level.INFO, "Creating dictionary file.\n");
        }

        loadRecipeFile(getWorkingFolder()+"/mods/customrecipes/dictionary.txt", false);
        log(Level.INFO, "Loading dictionary.txt");

        if(DICT_VERSION != DICT_VERSION_CURRENT){
            (new File(getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            regenerateDictionary();
            DICT_VERSION = DICT_VERSION_CURRENT;
            log(Level.INFO, "\nRecipe dictionary is outdated.\nBuilding new dictionary.\n");
            loadRecipeFile(getWorkingFolder()+"/mods/customrecipes/dictionary.txt", false);
            log(Level.INFO, "Loading dictionary.txt");
        }
        
        File dir = new File(getWorkingFolder()+"/mods/customrecipes/");
        
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".") && !name.substring(name.length()-1).equals("~");
            }
        };

        fail=!(new File(getWorkingFolder()+"/mods/customrecipes/dictionary_custom.txt")).exists();

        if(fail){
            (new File(getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(getWorkingFolder()+"/mods/customrecipes/dictionary_custom.txt"));
                out.write(
                    "# *** CUSTOM DICTIONARY ***\n"+
                    "# Here you can define aliases for mod items and blocks.\n"+
                    "#\n"+
                    "# To prevent confussion: You CAN'T create new blocks and items with this file.\n"+
                    "# They are added by other mods. This file only lets you define aliases for these items.\n"+
                    "#\n"+
                    "# This file will be read right after dictionary.txt to make sure\n"+
                    "# all your following recipes (in other files) can access these aliases.\n"+
                    "#\n"+
                    "# Example alias definition:\n"+
                    "# silmarilShoes = 7859\n"+
                    "# rubyGem = 9958,13  where 9958 is ID, 13 is DAMAGE\n#\n\n");
                
                fail=false;
                out.close();
                log(Level.INFO, "Creating empty file for Custom Dictionary.\n");
            }catch(IOException ioe){
                log(Level.WARNING, "* I/O ERROR: Could not create Custom Dictionary file.\n");
            }
        }

        log(Level.INFO, "Loading custom dictionary: dictionary_custom.txt");
        loadRecipeFile(getWorkingFolder() + "/mods/customrecipes/dictionary_custom.txt", true);
        
        
        //do all other recipes
        String[] children = dir.list(filter);
        if (children == null) {
            // Either dir does not exist or is not a directory
            fail=true;
        } else {
            for (int i=0; i<children.length; i++) {
                // Get filename of file or directory
                String filename = children[i];
                
                if(!filename.equals("dictionary.txt") && !filename.equals("dictionary_custom.txt")){
                    log(Level.INFO, "Loading recipes: "+filename);
                    loadRecipeFile(getWorkingFolder()+"/mods/customrecipes/"+filename, true);
                }
            }
        }
        
        if(fail){
            log(Level.INFO, "Dictionary not found, creating new one.");
            (new File(getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            regenerateDictionary();
        }
        
        log(Level.INFO, "Recipes loaded.\n\n");
	}

	private void regenerateDictionary(){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(getWorkingFolder()+"/mods/customrecipes/dictionary.txt"));
			for(int a=0;a<AliasDictionary.length;a++){
				out.write(AliasDictionary[a]);
				out.newLine();
			}
			out.close();
		}
		catch (IOException e)
		{
			log(Level.WARNING, "* I/O ERROR: Could not regenerate the dictionary in .minecraft/mods/customrecipes/dictionary.txt");
		}
	}
	
    private int getNumberFromString(String str)
    {
        try{
            int tmpi = Integer.valueOf(str).intValue();
            if(tmpi < -1) {
            	return 32767;
            }
            return (tmpi < -1 ? 0 : tmpi) ;                             
        } catch (NumberFormatException e)
        {        
        	System.out.println("Oops");
            return 0;
        }
    }
	
	private int getAnyNumberFromString(String str){
		try{
			int tmpi=Integer.valueOf(str);
			if(tmpi == -1) {
				return 32767;
			}
			return tmpi;
		}catch(NumberFormatException e){
        	System.out.println("Oops2");
			return 0;
		}
	}

	private boolean isValidItem(int i){
		if(i>0 && i <32000){
			if((i<256 && Block.blocksList[i]!=null) || (i>=256 && Item.itemsList[i]!=null)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	private boolean isGoodNull = false;
	
	/** get stack from alias, "null" or single number */
	private ItemStack getStackFromAliasOrNumber(String str){
		isGoodNull = false;
		if(str.equalsIgnoreCase("null")||str.equalsIgnoreCase("none")||str.equalsIgnoreCase("nothing") ||str.equalsIgnoreCase("empty")||str.equalsIgnoreCase("air")){
			isGoodNull = true;
			return null;
		}else{

			try{

				int tmpi=Integer.valueOf(str.trim());
				
				if(isValidItem(tmpi)){
					return (ItemStack) new ItemStack(tmpi,1,0);
				}else{
					errorUndefined(path, str, str);
					return null;
				}
				
			}catch(NumberFormatException e){

				if(dict.get(str.toLowerCase())!=null){
					Object obj = dict.get(str.toLowerCase());
					
					if(obj instanceof ItemStack){
						return ((ItemStack)obj).copy();
						
					}else if(obj instanceof Item){
						return new ItemStack((Item)obj, 1, 0);
						
					}else if(obj instanceof Block){
						return new ItemStack((Block)obj, 1, 0);
						
					}else{
						System.out.println("CR: INVALID DICTIONARY ENTRY! @ "+path);
						return null;
					}
					
				}else{
					return null;
				}

			}
		}
	}
	
	private String path;

	/** get object (stack, item, block) for the recipe part */
	private ItemStack getRecipeStack(String str){
		return parseStack(str, false); //disable size, disable forcestack
	}

	/** get stack for recipe product */
	private ItemStack getProductStack(String str){
		return parseStack(str, true); //enabled size, make a stack
	}

	/** object parser */
	private ItemStack parseStack(String str, boolean acceptSize){

		String[] parts=str.split("[,]");

		if(parts.length>=1){
		
			// create or load from dictionary
			ItemStack stack1 = getStackFromAliasOrNumber(parts[0]);			
			
			if(stack1 == null){
				if(!isGoodNull) errorUndefined(path,str,str);
				return null;
			}
			
			ItemStack stack = stack1.copy();
			
			if(parts.length>=2){
				
				if(!acceptSize){
					
					int dmg = getAnyNumberFromString(parts[1]);
					
					if(dmg >= 32000){
						//invalid damage
						errorAlert(path,str,"Warning - invalid item damage.");
					}else{
						stack.setItemDamage(dmg);
					}
					
				}else{
					
					int size = getNumberFromString(parts[1]);
					if(size < 0 || size > 256){
						//invalid size
						errorAlert(path,str,"Warning - invalid stack size.");
					}else{
						stack.stackSize = size;
						
						if(parts.length>=3){
							int dmg = getNumberFromString(parts[2]);
							if(dmg >= 32000){
								//invalid damage
								errorAlert(path,str,"Warning - invalid item damage.");
							}else{
								stack.setItemDamage(dmg);
							}
						}
						
					}
					
				}
				
			}
			
			if(acceptSize && stack.getItemDamage() == -1) stack.setItemDamage(0);
			
			return stack;
		}

		errorUndefined(path,str,str);
		return null;
		
	}

	private int getRecipeId(String str){
		ItemStack stack = getStackFromAliasOrNumber(str);
		if(stack == null) return -1;
		return stack.itemID;
	}

	private void loadRecipeFile(String file_path, boolean log){
		//int a,b,c;
		//String tmp;
		ArrayList<String> rawFile=readFile(file_path);
		
		// save to global
		path = file_path;

        
		if(log) log(Level.INFO, "Started to load recipes at " + file_path);
        
        
		for(int a=0; a < rawFile.size(); a++){
		    
	        
		    if(log) log(Level.INFO, "Loading Recipes syntaxes at " + file_path);
	        
		    
			String entry=(String)rawFile.get(a);
			String entryOrig=entry;
			
			if(entry.length()>= 4 && entry.substring(0,1).equals("*")){
               
			    if(log) log(Level.INFO, "Found alias syntax in "+file_path);
				parseRecipeAlias(file_path, entryOrig, entry);

			}else if(entry.length() >= 16 && entry.substring(0,9).equals("shapeless")){

	             log(Level.INFO, "Found shapeless syntax in "+file_path);
				parseRecipeShapeless(file_path, entryOrig, entry);
				

			}else if(entry.length() >= 9 && entry.substring(0,4).equals("fuel")){
               
			    log(Level.INFO, "Found fuel syntax in "+file_path);
				parseRecipeFuel(file_path, entryOrig, entry);
				

			}else if(entry.length()>= 15 && entry.substring(0,8).equals("smelting")){
                
			    log(Level.INFO, "Found smelting syntax in "+file_path);
				parseRecipeSmelting(file_path, entryOrig, entry);
				
				
			}else if(entry.length()>= 13 && entry.substring(0,6).equals("shaped")){
               
			    log(Level.INFO, "Found smelting syntax in "+file_path);
				parseRecipeShaped(file_path, entryOrig, entry);
				
			}
			else if(entry.length()>= 13 && entry.substring(0,6).equals("remove")){
	             log(Level.INFO, "Found remove syntax in "+file_path);
				parseRecipeRemove(file_path, entryOrig, entry);
			}
		}
	}
	


	private static final String ERR_MSG_UNDEFINED="Undefined alias or wrong ID (no such block or item exists).\nIf you are trying to get a mod item, try adding 256 to the id value.";
	private static final String ERR_MSG_SYNTAX="Syntax error.";
	private static final String ERR_MSG_SHAPE="Recipe is not rectangular or is larger than 3x3.";
	private static final String ERR_MSG_NOBURNTIME="No burn time specified. Recipe does not make sense.";

	private void errorAlert(String fpath, String line, String cause){
		log(Level.SEVERE, "\n* ERROR in recipe file \""+fpath+"\": "+line+"\n"+(cause==null?"":"  "+cause+"\n"));
	}

	private void errorUndefined(String fpath, String line, String fail){
		errorAlert(fpath, line, "  " + fail + " -> " + ERR_MSG_UNDEFINED);
	}
	
	private void errorSyntax(String fpath, String line){
		errorAlert(fpath, line, ERR_MSG_SYNTAX);
	}
	
	private static Object[] OAappend(Object[] source, Object what){
		Object[] tmp=new Object[source.length+1];
		for(int a=0;a<source.length;a++){
			tmp[a]=source[a];
		}
		tmp[source.length]=what;

		return tmp;
	}

	private static Object[] OAappendAll(Object[] source, Object[] what){
		Object[] tmp=new Object[source.length+what.length];
		for(int a=0;a<source.length;a++){
			tmp[a]=source[a];
		}
		for(int a=0;a<what.length;a++){
			tmp[source.length+a]=what[a];
		}

		return tmp;
	}

	 private ArrayList readFile(String url)
	  {
	        File file = new File(url);
	        FileInputStream fis = null;
	        //BufferedInputStream bis = null;
	        //DataInputStream dis = null;
	        BufferedReader reader = null;
	        ArrayList fileContents = new ArrayList();
	        try
	        {
	          fis = new FileInputStream(file);
	          reader = new BufferedReader(new InputStreamReader(fis));
	          //bis = new BufferedInputStream(fis);
	          //dis = new DataInputStream(bis);
	          //bas = new BufferedReader(new InputStreamReader(bis));

	         
	          String tmpString = reader.readLine(); // Read the first line
	         
	          while (tmpString != null)
	          {
	                //String tmpString = bas.readLine();

	                tmpString = tmpString.replaceAll("#.*$", "");
	                tmpString = tmpString.replaceAll("//.*$", "");
	                if ((!tmpString.equals("")) && (!tmpString.equals("\n"))) {
	                  tmpString = tmpString.replaceAll(" ", "");
	                  tmpString = tmpString.replaceAll("\t", "");
	                  tmpString = tmpString.replaceAll(":", ",");
	                  tmpString = tmpString.replaceAll("[/|;]", "/");

	                  if (((tmpString.length() >= 4) && (tmpString.substring(0, 1).equals("*"))) || ((tmpString.length() >= 9) && (tmpString.substring(0, 4).equals("fuel"))) || ((tmpString.length() >= 13) && (tmpString.substring(0, 6).equals("shaped"))) || ((tmpString.length() >= 16) && (tmpString.substring(0, 9).equals("shapeless"))) || ((tmpString.length() >= 15) && (tmpString.substring(0, 8).equals("smelting"))) || ((tmpString.length() >= 13) && (tmpString.substring(0, 6).equals("remove"))))
	                  {
	                        fileContents.add(tmpString);
	                  }
	                  else
	                  {
	                        log(Level.WARNING, "\nSyntax error in recipe file:\n" + url + "\n-> " + tmpString + "\n");
	                  }
	                }
	           
	                tmpString = reader.readLine(); // read the next line
	          }

	          reader.close();
	          fis.close();
	          //bis.close();
	          //dis.close();
	        }
	        catch (FileNotFoundException e) {
	          e.printStackTrace();
	        } catch (IOException e) {
	          e.printStackTrace();
	        }
	        return fileContents;
	  }

	private void parseRecipeAlias(String fpath, String entryOrig, String entryo){
		// ---------- dictionary entry --------------
		//*Name=123
		
		String entry=entryo.substring(1); //remove "*"
		String[] tokens=entry.split("[=]");
		if(tokens.length!=2){ // identifier, without a value
			errorAlert(fpath,entryOrig,ERR_MSG_SYNTAX);
			return;
		}

		String def = tokens[1];

		if(def != null){
			if(tokens[0].equals("DICTIONARY_VERSION")){
				DICT_VERSION=getNumberFromString(def);
			}else{			
				ItemStack stack = getRecipeStack(def);
				if(stack != null){
					dict.put((String)tokens[0].toLowerCase(),(ItemStack)stack);
				}else{
					errorUndefined(fpath,entryOrig,def);
					return;
				}
			}
		}else{
			errorSyntax(fpath,entryOrig);
			return;
		}
		
	}

	private void parseRecipeShapeless(String par_path, String par_entryOrig, String par_entry){
		String path = new String(par_path);
		String entryOrig = new String(par_entryOrig);
		String entry = new String(par_entry);
		
	
		
		// -------------- shapeless recipe --------------
		
		entry=entry.substring(9); //remove "shapeless"
		
		//tokens = recipe, product
		String[] tokens=entry.split("[>]");
		
		//check syntax
		if(tokens.length!=2 || tokens[0].length()<3 || tokens[1].length()<3){
			errorAlert(path,entryOrig,ERR_MSG_SYNTAX);
			return;
		}

		//2 parts, parse recipe
		String tmp = tokens[0];
		//remove trailing brackets
		tmp=tmp.replaceAll("[()]","");

		//split by "+" sign to individual items
		String[] recipeParts=tmp.split("[+]");

		//the output recipe
		Object[] recipe = {};

		//go through the recipe
		for(int b=0;b<recipeParts.length;b++){

			Object piece = getRecipeStack(recipeParts[b]);

			if(piece==null){
				errorUndefined(path,entryOrig,recipeParts[b]); return;
			}
			
			if(piece instanceof Block) piece = new ItemStack((Block)piece,1,-1); 
			
			recipe = OAappend(recipe, piece);

		}
		
		if(recipe.length <= 0 || recipe.length > 9){
			errorAlert(path,entryOrig,"Bad recipe syntax: Crafting of air, or more than 9 elements.");
			return;
		}

		//now do the product
		
		tmp=tokens[1];
		tmp=tmp.replaceAll("[()]","");

		ItemStack product = getProductStack(tmp);

		if(product==null){
			errorUndefined(path,entryOrig,tmp);
			return;
		}
		
		// finish, apply!
				
		FMLCommonHandler.instance().getFMLLogger().log(Level.INFO, "LOADED RECIPE");
		GameRegistry.addShapelessRecipe(product,recipe);
	}

	private void parseRecipeShaped(String file_path, String entryOrig,	String entryo) {
		
		// --------------- shaped recipe ----------------
		
		String entry=entryo.substring(6); //remove "shaped"
		String[] tokens=entry.split("[>]");

		if(tokens.length!=2 || tokens[0].length()<3 || tokens[1].length()<3){
			errorSyntax(file_path,entryOrig);
			return;
		}

		//2 parts
		String tmp=tokens[0];
		tmp=tmp.replaceAll("[()]","");
		
		//get the rows
		String[] rows=tmp.split("[/]");
		
		//counts items in last row, if not matches, throw error
		int lastRowLength=-1;

		Object[] rowStrings = new Object[0];
		Object[] explanation = new Object[0];

		int explCnt=0;

		Character[] table={'A','B','C','D','E','F','G','H','I'};

		if(rows.length<1 || rows.length>3){
			errorAlert(file_path,entryOrig,ERR_MSG_SHAPE);
			return;
		}

		int validPieces = 0;
		
		for(int c=0; c<rows.length; c++){
			//split row by + sign
			String[] recipeParts=rows[c].split("[+]");
			
			//building the letter pattern
			String rowbuilder="";

			//not rectangular
			if(lastRowLength != -1 && recipeParts.length != lastRowLength){
				errorAlert(file_path,entryOrig,ERR_MSG_SHAPE);
				return;
			}

			// too short or long row
			if(recipeParts.length<1 || recipeParts.length>3){
				errorAlert(file_path,entryOrig,ERR_MSG_SHAPE); return;
			}

			lastRowLength=recipeParts.length;
			
			// go through this row
			for(int b=0; b<recipeParts.length; b++){

				char tmpec=table[explCnt++];
				
				//add an unique letter 
				rowbuilder=rowbuilder+Character.toString(tmpec);

				Object piece = getRecipeStack(recipeParts[b]);

				// building itemstack if not blank field
				if(piece != null){
					validPieces++;
					// add char
					explanation = OAappend(explanation,Character.valueOf(tmpec));
					// add the item
					explanation = OAappend(explanation,piece);
				}
			}
			//add a row to row strings list
			rowStrings = OAappend(rowStrings,rowbuilder);
		}

		if(rowStrings.length <= 0){
			errorAlert(file_path,entryOrig,"Crafting of air.");
			return;
		}
		
		if(validPieces <= 0){
			errorAlert(file_path,entryOrig,"No valid items in recipe.");
			return;
		}

		Object[] recipe = OAappendAll(rowStrings,explanation);
		
		//doing product
		tmp=tokens[1];
		tmp=tmp.replaceAll("[()]","");
		
		ItemStack product = getProductStack(tmp);
		
		if(product == null){
			errorAlert(file_path,entryOrig,"Invalid product "+tmp);
			return;
		}
		

		//product done
		try{
			GameRegistry.addRecipe(product,recipe);
		}catch(ArrayIndexOutOfBoundsException e){
			log(Level.WARNING, "Shaped recipe "+entryOrig+" @ path "+path+" threw ArrayIndexOutOfBoundsException");
		}
		
	}

	private void parseRecipeSmelting(String file_path, String entryOrig, String entryo) {
		//  ------------ smelting recipe --------------
		String entry=entryo.substring(8); //remove "smelting"
		//split to recipe and pruduct
		String[] tokens=entry.split("[>]");
	
		// check syntax
		if(tokens.length!=2 || tokens[0].length()<3 || tokens[1].length()<3){
			errorAlert(file_path,entryOrig,ERR_MSG_SYNTAX);
			return;
		}
	
		//2 parts
		String recipe = tokens[0].replaceAll("[()]","");
	
		//doing the recipe part
	
		int intval=getRecipeId(recipe);
	
		if(intval<1){
			errorUndefined(file_path,entryOrig,recipe);
			return;
		}
		
		int recipeId=intval;
		
		//recipe done
		
		String tmp=tokens[1];
		tmp=tmp.replaceAll("[()]","");
	
		ItemStack product = getProductStack(tmp);
	
		//invalid
		if(product==null){
			errorUndefined(file_path,entryOrig,tmp);
			return;
		}
		
		//product done
		
		GameRegistry.addSmelting(recipeId,product, 4);
	}

	private void parseRecipeFuel(String file_path, String entryOrig, String oentry) {
		// -------------- adding fuel --------------
		// fuel(paper,100)

		String entry=oentry.substring(4); //remove "fuel"

		//remove brackets
		entry=entry.replaceAll("[()]","");

		//split by comma to ID and RATE
		String[] tokens=entry.split("[,]");
		
		if(tokens.length<2 || tokens.length>3){
			errorAlert(file_path,entryOrig,ERR_MSG_SYNTAX);
			return;
		}				
		
		//2 or 3 parts
		
		//work out itemstack
		String tmp=tokens[0];

		ItemStack fuelStack = getProductStack(tmp);

		if(fuelStack==null){
			errorUndefined(file_path,entryOrig,tmp);
			return;
		}
		
		//set stack damage if present
		if(tokens.length == 3){
			fuelStack.setItemDamage(getNumberFromString(tokens[1]));
		}

		//get burntime
		int burntime=getNumberFromString(tokens[tokens.length-1]);

		// if valid, add to recipes
		if(burntime>0){
			fuels.put((String)Integer.toString(fuelStack.itemID)+ "."+(String)Integer.toString(fuelStack.getItemDamage()) , Integer.valueOf(burntime));
		}else{
			errorAlert(file_path,entryOrig,ERR_MSG_NOBURNTIME);
			return;
		}
	}
	
    private void parseRecipeRemove(String file_path, String entryOrig, String entryo)
	{
		//-----Remove recipe
		//remove
		String entry=entryo.substring(6); //remove "remove"

		//remove brackets
		String recipe = entry.replaceAll("[()]","");

		int intval=getRecipeId(recipe);
		
		if(intval<1){
			errorUndefined(file_path,entryOrig,recipe);
			return;
		}

        ItemStack stack = getProductStack(recipe);  
        log(Level.INFO, "About to remove recipe with input" + recipe);
        removeRecipe(stack);
    }
	private void removeRecipe(ItemStack resultItem)
	{
	    ItemStack recipeResult = null;
	    ArrayList recipes = (ArrayList) CraftingManager.getInstance().getRecipeList();

	    for (int scan = 0; scan < recipes.size(); scan++)
	    {
	        IRecipe tmpRecipe = (IRecipe) recipes.get(scan);
	        if (tmpRecipe instanceof ShapedRecipes)
	        {
	            ShapedRecipes recipe = (ShapedRecipes)tmpRecipe;
	            recipeResult = recipe.getRecipeOutput();
	            log(Level.INFO, "Found shaped recipe!");
	        }

	        if (tmpRecipe instanceof ShapelessRecipes)
	        {
	            ShapelessRecipes recipe = (ShapelessRecipes)tmpRecipe;
	            recipeResult = recipe.getRecipeOutput();
	            log(Level.INFO, "Found shapeless recipe!");
	        }

	        if (ItemStack.areItemStacksEqual(resultItem, recipeResult))
	        {
	            log(Level.INFO, "Removed Recipe: " + recipes.get(scan) + " -> " + recipeResult);
	            recipes.remove(scan);
	        }
	        else {
	            log(Level.WARNING, "Couldn't remove the recipe with result: " + resultItem.toString());
	        }
	    }
	}

	private String[] AliasDictionary = {
		"########################",
		"#",
		"# CUSTOM RECIPES MOD: ALIAS DICTIONARY",
		"#",
		"# This file is vital for the CustomRecipes mod.",
		"# Do not edit this file to prevent compatibility issues.",
		"# The CustomRecipes mod uses this file for recipe reading.",
		"# If this file is removed, it will be regenerated on next Minecraft startup.",
		"#",
		"# Syntax: * Name = ID",
		"#      + \"#\" marks comments",
		"#         example: *SuperBlock=253 # My awesome block",
		"#",
		"# To define metadata (eg. wool colour): *BlockWithMeta=253,12",
		"#",
		"# Aliases are not case sensitive, thus Dirt = dirt = diRt.",
		"# ",
		"# There is no way how to get enchanted items.",
		"#",
		"#",
		"# You can define new aliases in your recipe files.",
		"# If you want to access custom Items (not Blocks), you (sometimes) have to add 256 to the item id.",
		"#",
		"# This file was created by MightyPork, ondra@ondrovo.com.",
		"# You can freely redistribute it, if you preserve this header.",
		"#",
		"########################",
		"",
		"",
		"# Dictionary version (if outdated, dictionary will be regenerated).",
		"*DICTIONARY_VERSION = "+Integer.toString(DICT_VERSION_CURRENT),
		"",
		"",
		"",
		"",
		"####### B L O C K S #######",
		"",
		"*stone = 1",
		"*smoothstone = 1",
		"*rock = 1",
		"*grass = 2",
		"*dirt = 3",
		"*cobblestone = 4",
		"",
		"*planks = 5",
		"*plank = 5",
		"*anyPlank = 5,-1",
		"*anyPlanks = 5,-1",
		"*planksWooden = 5",
		"*woodenPlanks = 5",
		"*planksWood = 5",
		"*woodPlanks = 5",
		"*plankWooden = 5",
		"*woodenPlank = 5",
		"*plankWood = 5",
		"*woodPlank = 5",
		"*woodPlankPine = planks,1",
		"*woodPlankBirch = planks,2",
		"*woodPlankJungle = planks,3",
		"*plankPine = planks,1",
		"*plankBirch = planks,2",
		"*planksJungle = planks,3",
		"*planksPine = planks,1",
		"*planksBirch = planks,2",
		"*planksJungle = planks,3",
		"*Pineplank = planks,1",
		"*Birchplank = planks,2",
		"*Jungleplanks = planks,3",
		"*Pineplanks = planks,1",
		"*Birchplanks = planks,2",
		"*Jungleplanks = planks,3",
		"",
		"*sapling = 6 # meta: 0=oak, 1=pine, 2=birch",
		"*saplingOak=sapling,0",
		"*oakSapling=sapling,0",
		"*saplingPine=sapling,1",
		"*pineSapling=sapling,1",
		"*saplingBirch=sapling,2",
		"*birchSapling=sapling,2",
		"*saplingJungle = sapling,3",
		"*jungleSapling = sapling,3",
		"*bedrock = 7",
		"",
		"*waterMoving = 8",
		"*waterFlowing = 8",
		"*flowingWater = 8",
		"*movingWater = 8",
		"*water = 8",
		"*stillWater = 9",
		"*stationaryWater = 9",
		"*waterStill = 9",
		"*waterStationary = 9",
		"",
		"*lavaMoving = 10",
		"*lavaFlowing = 10",
		"*movingLava = 10",
		"*flowingLava = 10",
		"*lava = 10",
		"*lavaStationary = 11",
		"*lavaStill = 11",
		"*stationaryLava = 11",
		"*stillLava = 11",
		"",
		"*sand = 12",
		"*gravel = 13",
		"",
		"*oreGold = 14",
		"*goldOre = 14",
		"*oreIron = 15",
		"*ironOre = 15",
		"*coalOre = 16",
		"*oreCoal = 16",
		"",
		"*wood = 17 # meta: 0=oak, 1=pine, 2=birch",
		"*log = 17",
		"*anyLog = 17,-1",
		"*anyWood = 17,-1",
		"*logs = log",
		"",
		"*oakWood = log,0",
		"*woodOak = log,0",
		"*oakLog = log,0",
		"*logOak = log,0",
		"*oakLogs = log,0",
		"*logsOak = log,0",
		"",
		"*pineWood = log,1",
		"*woodPine = log,1",
		"*pineLog = log,1",
		"*logPine = log,1",
		"*pineLogs = log,1",
		"*logsPine = log,1",
		"",
		"*birchWood = log,2",
		"*woodBirch = log,2",
		"*birchLog = log,2",
		"*logBirch = log,2",
		"*birchLogs = log,2",
		"*logsBirch = log,2",
		"",
		"*jungleWood = log,3",
		"*woodJungle = log,3",
		"*jungleLog = log,3",
		"*logJungle = log,3",
		"*jungleLogs = log,3",
		"*logsJungle = log,3",
		"",
		"*leaves = 18 # meta: 0=oak, 1=pine, 2=birch",
		"*leaf = leaves",
		"*anyLeaf = leaves,-1",
		"*anyLeaf = leaves,-1",
		"*oakLeaves = leaves,0",
		"*oakLeaf = leaves,0",
		"*leavesOak = leaves,0",
		"*leafOak = leaves,0",
		"*pineLeaves = leaves,1",
		"*pineLeaf = leaves,1",
		"*leavesPine = leaves,1",
		"*leafPine = leaves,1",
		"*birchLeaves = leaves,2",
		"*birchLeaf = leaves,2",
		"*leavesBirch = leaves,2",
		"*leafBirch = leaves,2",
		"*jungleLeaves = leaves,3",
		"*jungleLeaf = leaves,3",
		"*leavesJungle = leaves,3",
		"*leafJungle = leaves,3",
		"",
		"*sponge = 19",
		"*glass = 20",
		"*window = 20",
		"",
		"*oreLapisLazuli = 21",
		"*oreLapis = 21",
		"*lapisLazuliOre = 21",
		"*lapisOre = 21",
		"",
		"*lapisLazuliBlock = 22",
		"*blockLapisLazuli = 22",
		"*blockLapis = 22",
		"",
		"*dispenser = 23",
		"",
		"*sandStone = 24",
		"*anySandStone = 24,-1",
		"*sandStoneHieroglyphic = sandStone,1",
		"*sandStoneSmooth = sandStone,2",
		"",
		"*noteBlock = 25",
		"*blockNote = 25",
		"",
		"*bedBlock = 26",
		"*blockBed = 26",
		"",
		"*poweredTrack = 27",
		"*trackPowered = 27",
		"*poweredRail = 27",
		"*railPowered = 27",
		"*booster = 27",
		"*boosterRail = 27",
		"*railBooster = 27",
		"*boosterTrack = 27",
		"*trackBooster = 27",
		"",
		"*detectorRail = 28",
		"*railDetector = 28",
		"*detectorTrack = 28",
		"*trackDetector = 28",
		"",
		"*stickyPiston = 29",
		"*pistonSticky = 29",
		"",
		"*web = 30",
		"*cobweb = 30",
		"*net = 30",
		"",
		"*tallGrassDeadShrub = 31",
		"*tallGrass = 31,1",
		"*tallGrassBracken = 31,2",
		"*bracken = 31,2",
		"*deadShrub = 32",
		"",
		"*piston = 33",
		"*pistonHead = 34 # you should not own this",
		"*headPiston = 34 # you should not own this",
		"",
		"*wool=35",
		"*cloth=wool",
		"*anyCloth=wool,-1",
		"*anyWool=wool,-1",
		"# meta: ",
		"# 0=white, 1=orange, 2=magenta, 3=lightBlue, 4=yellow",
		"# 5=lime, 6=pink, 7=gray, 8=lightGray",
		"# 9=cyan, 10=purple, 11=blue, 12=brown",
		"# 13=green, 14=red, 15=black",
		"",
		"*woolWhite=wool,0",
		"*whiteWool=wool,0",
		"*clothWhite=wool,0",
		"*whiteCloth=wool,0",
		"",
		"*woolOrange=wool,1",
		"*orangeWool=wool,1",
		"*clothOrange=wool,1",
		"*orangeCloth=wool,1",
		"",
		"*woolMagenta=wool,2",
		"*magentaWool=wool,2",
		"*clothMagenta=wool,2",
		"*magentaCloth=wool,2",
		"",
		"*woolLightBlue=wool,3",
		"*lightBlueWool=wool,3",
		"*clothLightBlue=wool,3",
		"*lightBlueCloth=wool,3",
		"",
		"*woolYellow=wool,4",
		"*yellowWool=wool,4",
		"*clothYellow=wool,4",
		"*yellowCloth=wool,4",
		"",
		"*woolLime=wool,5",
		"*limeWool=wool,5",
		"*clothLime=wool,5",
		"*limeCloth=wool,5",
		"*woolLimeGreen=wool,5",
		"*limeGreenWool=wool,5",
		"*clothLimeGreen=wool,5",
		"*limeGreenCloth=wool,5",
		"",
		"*woolPink=wool,6",
		"*pinkWool=wool,6",
		"*clothPink=wool,6",
		"*pinkCloth=wool,6",
		"",
		"*woolGray=wool,7",
		"*grayWool=wool,7",
		"*clothGray=wool,7",
		"*grayCloth=wool,7",
		"",
		"*woolLightGray=wool,8",
		"*lightGrayWool=wool,8",
		"*clothLightGray=wool,8",
		"*lightGrayCloth=wool,8",
		"",
		"*woolCyan=wool,9",
		"*cyanWool=wool,9",
		"*clothCyan=wool,9",
		"*cyanCloth=wool,9",
		"",
		"*woolPurple=wool,10",
		"*purpleWool=wool,10",
		"*clothPurple=wool,10",
		"*purpleCloth=wool,10",
		"",
		"*woolBlue=wool,11",
		"*blueWool=wool,11",
		"*clothBlue=wool,11",
		"*blueCloth=wool,11",
		"",
		"*woolBrown=wool,12",
		"*brownWool=wool,12",
		"*clothBrown=wool,12",
		"*brownCloth=wool,12",
		"",
		"*woolGreen=wool,13",
		"*greenWool=wool,13",
		"*clothGreen=wool,13",
		"*greenCloth=wool,13",
		"",
		"*woolRed=wool,14",
		"*redWool=wool,14",
		"*clothRed=wool,14",
		"*redCloth=wool,14",
		"",
		"*woolBlack=wool,15",
		"*blackWool=wool,15",
		"*clothBlack=wool,15",
		"*blackCloth=wool,15",
		"",
		"*yellowFlower = 37",
		"*flowerYellow = 37",
		"*dandelion = 37",
		"",
		"*rose = 38",
		"*flowerRed = 38",
		"*redFlower = 38",
		"",
		"*brownMushroom = 39",
		"*mushroomBrown = 39",
		"",
		"*redMushroom = 40",
		"*mushroomRed = 40",
		"",
		"*goldBlock = 41",
		"*blockGold = 41",
		"*storageGold = 41",
		"*goldStorage = 41",
		"*ironBlock = 42",
		"*blockIron = 42",
		"*storageIron = 42",
		"*ironStorage = 42",
		"*ironCube = 42",
		"",
		"*slab = 44 # meta: 0=stone, 1=sandstone, 2=wooden, 3=cobblestone",
		"*stair = 44",
		"*stairSingle = 44",
		"*singleStair = 44",
		"*slabSingle = 44",
		"*singleSlab = 44",
		"",
		"*anySlab = 44,-1",
		"",		
		"*SlabStone = 44,0",
		"*StepStone = 44,0",
		"*stairStone = 44,0",
		"*stoneSlab = 44,0",
		"*stoneStep = 44,0",
		"*stoneStair = 44,0",
		"",
		"*stepSandstone = 44,1",
		"*slabSandstone = 44,1",
		"*stairSandstone = 44,1",
		"*sandstoneSlab = 44,1",
		"*sandstoneStep = 44,1",
		"*sandstoneStair = 44,1",
		"",
		"*stepWood = 44,2",
		"*slabWood = 44,2",
		"*stairWood = 44,2",
		"*woodSlab = 44,2",
		"*woodStep = 44,2",
		"*woodStair = 44,2",
		"*stepWooden = 44,2",
		"*slabWooden = 44,2",
		"*stairWooden = 44,2",
		"*woodenSlab = 44,2",
		"*woodenStep = 44,2",
		"*woodenStair = 44,2",
		"",
		"*stepCobblestone = 44,3",
		"*slabCobblestone = 44,3",
		"*stairCobblestone = 44,3",
		"*cobblestoneSlab = 44,3",
		"*cobblestoneStep = 44,3",
		"*cobblestoneStair = 44,3",

		"*stepBrick = 44,4",
		"*slabBrick = 44,4",
		"*stairBrick = 44,4",
		"*BrickSlab = 44,4",
		"*BrickStep = 44,4",
		"*BrickStair = 44,4",

		"*stepStoneBrick = 44,5",
		"*slabStoneBrick = 44,5",
		"*stairStoneBrick = 44,5",
		"*StoneBrickSlab = 44,5",
		"*StoneBrickStep = 44,5",
		"*StoneBrickStair = 44,5",

		"",
		"*brickWall = 45",
		"*wallBrick = 45",
		"*wallBricks = 45",
		"*bricksWall = 45",
		"",
		"*TNT = 46",
		"*tnt = 46",
		"*explosive = 46",
		"*dynamite = 46",
		"*dinamite = 46",
		"",
		"*bookshelf = 47",
		"",
		"*mossyCobblestone = 48",
		"*cobblestoneMossy = 48",
		"*mossStone = 48",
		"*stoneMoss = 48",
		"*mossyStone = 48",
		"*stoneMossy = 48",
		"",
		"*obsidian = 49",
		"*torch = 50",
		"*torchWood = 50",
		"*woodTorch = 50",
		"*torchWooden = 50",
		"*woodenTorch = 50",
		"*coalTorch = 50",
		"*torchCoal = 50",
		"*candle = 50",
		"*fire = 51",
		"",
		"*monsterSpawner = 52",
		"*spawner = 52",
		"*mobSpawner = 52",
		"*pigSpawner = 52",
		"",
		"*woodenStairs = 53",
		"*stairsWooden = 53",
		"*stairsWood = 53",
		"*woodStairs = 53",
		"",
		"*chest = 54",
		"*redstoneWire = 55",
		"",
		"*diamondOre = 56",
		"*oreDiamond = 56",
		"*diamondBlock = 57",
		"*blockDiamond = 57",
		"*diamondStorage = 57",
		"*storageDiamond = 57",
		"",
		"*workbench = 58",
		"*craftingTable = 58",
		"",
		"*crops = 59",
		"*wheatBlock = 59",
		"*blockWheat = 59",
		"",
		"*soil = 60",
		"*farmland = 60",
		"*field = 60",
		"",
		"*furnace = 61",
		"*forge = 61",
		"*oven = 61",
		"*burningFurnace = 62 # don't use this for crafting recipes!",
		"*furnaceBurning = 62",
		"",
		"*signPost = 63",
		"*signPostBlock = 63",
		"*blockSignPost = 63",
		"",
		"*woodenDoorBlock = 64 # use woodenDoor for crafting recipes!",
		"*doorWoodenBlock = 64",
		"*doorWoodBlock = 64",
		"*blockWoodenDoor = 64",
		"*blockDoorWooden = 64",
		"*blockDoorWood = 64",
		"",
		"*ladder = 65",
		"*ladders = 65",
		"",
		"*rails = 66",
		"*rail = 66",
		"*track = 66",
		"*tracks = 66",
		"*minetrack = 66",
		"*minetracks = 66",
		"",
		"*cobblestoneStairs = 67",
		"*stairsCobblestone = 67",
		"*stairCompactCobblestone = 67",
		"*stairsCompactCobblestone = 67",
		"*compactCobblestoneStairs = 67",
		"*compactCobblestoneStair = 67",
		"*compactStairCobblestone = 67",
		"*compactStairsCobblestone = 67",
		"",
		"*wallSign = 68",
		"*wallSignBlock = 68",
		"*blockWallSign = 68",
		"",
		"*lever = 69",
		"",
		"*stonePressurePlate = 70",
		"*pressurePlateStone = 70",
		"",
		"*ironDoorBlock = 71",
		"*doorIronBlock = 71",
		"*blockIronDoor = 71",
		"*blockDoorIron = 71",
		"*steelDoorBlock = 71",
		"*doorSteelBlock = 71",
		"*blockSteelDoor = 71",
		"*blockDoorSteel = 71",
		"",
		"*woodenPressurePlate = 72",
		"*pressurePlateWooden = 72",
		"*pressurePlateWood = 72",
		"",
		"*redstoneOre = 73",
		"*oreRedstone = 73",
		"",
		"*glowingRedstoneOre = 74",
		"*glowingOreRedstone = 74",
		"*redstoneOreGlowing = 74",
		"",
		"*redstoneTorchOff  = 75 # off",
		"*torchRedstoneOff  = 75",
		"",
		"*redstoneTorch  = 76 # on (this can be crafted)",
		"*torchRedstone  = 76",
		"*redstoneTorchOn  = 76 # on (this can be crafted)",
		"*torchRedstoneOn  = 76",
		"",
		"*stoneButton = 77",
		"*button = 77",
		"*buttonStone = 77",
		"",
		"*snowLayer = 78",
		"*ice = 79",
		"",
		"*snowBlock = 80",
		"*blockSnow = 80",
		"*snow = 80",
		"",
		"*cactus = 81",
		"*cacti = 81",
		"",
		"*clay = 82",
		"*clayBlock = 82",
		"*blockClay = 82",
		"",
		"*sugarCaneBlock = 83",
		"*reedBlock = 83",
		"*reedsBlock = 83",
		"*blockSugarCane = 83",
		"*blockReed = 83",
		"*blockReeds = 83",
		"",
		"*jukebox = 84",
		"*fence = 85",
		"*pumpkin = 86",
		"*netherrack = 87",
		"*netherStone = 87",
		"",
		"*soulSand = 88",
		"*sandSoul = 88",
		"",
		"*glowStone = 89",
		"",
		"*portal = 90",
		"*portalBlock = 90",
		"*portalTile = 90",
		"*blockPortal = 90",
		"*tilePortal = 90",
		"",
		"*jackOLantern = 91",
		"*pumpkinLantern = 91",
		"*lanternPumpkin = 91",
		"",
		"*cakeBlock = 92",
		"*blockCake = 92",
		"",
		"*redstoneRepeaterBlockOff  = 93",
		"*blockRedstoneRepeaterOff  = 93",
		"*repeaterBlockOff  = 93",
		"*blockRepeaterOff  = 93",
		"*repeaterBlockOff  = 93",
		"*repeaterBlockOff  = 93",
		"",
		"*redstoneRepeaterBlock  = 94",
		"*redstoneRepeaterBlockOn  = 94",
		"*blockRedstoneRepeater  = 94",
		"*blockRedstoneRepeaterOn  = 94",
		"*repeaterBlock  = 94",
		"*repeaterBlockOn  = 94",
		"*blockRepeater  = 94",
		"*blockRepeaterOn  = 94",
		"",
		"*lockedChest = 95 #removed from game",
		"*chestLocked = 95",
		"",
		"*trapdoor = 96",
		"*hatch = 96",
		"*anySilverfishStone = 97,-1",
		"*silverfishStone = 97,0",
		"*silverfishCobbleStone = 97,1",
		"*silverfishStoneBrick = 97,2",
		"*stoneSilverfish = 97",
		"*anyStoneBrick = 98,-1",
		"*stoneBrick = 98,0",
		"*stoneBrickMossy = 98,1",
		"*mossyStoneBrick = 98,1",
		"*crackedStoneBrick = 98,2",
		"*stoneBrickCracked = 98,2",
		"*circleStoneBrick = 98,3",
		"*stoneBrickCircle = 98,3",
		"*giantRedMushroom = 99",
		"*giantBrownMushroom = 100",
		"*ironBars = 101",
		"*ironBar = 101",
		"*barsIron = 101",
		"*barIron = 101",
		"*glassPane = 102",
		"*glassPanel = 102",
		"*flatGlass = 102",
		"*thinGlass = 102",
		"*melon = 103",
		"*melonBlock = 103",
		"*watermelon = 103",
		"*stempumpkin = 104",
		"*stemmelon = 105",
		"*pumpkinstem = 104",
		"*melonstem = 105",
		"*vines = 106",
		"*fenceGate = 107",
		"*gate = 107",
		"*gateFence = 107",
		"*brickStairs = 108",
		"*stairsBrick = 108",
		"*stoneBrickStairs = 109",
		"*stairsStoneBrick = 109",
		"",
		"#start of 1.0.0 blocks",
		"",
		"*mycelium = 110",
		"*mushroomGrass = 110",
		"*grassMushroom = 110",
		"",
		"*lily = 111",
		"*lilyPad = 111",
		"*waterLily = 111",
		"",
		"*netherBrick = 112",
		"*brickNether = 112",
		"*netherrackBrick = 112",
		"*brickNetherrack = 112",
		"*netherstoneBrick = 112",
		"*brickNetherstone = 112",
		"",
		"*netherFence = 113",
		"*fenceNether = 113",
		"*netherrackFence = 113",
		"*fenceNetherrack = 113",
		"*netherstoneFence = 113",
		"*fenceNetherstone = 113",
		"",
		"*netherBrickFence = 113",
		"*fenceNetherBrick = 113",
		"*netherrackBrickFence = 113",
		"*fenceNetherrackBrick = 113",
		"*netherstoneBrickFence = 113",
		"*fenceNetherstoneBrick = 113",
		"",
		"*netherStairs = 114",
		"*stairsNether = 114",
		"*netherrackStairs = 114",
		"*stairsNetherrack = 114",
		"*netherstoneStairs = 114",
		"*stairsNetherstone = 114",
		"",
		"*netherBrickStairs = 114",
		"*stairsNetherBrick = 114",
		"*netherrackBrickStairs = 114",
		"*stairsNetherrackBrick = 114",
		"*netherstoneBrickStairs = 114",
		"*stairsNetherstoneBrick = 114",
		"",
		"*netherWart = 115",
		"*wartNether = 115",
		"*wart = 115",
		"*netherCrops = 115",
		"*netherplant = 115",
		"",
		"*enchantmentTable = 116",

		"*brewingStandBlock = 117 #should not be crafted. craft the ITEM!",
		"*potionStandBlock = 117",
		"*BlockBrewingStand = 117",
		"*BlockpotionStand = 117",

		"*cauldronBlock = 118",
		"",
		"*endPortal = 119",
		"*enderPortal = 119",
		"",
		"*endPortalFrame = 120",
		"*enderPortalFrame = 120",
		"",
		"*enderStone = 121",
		"*endStone = 121",
		"",
		"*dragonEgg = 122",
		"*eggDragon = 122",
		"",
		"*lightOff = 123",
		"*redstoneLightOff = 123",
		"*lightBulbOff = 123",
		"*lanternOff = 123",
		"",
		"*lightOn = 124",
		"*redstoneLightOn = 124",
		"*lightBulbOn = 124",
		"*lanternOn = 124",
		"*light = 124",
		"*redstoneLight = 124",
		"*lightBulb = 124",
		"*lantern = 124",
		"",
		"",
		"####### I T E M S #######",
		"",
		"*ironShovel = 256",
		"*shovelIron = 256",
		"*shovelSteel = 256",
		"*steelShovel = 256",
		"",
		"*ironPickaxe = 257",
		"*pickaxeIron = 257",
		"*steelPickaxe = 257",
		"*pickaxeSteel = 257",
		"",
		"*ironAxe = 258",
		"*axeIron = 258",
		"*steelAxe = 258",
		"*axeSteel = 258",
		"",
		"*flintAndSteel = 259",
		"*lighter = 259",
		"",
		"*apple = 260",
		"*bow = 261",
		"*arrow = 262",
		"*coal = 263,0 # meta: 0=coal, 1=charcoal",
		"*charcoal = 263,1",
		"*anyCoal = 263,-1",
		"",
		"*diamond = 264",
		"*gem = 264",
		"",
		"*ironIngot = 265",
		"*ingotIron = 265",
		"*iron = 265",
		"",
		"*goldIngot = 266",
		"*ingotGold = 266",
		"*gold = 266",
		"",
		"*ironSword = 267",
		"*swordIron = 267",
		"*steelSword = 267",
		"*swordSteel = 267",
		"",
		"*woodenSword = 268",
		"*woodSword = 268",
		"*swordWooden = 268",
		"*swordWood = 268",
		"",
		"*woodenShovel = 269",
		"*shovelWooden = 269",
		"*woodShovel = 269",
		"*shovelWood = 269",
		"",
		"*woodenPickaxe = 270",
		"*pickaxeWooden = 270",
		"*woodPickaxe = 270",
		"*pickaxeWood = 270",
		"",
		"*woodenAxe = 271",
		"*axeWooden = 271",
		"*woodAxe = 271",
		"*axeWood = 271",
		"",
		"*stoneSword = 272",
		"*swordStone = 272",
		"",
		"*stoneShovel = 273",
		"*shovelStone = 273",
		"",
		"*stonePickaxe = 274",
		"*pickaxeStone = 274",
		"",
		"*stoneAxe = 275",
		"*axeStone = 275",
		"",
		"*diamondSword = 276",
		"*swordDiamond = 276",
		"",
		"*diamondShovel = 277",
		"*shovelDiamond = 277",
		"",
		"*diamondPickaxe = 278",
		"*pickaxeDiamond = 278",
		"",
		"*diamondAxe = 279",
		"*axeDiamond = 279",
		"",
		"*stick = 280",
		"*sticks = 280",
		"*bowl = 281",
		"*bowls = 281",
		"*bowlEmpty = 281",
		"*emptyBowl = 281",
		"*bowlMushroomSoup = 282",
		"*bowlMushroomStew = 282",
		"*bowlMushrooms = 282",
		"*mushroomSoup = 282",
		"*mushroomStew = 282",
		"",
		"*goldSword = 283",
		"*swordGold = 283",
		"*goldenSword = 283",
		"*swordGolden = 283",
		"",
		"*goldShovel = 284",
		"*shovelGold = 284",
		"*goldenShovel = 284",
		"*shovelGolden = 284",
		"",
		"*goldPickaxe = 285",
		"*pickaxeGold = 285",
		"*goldenPickaxe = 285",
		"*pickaxeGolden = 285",
		"",
		"*goldAxe = 286",
		"*axeGold = 286",
		"*goldenAxe = 286",
		"*axeGolden = 286",
		"",
		"*string = 287",
		"*silk = 287",
		"",
		"*feather = 288",
		"",
		"*sulphur = 289",
		"*gunpowder = 289",
		"",
		"*woodenHoe = 290",
		"*woodHoe = 290",
		"*hoeWooden = 290",
		"*hoeWood = 290",
		"",
		"*stoneHoe = 291",
		"*hoeStone = 291",
		"",
		"*ironHoe = 292",
		"*hoeIron = 292",
		"*steelHoe = 292",
		"*hoeSteel = 292",
		"",
		"*diamondHoe = 293",
		"*hoeDiamond = 293",
		"",
		"*goldHoe = 294",
		"*goldenHoe = 294",
		"*hoeGold = 294",
		"*hoeGolden = 294",
		"",
		"*seeds = 295",
		"*seed = 295",
		"*wheat = 296",
		"*wheats = 296",
		"*bread = 297",
		"*breads = 297",
		"",
		"*leatherHelmet = 298",
		"*helmetLeather = 298",
		"",
		"*leatherChestplate = 299",
		"*chestplateLeather = 299",
		"",
		"*leatherLeggings = 300",
		"*leggingsLeather = 300",
		"",
		"*leatherBoots = 301",
		"*bootsLeather = 301",
		"",
		"*chainmailHelmet = 302",
		"*helmetChainmail = 302",
		"",
		"*chainmailChestplate = 303",
		"*chestplateChainmail = 303",
		"",
		"*chainmailLeggings = 304",
		"*leggingsChainmail = 304",
		"",
		"*chainmailBoots = 305",
		"*bootsChainmail = 305",
		"",
		"*ironHelmet = 306",
		"*helmetIron = 306",
		"*steelHelmet = 306",
		"*helmetSteel = 306",
		"",
		"*ironChestplate = 307",
		"*chestplateIron = 307",
		"*steelChestplate = 307",
		"*chestplateSteel = 307",
		"",
		"*ironLeggings = 308",
		"*leggingsIron = 308",
		"*steelLeggings = 308",
		"*leggingsSteel = 308",
		"",
		"*ironBoots = 309",
		"*bootsIron = 309",
		"*steelBoots = 309",
		"*bootsSteel = 309",
		"",
		"*diamondHelmet = 310",
		"*helmetDiamond = 310",
		"",
		"*diamondChestplate = 311",
		"*chestplateDiamond = 311",
		"",
		"*diamondLeggings = 312",
		"*leggingsDiamond = 312",
		"",
		"*diamondBoots = 313",
		"*bootsDiamond = 313",
		"",
		"*goldHelmet = 314",
		"*helmetGold = 314",
		"*goldenHelmet = 314",
		"*helmetGolden = 314",
		"",
		"*goldChestplate = 315",
		"*chestplate gold = 315",
		"*goldenChestplate = 315",
		"*chestplate golden = 315",
		"",
		"*goldLeggings = 316",
		"*leggingsGold = 316",
		"*goldenLeggings = 316",
		"*leggingsGolden = 316",
		"",
		"*goldBoots = 317",
		"*bootsGold = 317",
		"*goldenBoots = 317",
		"*bootsGolden = 317",
		"",
		"*flint = 318",
		"*quartz = 318",
		"",
		"*rawPorkchop = 319",
		"*porkchopRaw = 319",
		"*porkchop = 319",
		"",
		"*cookedPorkchop = 320",
		"*porkchopCooked = 320",
		"",
		"*painting = 321",
		"*picture = 321",
		"",
		"*goldenApple = 322",
		"*goldApple = 322",
		"*appleGold = 322",
		"*appleGolden = 322",
		"",
		"*sign = 323",
		"*signItem = 323",
		"",
		"*woodenDoor = 324",
		"*woodDoor = 324",
		"*doorWood = 324",
		"*doorWooden = 324",
		"",
		"*bucket = 325",
		"*bucketEmpty = 325",
		"*emptyBucket = 325",
		"",
		"*waterBucket = 326",
		"*bucketWater = 326",
		"*bucketOfWater = 326",
		"",
		"*lavaBucket = 327",
		"*bucketLava = 327",
		"*bucketOfLava = 327",
		"",
		"*minecart = 328",
		"*cart = 328",
		"*saddle = 329",
		"",
		"*ironDoor = 330",
		"*doorIron = 330",
		"*steelDoor = 330",
		"*doorSteel = 330",
		"",
		"*redstone = 331",
		"*redstoneDust = 331",
		"",
		"*snowBall = 332",
		"*snowBalls = 332",
		"*ballSnow = 332",
		"*ballsSnow = 332",
		"",
		"*boat = 333",
		"",
		"*leather = 334",
		"*skin = 334",
		"*hide = 334",
		"",
		"*milkBucket = 335",
		"*bucketMilk = 335",
		"*bucketOfMilk = 335",
		"",
		"*clayBricks = 336",
		"*clayBrick = 336",
		"*brick = 336",
		"*bricks = 336",
		"",
		"*clayBalls = 337",
		"*clayBall = 337",
		"*ballClay = 337",
		"*ballsClay = 337",
		"",
		"*sugarCane = 338",
		"*reed = 338",
		"*reeds = 338",
		"",
		"*paper = 339",
		"*papers = 339",
		"*book = 340",
		"*books = 340",
		"",
		"*ballSlime = 341",
		"*slimeBall = 341",
		"*ballsSlime = 341",
		"*slimeBalls = 341",
		"",
		"*storageMinecart = 342",
		"*minecartStorage = 342",
		"*minecartChest = 342",
		"*chestMinecart = 342",
		"*minecartWithChest = 342",
		"",
		"*poweredMinecart = 343",
		"*minecartPowered = 343",
		"*minecartFurnace = 343",
		"*minecartWithFurnace = 343",
		"*engineMinecart = 343",
		"*minecartEngine = 343",
		"",
		"*egg = 344",
		"*eggs = 344",
		"*compass = 345",
		"*fishingRod = 346",
		"*rod = 346",
		"*clock = 347",
		"*glowstoneDust = 348",
		"*glowstonePowder = 348",
		"*rawFish = 349",
		"*fish = 349",
		"*cookedFish = 350",
		"*fishRaw = 349",
		"*fishCooked = 350",
		"",
		"*anyDye = 351,-1",
		"*dye = 351",
		"*dyePowder = 351 # meta:",
		"# 0=black (ink sack), 1=red (rose), 2=green (cactus), 3=brown (cocoa), 4=lapis lazuli",
		"# 5=purple, 6=cyan, 7=light gray, 8=gray, 9=pink",
		"# 10=lime, 11=yellow, 12=light blue, 13=magenta, 14=orange, 15=bone meal",
		"",
		"*dyeBlack = 351,0",
		"*blackDye = 351,0",
		"*inkSack = 351,0",
		"*ink = 351,0",

		"*dyeRed = 351,1",
		"*redDye = 351,1",
		"*roseRed = 351,1",
		"*red = 351,1",

		"*dyeGreen = 351,2",
		"*greenDye = 351,2",
		"*cactusGreen = 351,2",
		"*green = 351,2",

		"*dyeBrown = 351,3",
		"*brownDye = 351,3",
		"*cocoaBeans = 351,3",
		"*cocoBeans = 351,3",
		"*brown = 351,3",
		
		"*dyeBlue = 351,4",
		"*blueDye = 351,4",
		"*LapisLazuli = 351,4",
		"*lapis = 351,4",
		
		"*dyePurple = 351,5",
		"*purpleDye = 351,5",
		"*purple = 351,5",

		"*dyeCyan = 351,6",
		"*cyanDye = 351,6",
		"*cyan = 351,6",
		
		"*dyeLightGray = 351,7",
		"*lightGrayDye = 351,7",
		"*lightGray = 351,7",

		"*dyeGray = 351,8",
		"*grayDye = 351,8",
		"*gray = 351,8",
		
		"*dyePink = 351,9",
		"*pinkDye = 351,9",
		"*pink = 351,9",
		
		"*dyeLime = 351,10",
		"*limeDye = 351,10",
		"*lime = 351,10",
		"*dyeLimeGreen = 351,10",
		"*limeGreenDye = 351,10",
		"*limeGreen = 351,10",
		
		"*dyeYellow = 351,11",
		"*yellowDye = 351,11",
		"*yellow = 351,11",
		"*dandelionYellow = 351,11",
		
		"*dyeLightBlue = 351,12",
		"*lightBlueDye = 351,12",
		"*lightBlue = 351,12",
		
		"*dyeMagenta = 351,13",
		"*magentaDye = 351,13",
		"*magenta = 351,13",
		
		"*dyeOrange = 351,14",
		"*orangeDye = 351,14",
		"*orange = 351,14",
		
		"*dyeWhite = 351,15",
		"*whiteDye = 351,15",
		"*boneMeal = 351,15",
		"*white = 351,15",
		"*fertilizer = 351,15",

		"",
		"*bone = 352",
		"*bones = 352",
		"",
		"*sugar = 353",
		"*cake = 354",
		"*cakeItem = 354",
		"*itemCake = 354",
		"",
		"*bed = 355",
		"*bedItem = 355",
		"*itemBed = 355",
		"",
		"*redstoneRepeater = 356",
		"*repeater = 356",
		"",
		"*cookie = 357",
		"*cookies = 357",
		"",
		"*map = 358",
		"*shears = 359",
		"*scissors = 359",
		"",
		"*melonSlice = 360",
		"*melonSliced = 360",
		"*sliceMelon = 360",
		"*slicedMelon = 360",
		"*melonItem = 360",
		"",
		"*seedsPumpkin = 361",
		"*seedsMelon = 362",
		"*PumpkinSeeds = 361",
		"*MelonSeeds = 362",
		"",
		"*rawBeef = 363",
		"*beef = 363",
		"*cowMeat = 363",
		"*meatCow = 363",
		"*beefRaw = 363",
		"*cookedBeef = 364",
		"*beefCooked = 364",
		"*steak = 364",
		"*steakCooked = 364",
		"*cookedSteak = 364",
		"*CowSteak = 364",
		"*SteakCow = 364",
		"",
		"*chicken = 365",
		"*pollo = 365",
		"*chickenRaw = 365",
		"*rawChicken = 365",
		"*chickenCooked = 366",
		"*cookedChicken = 366",
		"*chickenRoasted = 366",
		"*roastedChicken = 366",
		"",
		"*rottenFlesh = 367",
		"*fleshRotten = 367",
		"*zombieMeat = 367",
		"*meatRotten = 367",
		"*rottenMeat = 367",
		"",
		"*enderPearl = 368",
		"*endermanPearl = 368",
		"",
		"*blaze = 369",
		"*blazeRod = 369",
		"*rodBlaze = 369",
		"*blazeStick = 369",
		"*stickBlaze = 369",
		"",
		"*ghastTear = 370",
		"*tearGhast = 370",
		"",
		"*goldNugget = 371",
		"*nuggetGold = 371",
		"",
		"*netherWartSeeds = 372",
		"*seedsNetherWart = 372",
		"",
		"# There are many other potions. Write the damages yourself :P",
		"*bottleWater = 373,0",
		"*WaterBottle = 373,0",
		"*WaterPotion = 373,0",
		"*PotionWater = 373,0",
		"",
		"*potion = 373",
		"*potionRegeneration = 373,1",
		"*regenerationPotion = 373,1",
		"*SwiftnessPotion = 373,2",
		"*potionSwiftness = 373,2",
		"*FireResistancePotion = 373,3",
		"*potionFireResistance = 373,3",
		"*FirePotion = 373,3",
		"*potionFire = 373,3",
		"*potionPoison = 373,4",
		"*PoisonPotion = 373,4",
		"*potionHealing = 373,5",
		"*HealingPotion = 373,5",
		"*potionWeakness = 373,8",
		"*WeaknessPotion = 373,8",
		"*potionStrength = 373,9",
		"*StrengthPotion = 373,9",
		"*potionSlowness = 373,10",
		"*SlownessPotion = 373,10",
		"*potionDiffuse = 373,11",
		"*DiffusePotion = 373,11",
		"*potionHarm = 373,12",
		"*HarmPotion = 373,12",
		"*potionArtless = 373,13",
		"*ArtlessPotion = 373,13",
		"*potionThin = 373,14",
		"*ThinPotion = 373,14",
		"*potionThin2 = 373,15",
		"*ThinPotion2 = 373,15",
		"*potionAwkward = 373,16",
		"*AwkwardPotion = 373,16",
		"",
		"*bottleEmpty = 374",
		"*bottle = 374",
		"*GlassBottle = 374",
		"",
		"*spiderEye = 375",
		"*eyeSpider = 375",
		"*fermentedSpiderEye = 376",
		"*SpiderEyeFermented = 376",
		"",
		"*BlazePowder = 377",
		"*PowderBlaze = 377",
		"",
		"*MagmaCream = 378",
		"*CreamMagma = 378",
		"",
		"*brewingStand = 379",
		"*potionStand = 379",
		"*cauldron = 380",
		"*enderEye = 381",
		"*eyeOfEnder = 381",
		"",
		"*GlisteringMelon = 382",
		"*MelonGlistering = 382",

		"",
		"*EggCreeper = 383,50",
		"*EggSkeleton = 383,51",
		"*EggSpider = 383,52",
		"*EggZombie = 383,54",
		"*EggSlime = 383,55",
		"*EggGhast = 383,56",
		"*EggPigzombie = 383,57",
		"*EggEnderman = 383,58",
		"*EggCaveSpider = 383,59",
		"*EggSilverfish = 383,60",
		"*EggBlaze = 383,61",
		"*EggMagmaCube = 383,62",
		"*EggPig = 383,90",
		"*EggSheep = 383,91",
		"*EggCow = 383,92",
		"*EggChicken = 383,93",
		"*EggSquid = 383,94",
		"*EggWolf = 383,95",
		"*EggMooshroom = 383,96",
		"*EggOcelot = 383,98",
		"*EggOzelot = 383,98",
		"*EggCat = 383,98",
		"*EggVillager = 383,120",
		"",
		"*SpawnerCreeper = 383,50",
		"*SpawnerSkeleton = 383,51",
		"*SpawnerSpider = 383,52",
		"*SpawnerZombie = 383,54",
		"*SpawnerSlime = 383,55",
		"*SpawnerGhast = 383,56",
		"*SpawnerPigzombie = 383,57",
		"*SpawnerEnderman = 383,58",
		"*SpawnerCaveSpider = 383,59",
		"*SpawnerSilverfish = 383,60",
		"*SpawnerBlaze = 383,61",
		"*SpawnerMagmaCube = 383,62",
		"*SpawnerPig = 383,90",
		"*SpawnerSheep = 383,91",
		"*SpawnerCow = 383,92",
		"*SpawnerChicken = 383,93",
		"*SpawnerSquid = 383,94",
		"*SpawnerWolf = 383,95",
		"*SpawnerMooshroom = 383,96",
		"*SpawnerOcelot = 383,98",
		"*SpawnerOzelot = 383,98",
		"*SpawnerCat = 383,98",
		"*SpawnerVillager = 383,120",
		"",
		"*SpawnerEggCreeper = 383,50",
		"*SpawnerEggSkeleton = 383,51",
		"*SpawnerEggSpider = 383,52",
		"*SpawnerEggZombie = 383,54",
		"*SpawnerEggSlime = 383,55",
		"*SpawnerEggGhast = 383,56",
		"*SpawnerEggPigzombie = 383,57",
		"*SpawnerEggEnderman = 383,58",
		"*SpawnerEggCaveSpider = 383,59",
		"*SpawnerEggSilverfish = 383,60",
		"*SpawnerEggBlaze = 383,61",
		"*SpawnerEggMagmaCube = 383,62",
		"*SpawnerEggPig = 383,90",
		"*SpawnerEggSheep = 383,91",
		"*SpawnerEggCow = 383,92",
		"*SpawnerEggChicken = 383,93",
		"*SpawnerEggSquid = 383,94",
		"*SpawnerEggWolf = 383,95",
		"*SpawnerEggMooshroom = 383,96",
		"*SpawnerEggOcelot = 383,98",
		"*SpawnerEggOzelot = 383,98",
		"*SpawnerEggCat = 383,98",
		"*SpawnerEggVillager = 383,120",
		"",
		"*experienceBottle = 384",

		"",
		"*disc13 = 2256",
		"*discGold = 2256",
		"",
		"*discCat = 2257",
		"*discGreen = 2257",
		"",
		"*discBlocks = 2258",
		"*discOrange = 2258",
		"",
		"*discChirp = 2259",
		"*discRed = 2259",
		"",
		"*discFar = 2260",
		"*discLime = 2260",
		"",
		"*discMall = 2261",
		"*discPurple = 2261",
		"",
		"*discMellohi = 2262",
		"*discViolet = 2262",
		"*discPink = 2262",
		"",
		"*discStal = 2263",
		"*discBlack = 2263",
		"",
		"*discStrad = 2264",
		"*discWhite = 2264",
		"",
		"*discWard = 2265",
		"*discCyan = 2265",
		"",
		"*disc11 = 2266",
		"*discCracked = 2266",
		"*discGray = 2266",
		"#end of file"
	};

    @Override
    public int getBurnTime(ItemStack fuel)
    {
        return getFuel(fuel.itemID, fuel.getItemDamage());
    }


}
