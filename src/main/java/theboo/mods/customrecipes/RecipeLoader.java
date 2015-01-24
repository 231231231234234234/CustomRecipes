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
import java.util.Hashtable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.StatCollector;

import org.apache.logging.log4j.Level;

import theboo.mods.customrecipes.dictionary.Dictionary;
import theboo.mods.customrecipes.lib.Reference;
import theboo.mods.customrecipes.logger.Logger;
import cpw.mods.fml.common.IFuelHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * CustomRecipes RecipeLoader
 * 
 * <br> The loader class loads all the recipes and holds all similar methods.
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
 * @author MightyPork
 *   
 */
public class RecipeLoader implements IFuelHandler {
	
	private Hashtable<String,ItemStack> dict = new Hashtable<String,ItemStack>();
	private Hashtable<String,Integer> fuels = new Hashtable<String,Integer>();
	private int DICT_VERSION = 0;
	
	public static RecipeLoader instance;
	
	public RecipeLoader() {
		instance = this;
	}
	
	public int getFuel(int i, int j) {
		String identifier = Integer.toString(i) + "." + Integer.toString(j);
		if(fuels.get(identifier)==null){return 0;}
		return (Integer)fuels.get(identifier);
	}
	
    @Override
    public int getBurnTime(ItemStack fuel) {
        return getFuel(Item.getIdFromItem(fuel.getItem()), fuel.getItemDamage());
    }
	
