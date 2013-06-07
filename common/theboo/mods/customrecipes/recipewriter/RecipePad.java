package theboo.mods.customrecipes.recipewriter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.UndoManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import theboo.mods.customrecipes.CustomRecipes;
import cpw.mods.fml.client.FMLClientHandler;

public class RecipePad extends JFrame implements ActionListener
{
    private static final long serialVersionUID = 1L;

    String username = System.getProperty("user.name");
    private String mcdir = "C:/Users/" + username + "/AppData/Roaming/.minecraft/";
    
    
    //private TextArea textArea = new TextArea("", 0,0, TextArea.SCROLLBARS_VERTICAL_ONLY);
    private JEditorPane textArea = new JEditorPane();
    
    private MenuBar menuBar = new MenuBar();
    private Menu file = new Menu();
    public JScrollPane scroll;

    private MenuItem newFile = new MenuItem();
    private MenuItem openFile = new MenuItem();
    private MenuItem saveFile = new MenuItem();
    private MenuItem saveFileAs = new MenuItem();
    private MenuItem close = new MenuItem();
    private MenuItem exit = new MenuItem();
    
    private CustomRecipes cr = CustomRecipes.instance;
    
    private Clipboard clip = getToolkit().getSystemClipboard();
    UndoManager undoManager = new UndoManager();

    private Menu edit = new Menu();
    private MenuItem cut = new MenuItem();
    private MenuItem copy = new MenuItem();
    private MenuItem paste = new MenuItem();
  
    
    private Menu debug = new Menu();
    private MenuItem test = new MenuItem();

    private String content;
    private String path = "";
        
    public RecipePad() {
        this.setSize(700, 500);
        this.setTitle("Recipe Writer");
        setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
        
        this.textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        this.textArea.setDocument(new RecipeDocument());
        //this.textArea.getDocument().addDocumentListener(this);
        //textArea.
        //this.textArea.addTextListener(this);
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(textArea);

        scroll = new JScrollPane(textArea, scroll.VERTICAL_SCROLLBAR_AS_NEEDED, scroll.HORIZONTAL_SCROLLBAR_AS_NEEDED); //adding scrollbar to text area;
        this.add(scroll);
        
        this.setMenuBar(this.menuBar);
        this.menuBar.add(this.file);
        
        //file
        this.file.setLabel("File");

        this.newFile.setLabel("New");
        this.newFile.addActionListener(this);
        this.newFile.setShortcut(new MenuShortcut(KeyEvent.VK_N, false));
        this.file.add(this.newFile);
        
        this.openFile.setLabel("Open");
        this.openFile.addActionListener(this);
        this.openFile.setShortcut(new MenuShortcut(KeyEvent.VK_O, false));
        this.file.add(this.openFile);
        
        this.saveFile.setLabel("Save");
        this.saveFile.addActionListener(this);
        this.saveFile.setShortcut(new MenuShortcut(KeyEvent.VK_S, false));
        this.file.add(this.saveFile);
        
        saveFileAs = new MenuItem("Save As");
        saveFileAs.addActionListener(this);
        this.file.add(this.saveFileAs);

        this.close.setLabel("Close");
        this.close.setShortcut(new MenuShortcut(KeyEvent.VK_F4, false));
        this.close.addActionListener(this);
        this.file.add(this.close);
        
        this.exit.setLabel("Exit");
        this.exit.setShortcut(new MenuShortcut(KeyEvent.VK_F4, false));
        this.exit.addActionListener(this);
        this.file.add(exit);
        
        //edit
        this.menuBar.add(this.edit);
        this.edit.setLabel("Edit");
        
        this.cut.setLabel("Cut");
        this.cut.addActionListener(this);
        this.cut.setShortcut(new MenuShortcut(KeyEvent.VK_X, false));
        this.edit.add(this.cut);
        
        this.copy.setLabel("Copy");
        this.copy.addActionListener(this);
        this.copy.setShortcut(new MenuShortcut(KeyEvent.VK_C, false));
        this.edit.add(this.copy);
        
        this.paste.setLabel("Paste");
        this.paste.addActionListener(this);
        this.paste.setShortcut(new MenuShortcut(KeyEvent.VK_V, false));
        this.edit.add(this.paste);
        
        //debug
        this.menuBar.add(this.debug);
        this.debug.setLabel("Debug");
        
        this.test.setLabel("Test");
        this.test.addActionListener(this);
        this.test.setShortcut(new MenuShortcut(KeyEvent.VK_T, false));
        this.debug.add(this.test);
    }
    
    public void actionPerformed(ActionEvent event)
    {
        if(event.getSource() == this.close)
            this.closeFile();
        else if(event.getSource() == this.exit)
            this.exit();
        else if(event.getSource() == this.newFile)
            this.newFile();
        else if(event.getSource() == this.openFile)
            this.openFile();
        else if(event.getSource() == this.saveFile)
            this.saveFile();
        else if(event.getSource() == this.saveFileAs)
            this.saveFileAs();
        else if(event.getSource() == this.cut)
            this.editCut();
        else if(event.getSource() == this.copy)
            this.editCopy();  
        else if(event.getSource() == this.paste)
            this.editPaste();
        else if(event.getSource() == this.test)
            this.testRecipe();
    }
    
