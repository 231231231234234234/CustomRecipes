package theboo.mods.customrecipes.api;

import theboo.mods.customrecipes.RecipeLoader;

public class ApiLoader {
	
	public static void addRecipeSyntax(String syntaxName) {
		String[] syntaxes = RecipeLoader.instance.syntaxes;
		int id = -1;
		
		for (int i = 0; i < syntaxes.length; i ++) {
			if (!syntaxes[i].equals("")) {
				id = i;
				break;
			}
		}
		
		if (id == 0) return;
		
		RecipeLoader.instance.syntaxes[id] = syntaxName;
	}
	
	public void getRecipeList() {
		
	}
}