	public void loadRecipes()	{
        boolean fail=!(new File(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/dictionary.txt")).exists();
        
        if(fail){
            (new File(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            regenerateDictionary();
            fail=false;
            Logger.log(Level.INFO, "Creating dictionary file.\n");
        }

        loadRecipeFile(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/dictionary.txt", Reference.DEBUG);
        Logger.log(Level.INFO, "Loading dictionary.txt");

        if(DICT_VERSION != Dictionary.DICT_VERSION_CURRENT){
            (new File(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            regenerateDictionary();
            DICT_VERSION = Dictionary.DICT_VERSION_CURRENT;
            Logger.log(Level.INFO, "\nRecipe dictionary is outdated.\nBuilding new dictionary.\n");
            loadRecipeFile(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/dictionary.txt", false);
            Logger.log(Level.INFO, "Loading dictionary.txt");
        }
        
        File dir = new File(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/");
        
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith(".") && !name.substring(name.length()-1).equals("~");
            }
        };

        fail=!(new File(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/dictionary_custom.txt")).exists();

        if(fail){
            (new File(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/dictionary_custom.txt"));
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
                Logger.log(Level.INFO, "Creating empty file for Custom Dictionary.\n");
            }catch(IOException ioe){
                Logger.log(Level.WARN, "* I/O ERROR: Could not create Custom Dictionary file.\n");
            }
        }

        Logger.log(Level.INFO, "Loading custom dictionary: dictionary_custom.txt");
        loadRecipeFile(CustomRecipes.instance.getWorkingFolder() + "/mods/customrecipes/dictionary_custom.txt", true);
        
		generateAliasByLocalizedName();
        
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
                    Logger.log(Level.INFO, "Loading recipes: "+filename);
                    loadRecipeFile(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/"+filename, true);
                }
            }
        }
        
        if(fail){
            Logger.log(Level.INFO, "Dictionary not found, creating new one.");
            (new File(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/")).mkdirs();
            regenerateDictionary();
        }
        
        Logger.log(Level.INFO, "Recipes loaded.\n\n");
	}

	private void regenerateDictionary(){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(CustomRecipes.instance.getWorkingFolder()+"/mods/customrecipes/dictionary.txt"));
			for(int a=0;a<Dictionary.AliasDictionary.length;a++){
				out.write(Dictionary.AliasDictionary[a]);
				out.newLine();
			}
			out.close();
		}
		catch (IOException e)
		{
			Logger.log(Level.WARN, "* I/O ERROR: Could not regenerate the dictionary in .minecraft/mods/customrecipes/dictionary.txt");
		}
	}
	
	public void generateAliasByLocalizedName() {
		Logger.log(Level.INFO, "Adding all the items localized names as dictionary entries...");
		
		try {
			
			for(Object i : GameData.getItemRegistry().getKeys().toArray()) {
				Item item = (Item) i;
				if(item == null) continue;
				
				dict.put(StatCollector.translateToLocal(item.getUnlocalizedName()).replace(" ", ""), new ItemStack(item, 1, 0));
				dict.put(StatCollector.translateToLocal(item.getUnlocalizedName()).replace(" ", "").toLowerCase(), new ItemStack(item, 1, 0));
				dict.put(item.getUnlocalizedName().substring(5).replace(" ", ""), new ItemStack(item, 1, 0));
				dict.put(item.getUnlocalizedName().substring(5).replace(" ", "").toLowerCase(), new ItemStack(item, 1, 0));
			}
			
			
			for(Object b : GameData.getBlockRegistry().getKeys().toArray()) {
				Block block = (Block) b;
				if(block == null) continue;

				dict.put(block.getLocalizedName().replace(" ", ""), new ItemStack(block, 1, 0));
				dict.put(block.getLocalizedName().replace(" ", "").toLowerCase(), new ItemStack(block, 1, 0));
				dict.put(block.getUnlocalizedName().substring(5).replace(" ", ""), new ItemStack(block, 1, 0));
				dict.put(block.getUnlocalizedName().substring(5).replace(" ", "").toLowerCase(), new ItemStack(block, 1, 0));
			}
		} catch (StringIndexOutOfBoundsException e) {
			e.printStackTrace();
			Logger.log(Level.WARN, "Failed to add items unlocalized names as dictionary entries, BUT prevented a crash. This is probably an issue from a mod developer.");
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
			return 0;
		}
	}

	private boolean isValidItem(int i){
		if(i>0 && i <32000){
			if((i<4096 && Block.getBlockById(i)!=null) || (i>=256 && Item.getItemById(i)!=null)){
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
					return (ItemStack) new ItemStack(Item.getItemById(tmpi),1,0);
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
					
					if(dmg >= 32000 && dmg != 32767){
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
		return Item.getIdFromItem(stack.getItem());
	}

	private void loadRecipeFile(String file_path, boolean log){
		//int a,b,c;
		//String tmp;
		ArrayList<String> rawFile=readFile(file_path);
		
		// save to global
		path = file_path;

        
		if(log) Logger.log(Level.INFO, "Started to load recipes at " + file_path);
        
        
		for(int a=0; a < rawFile.size(); a++){
		    
	        
		    if(log) Logger.log(Level.INFO, "Loading Recipes syntaxes at " + file_path);
	        
		    
			String entry=(String)rawFile.get(a);
			String entryOrig=entry;
			
			if(entry.length()>= 4 && entry.substring(0,1).equals("*")){
               
			    if(log) Logger.log(Level.INFO, "Found alias syntax in "+file_path);
				parseRecipeAlias(file_path, entryOrig, entry);

			}else if(entry.length() >= 16 && entry.substring(0,9).equals("shapeless")){

	             Logger.log(Level.INFO, "Found shapeless syntax in "+file_path);
				parseRecipeShapeless(file_path, entryOrig, entry);
				

			}else if(entry.length() >= 9 && entry.substring(0,4).equals("fuel")){
               
			    Logger.log(Level.INFO, "Found fuel syntax in "+file_path);
				parseRecipeFuel(file_path, entryOrig, entry);
				

			}else if(entry.length()>= 15 && entry.substring(0,8).equals("smelting")){
                
			    Logger.log(Level.INFO, "Found smelting syntax in "+file_path);
				parseRecipeSmelting(file_path, entryOrig, entry);
				
				
			}else if(entry.length()>= 13 && entry.substring(0,6).equals("shaped")){
               
			    Logger.log(Level.INFO, "Found shaped syntax in "+file_path);
				parseRecipeShaped(file_path, entryOrig, entry);
				
			}
			else if(entry.length()>= 13 && entry.substring(0,6).equals("remove")){
	             Logger.log(Level.INFO, "Found remove syntax in "+file_path);
				parseRecipeRemove(file_path, entryOrig, entry);
			}
		}
	}
	


	private static final String ERR_MSG_UNDEFINED="Undefined alias or wrong ID (no such block or item exists).\nIf you are trying to get a mod item, try adding 256 to the id value.";
	private static final String ERR_MSG_SYNTAX="Syntax error.";
	private static final String ERR_MSG_SHAPE="Recipe is not rectangular or is larger than 3x3.";
	private static final String ERR_MSG_NOBURNTIME="No burn time specified. Recipe does not make sense.";

	private void errorAlert(String fpath, String line, String cause){
		Logger.log(Level.FATAL, "\n* ERROR in recipe file \""+fpath+"\": "+line+"\n"+(cause==null?"":"  "+cause+"\n"));
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
	                        Logger.log(Level.WARN, "\nSyntax error in recipe file:\n" + url + "\n-> " + tmpString + "\n");
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
				
		Logger.log(Level.INFO, "LOADED RECIPE");
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
			Logger.log(Level.WARN, "Shaped recipe "+entryOrig+" @ path "+path+" threw ArrayIndexOutOfBoundsException");
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
		
		GameRegistry.addSmelting(Block.getBlockById(recipeId),product, 4);
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
			fuels.put((String)Integer.toString(Item.getIdFromItem(fuelStack.getItem()))+ "."+(String)Integer.toString(fuelStack.getItemDamage()) , Integer.valueOf(burntime));
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
        Logger.log(Level.INFO, "About to remove recipe with input" + recipe);
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
	            Logger.log(Level.INFO, "Found shaped recipe!");
	        }

	        if (tmpRecipe instanceof ShapelessRecipes)
	        {
	            ShapelessRecipes recipe = (ShapelessRecipes)tmpRecipe;
	            recipeResult = recipe.getRecipeOutput();
	            Logger.log(Level.INFO, "Found shapeless recipe!");
	        }

	        if (ItemStack.areItemStacksEqual(resultItem, recipeResult))
	        {
	            Logger.log(Level.INFO, "Removed Recipe: " + recipes.get(scan) + " -> " + recipeResult);
	            recipes.remove(scan);
	        }
	        else {
	            Logger.log(Level.WARN, "Couldn't remove the recipe with result: " + resultItem.toString());
	        }
	    }
	}
}
