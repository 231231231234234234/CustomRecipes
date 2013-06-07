package theboo.mods.customrecipes.recipewriter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class FileTree extends JFrame
{
    public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");
    public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");
    public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");
    public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon(
            "expandedfolder.gif");

    protected JTree m_tree;
    protected DefaultTreeModel m_model;
    protected JTextField m_display;

    // NEW
    protected JPopupMenu m_popup;
    protected Action m_action;
    protected TreePath m_clickedPath;

    public FileTree()
    {
        super("Directories Tree [Popup Menus]");
        setSize(400, 300);

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(
                ICON_COMPUTER, null, "Computer"));

        DefaultMutableTreeNode node;
        File[] roots = File.listRoots();
        for (int k = 0; k < roots.length; k++)
        {
            node = new DefaultMutableTreeNode(new IconData(ICON_DISK, null,
                    new FileNode(roots[k])));
            top.add(node);
            node.add(new DefaultMutableTreeNode(new Boolean(true)));
        }

        m_model = new DefaultTreeModel(top);
        m_tree = new JTree(m_model);

        m_tree.putClientProperty("JTree.lineStyle", "Angled");

        TreeCellRenderer renderer = new IconCellRenderer();
        m_tree.setCellRenderer(renderer);

        m_tree.addTreeExpansionListener(new DirExpansionListener());

        m_tree.addTreeSelectionListener(new DirSelectionListener());

        m_tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_tree.setShowsRootHandles(true);
        m_tree.setEditable(false);

        JScrollPane s = new JScrollPane();
        s.getViewport().add(m_tree);
        getContentPane().add(s, BorderLayout.CENTER);

        m_display = new JTextField();
        m_display.setEditable(false);
        getContentPane().add(m_display, BorderLayout.NORTH);

        // NEW
        m_popup = new JPopupMenu();
        m_action = new AbstractAction() {
            public void actionPerformed(ActionEvent e)
            {
                if (m_clickedPath == null) return;
                if (m_tree.isExpanded(m_clickedPath))
                    m_tree.collapsePath(m_clickedPath);
                else
                    m_tree.expandPath(m_clickedPath);
            }
        };
        m_popup.add(m_action);
        m_popup.addSeparator();

        Action a1 = new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e)
            {
                m_tree.repaint();
                JOptionPane.showMessageDialog(FileTree.this,
                        "Delete option is not implemented", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };
        m_popup.add(a1);

        Action a2 = new AbstractAction("Rename") {
            public void actionPerformed(ActionEvent e)
            {
                m_tree.repaint();
                JOptionPane.showMessageDialog(FileTree.this,
                        "Rename option is not implemented", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };
        m_popup.add(a2);
        m_tree.add(m_popup);
        m_tree.addMouseListener(new PopupTrigger());

        WindowListener wndCloser = new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        };
        addWindowListener(wndCloser);

        setVisible(true);
    }

    DefaultMutableTreeNode getTreeNode(TreePath path)
    {
        return (DefaultMutableTreeNode) (path.getLastPathComponent());
    }

    FileNode getFileNode(DefaultMutableTreeNode node)
    {
        if (node == null) return null;
        Object obj = node.getUserObject();
        if (obj instanceof IconData) obj = ((IconData) obj).getObject();
        if (obj instanceof FileNode)
            return (FileNode) obj;
        else
            return null;
    }

    // NEW
    class PopupTrigger extends MouseAdapter
    {
        public void mouseReleased(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                int x = e.getX();
                int y = e.getY();
                TreePath path = m_tree.getPathForLocation(x, y);
                if (path != null)
                {
                    if (m_tree.isExpanded(path))
                        m_action.putValue(Action.NAME, "Collapse");
                    else
                        m_action.putValue(Action.NAME, "Expand");
                    m_popup.show(m_tree, x, y);
                    m_clickedPath = path;
                }
            }
        }
    }

    // Make sure expansion is threaded and updating the tree model
    // only occurs within the event dispatching thread.
    class DirExpansionListener implements TreeExpansionListener
    {
        public void treeExpanded(TreeExpansionEvent event)
        {
            final DefaultMutableTreeNode node = getTreeNode(event.getPath());
            final FileNode fnode = getFileNode(node);

            Thread runner = new Thread() {
                public void run()
                {
                    if (fnode != null && fnode.expand(node))
                    {
                        Runnable runnable = new Runnable() {
                            public void run()
                            {
                                m_model.reload(node);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                }
            };
            runner.start();
        }

        public void treeCollapsed(TreeExpansionEvent event)
        {
        }
    }

    class DirSelectionListener implements TreeSelectionListener
    {
        public void valueChanged(TreeSelectionEvent event)
        {
            DefaultMutableTreeNode node = getTreeNode(event.getPath());
            FileNode fnode = getFileNode(node);
            if (fnode != null)
                m_display.setText(fnode.getFile().getAbsolutePath());
            else
                m_display.setText("");
        }
    }

    public static void main(String argv[])
    {
        new FileTree();
    }
}

class IconCellRenderer extends JLabel implements TreeCellRenderer
{
    protected Color m_textSelectionColor;
    protected Color m_textNonSelectionColor;
    protected Color m_bkSelectionColor;
    protected Color m_bkNonSelectionColor;
    protected Color m_borderSelectionColor;

    protected boolean m_selected;

    public IconCellRenderer()
    {
        super();
        m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
        m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
        m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
        m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
        m_borderSelectionColor = UIManager
                .getColor("Tree.selectionBorderColor");
        setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus)

    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        setText(obj.toString());

        if (obj instanceof Boolean) setText("Retrieving data...");

        if (obj instanceof IconData)
        {
            IconData idata = (IconData) obj;
            if (expanded)
                setIcon(idata.getExpandedIcon());
            else
                setIcon(idata.getIcon());
        }
        else
            setIcon(null);

        setFont(tree.getFont());
        setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
        setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
        m_selected = sel;
        return this;
    }

    public void paintComponent(Graphics g)
    {
        Color bColor = getBackground();
        Icon icon = getIcon();

        g.setColor(bColor);
        int offset = 0;
        if (icon != null && getText() != null)
            offset = (icon.getIconWidth() + getIconTextGap());
        g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);

        if (m_selected)
        {
            g.setColor(m_borderSelectionColor);
            g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
        }

        super.paintComponent(g);
    }
}