    private void openFile()
    {
       // FileTree tree = new FileTree();
        JFileChooser browse = new JFileChooser();
        browse.setCurrentDirectory(new java.io.File(mcdir));
        browse.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int option = browse.showOpenDialog(this);
        if(option == browse.CANCEL_OPTION)
            return;
        File file = browse.getSelectedFile();
        if(file == null || file.getName().equals(""))
        {
            JOptionPane.showMessageDialog(this, "Select a file!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        this.textArea.setText("");
        try
        {
            BufferedReader input = new BufferedReader(new FileReader(file));
            StringBuffer str = new StringBuffer();
            String line;
            while((line = input.readLine()) != null)
                str.append(line + "\n");
            textArea.setText(str.toString());
            path = file.toString();
            this.setTitle(file.getName() + " - Recipe Writer");
            
            /*Scanner scan = new Scanner(new FileReader(browse.getSelectedFile().getPath()));
            while(scan.hasNext())
                this.textArea.append(scan.nextLine() + "\n");
            setTitle(browse.getSelectedFile().getName() + " - Recipe Writer");*/
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(null, "There was an error when opening the file!");
        }
        
    }
    
    private void saveFile()
    {
        if(path == "")
        {
            this.saveFileAs();
        }
        else 
        {
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(path));
                writer.write(textArea.getText());
                content = textArea.getText();
                writer.close();
            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(this,"There was an error when saving the file!","Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveFileAs()
    {
        JFileChooser browse = new JFileChooser();
        browse.setCurrentDirectory(new java.io.File(mcdir));
        browse.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = browse.showSaveDialog(this);
        if(option == browse.CANCEL_OPTION)
            return;
        File file = browse.getSelectedFile();     
        
        if (file == null || file.getName().equals(""))
        {
            JOptionPane.showMessageDialog(null, "You must enter a file name!");
            return;
        }

        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(browse.getSelectedFile().getPath()));
            writer.write(this.textArea.getText());
            content = textArea.getText();
            this.setTitle(browse.getSelectedFile().getName()+ " - Recipe Writer");
            path = file.getName();
            writer.close();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,"There was an error when saving the file!");
        }
    }

    private void newFile()
    {
        if(!isFileChanged())
        {
            textArea.setText("");
            content = "";
            path = "";
            this.setTitle("Untitled - Recipe Writer");
        }
        else 
        {
            int dialog = this.promptSaveDialog();
            if(dialog == 0)
                saveFile();
            else if(dialog==1){
                textArea.setText("");
                path = "";
                setTitle("Untitled - Recipe Writer");
            }
            else if(dialog==2)
                return;
        }
    }
    
    private void closeFile()
    {
        if(!isFileChanged())
        {
            textArea.setText("");
            path = "";
            this.setTitle("Untitled - Recipe Writer");
        }
        else 
        {
            int dialog = this.promptSaveDialog();
            if(dialog == 0)
                saveFile();
            else if(dialog==1){
                textArea.setText("");
                path = "";
                setTitle("Untitled - Recipe Writer");
            }
            else if(dialog==2)
                return;
        }
    }
    
    private void exit()
    {
        if(!isFileChanged())
        {
            this.dispose();
        }
        else 
        {
            int dialog = this.promptSaveDialog();
            if(dialog == 0)
                saveFile();
            else if(dialog==1){
                this.dispose();
            }
            else if(dialog==2)
                return;
        }
    }
    
    private void editCut()
    {
        String sel = textArea.getSelectedText();
        StringSelection ss = new StringSelection(sel);
        clip.setContents(ss,ss);
        //textArea.replaceRange(" ",textArea.getSelectionStart(),textArea.getSelectionEnd());
        textArea.select(textArea.getSelectionStart(), textArea.getSelectionEnd());
        textArea.replaceSelection(" ");
    }
    
    private void editCopy()
    {
        String sel = textArea.getSelectedText();
        StringSelection ss = new StringSelection(sel);
        clip.setContents(ss, ss);
    }
    
    private void editPaste()
    {
        Transferable text = clip.getContents(this);
        try
        {
            String sel = (String)text.getTransferData(DataFlavor.stringFlavor);
            textArea.select(textArea.getSelectionStart(), textArea.getSelectionEnd());
            textArea.replaceSelection(sel);
            //textArea.replaceRange(sel, textArea.getSelectionStart(), textArea.getSelectionEnd());
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(null, "There was an error when pasting the text!");
        }
    }
    
    private void testRecipe()
    {            
        Minecraft mc = FMLClientHandler.instance().getClient();
        try
        {
            
            EntityPlayer player = mc.thePlayer;            
            int x = (int)player.posX;
            int y = (int)player.posY;
            int z = (int)player.posZ;
            player.displayGUIWorkbench(x, y, z);
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(null,"You have to enter a minecraft world to display the workbench GUI!");
        }
    }
    
    private int promptSaveDialog()
    {
        return JOptionPane.showConfirmDialog(null, "The text has been changed\nDo you want to save the changes?");
    }
    
    private boolean isFileChanged()
    {
        if(textArea.getText().equals("") || textArea.getText().equals(content))
            return true;
        else return false;
    }
}