class IconData
{
    protected Icon m_icon;
    protected Icon m_expandedIcon;
    protected Object m_data;

    public IconData(Icon icon, Object data)
    {
        m_icon = icon;
        m_expandedIcon = null;
        m_data = data;
    }

    public IconData(Icon icon, Icon expandedIcon, Object data)
    {
        m_icon = icon;
        m_expandedIcon = expandedIcon;
        m_data = data;
    }

    public Icon getIcon()
    {
        return m_icon;
    }

    public Icon getExpandedIcon()
    {
        return m_expandedIcon != null ? m_expandedIcon : m_icon;
    }

    public Object getObject()
    {
        return m_data;
    }

    public String toString()
    {
        return m_data.toString();
    }
}

class FileNode
{
    protected File m_file;

    public FileNode(File file)
    {
        m_file = file;
    }

    public File getFile()
    {
        return m_file;
    }

    public String toString()
    {
        return m_file.getName().length() > 0 ? m_file.getName() : m_file
                .getPath();
    }

    public boolean expand(DefaultMutableTreeNode parent)
    {
        DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent
                .getFirstChild();
        if (flag == null) // No flag
            return false;
        Object obj = flag.getUserObject();
        if (!(obj instanceof Boolean)) return false; // Already expanded

        parent.removeAllChildren(); // Remove Flag

        File[] files = listFiles();
        if (files == null) return true;

        Vector v = new Vector();

        for (int k = 0; k < files.length; k++)
        {
            File f = files[k];
            if (!(f.isDirectory())) continue;

            FileNode newNode = new FileNode(f);

            boolean isAdded = false;
            for (int i = 0; i < v.size(); i++)
            {
                FileNode nd = (FileNode) v.elementAt(i);
                if (newNode.compareTo(nd) < 0)
                {
                    v.insertElementAt(newNode, i);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) v.addElement(newNode);
        }

        for (int i = 0; i < v.size(); i++)
        {
            FileNode nd = (FileNode) v.elementAt(i);
            IconData idata = new IconData(FileTree.ICON_FOLDER,
                    FileTree.ICON_EXPANDEDFOLDER, nd);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
            parent.add(node);

            if (nd.hasSubDirs())
                node.add(new DefaultMutableTreeNode(new Boolean(true)));
        }

        return true;
    }

    public boolean hasSubDirs()
    {
        File[] files = listFiles();
        if (files == null) return false;
        for (int k = 0; k < files.length; k++)
        {
            if (files[k].isDirectory()) return true;
        }
        return false;
    }

    public int compareTo(FileNode toCompare)
    {
        return m_file.getName().compareToIgnoreCase(toCompare.m_file.getName());
    }

    protected File[] listFiles()
    {
        if (!m_file.isDirectory()) return null;
        try
        {
            return m_file.listFiles();
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Error reading directory "
                    + m_file.getAbsolutePath(), "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }
}