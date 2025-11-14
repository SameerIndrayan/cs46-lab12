package trees;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;

public class FamilyTree {

    private static class TreeNode {
        private String name;
        private TreeNode parent;
        private ArrayList<TreeNode> children;

        TreeNode(String name) {
            this.name = name;
            children = new ArrayList<>();
        }

        String getName() {
            return name;
        }

        void addChild(TreeNode childNode) {
            childNode.parent = this;
            this.children.add(childNode);
        }

        // Searches subtree at this node for a node
        // with the given name. Returns the node, or null if not found.
        TreeNode getNodeWithName(String targetName) {
    // 1) Check myself
    if (this.name.equals(targetName)) {
        return this;
    }

    // 2) Recurse into children
    for (TreeNode child : children) {
        TreeNode found = child.getNodeWithName(targetName);
        if (found != null) {
            return found;
        }
    }

    // 3) Not found anywhere in this subtree
    return null;
}


        // Returns a list of ancestors of this TreeNode, starting with this node’s
        // parent and
        // ending with the root. Order is from recent to ancient.
        ArrayList<TreeNode> collectAncestorsToList() {
            ArrayList<TreeNode> ancestors = new ArrayList<>();

            TreeNode cur = this.parent;
            while (cur != null) {
                ancestors.add(cur);
                cur = cur.parent;
            }
            return ancestors;
        }

        public String toString() {
            return toStringWithIndent("");
        }

        private String toStringWithIndent(String indent) {
            String s = indent + name + "\n";
            indent += "  ";
            for (TreeNode childNode : children)
                s += childNode.toStringWithIndent(indent);
            return s;
        }
    }

    private TreeNode root;

    //
    // Displays a file browser so that user can select the family tree file.
    //
    public FamilyTree() throws IOException, TreeException {
        // User chooses input file. This block doesn't need any work.
        FileNameExtensionFilter filter = new FileNameExtensionFilter
        ("Family tree text files", "txt");
        
        File dirf = new File("data");
        if (!dirf.exists()) dirf = new File(".");

        JFileChooser chooser = new JFileChooser(dirf);
        chooser.setFileFilter(filter);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) System.exit(1);
        File treeFile = chooser.getSelectedFile();

        // Parse the input file. Create a FileReader that reads treeFile. Create a BufferedReader
        // that reads from the FileReader.
        FileReader fr = new FileReader(treeFile);
        BufferedReader br = new BufferedReader(fr); 
        String line;
        while ((line = br.readLine()) != null)
            addLine(line);
        br.close();
        fr.close();
    }

    //
    // Line format is "parent:child1,child2 ..."
    // Throws TreeException if line is illegal.
    //
    private void addLine(String line) throws TreeException {
        int colonIndex = line.indexOf(':');
        if (colonIndex < 0) {
            throw new TreeException("Missing ':' in line: " + line);
        }

        String parent = line.substring(0, colonIndex);
        String childrenString = line.substring(colonIndex + 1);
        String[] childrenArray = childrenString.split(",");

        TreeNode parentNode;
        if (root == null) {
            parentNode = root = new TreeNode(parent);
        } else {
            parentNode = root.getNodeWithName(parent);
            if (parentNode == null) {
                throw new TreeException("Parent name " + parent + " not found in tree.");
            }
        }

        for (String childName : childrenArray) {
            TreeNode childNode = new TreeNode(childName.trim());
            childNode.parent = parentNode;
            parentNode.children.add(childNode);
        }
    }

    // Returns the "deepest" node that is an ancestor of the node named name1, and
    // also is an
    // ancestor of the node named name2.
    //
    // "Depth" of a node is the "distance" between that node and the root. The depth
    // of the root is 0. The
    // depth of the root's immediate children is 1, and so on.
    //
    TreeNode getMostRecentCommonAncestor(String name1, String name2) throws TreeException{
        // Get nodes for input names.
        TreeNode node1 = root.getNodeWithName(name1);
        if (node1 == null) throw new TreeException("Unknown person: " + name1);
        TreeNode node2 = root.getNodeWithName(name2);
        if (node2 == null) throw new TreeException("Unknown person: " + name2);
        
        // Get ancestors of node1 and node2.
        ArrayList<TreeNode> ancestorsOf1 = node1.collectAncestorsToList();
        ArrayList<TreeNode> ancestorsOf2 = node2.collectAncestorsToList();
        
        // Check members of ancestorsOf1 in order until you find a node that is also
        // an ancestor of 2. 
        for (TreeNode n1: ancestorsOf1)
            if (ancestorsOf2.contains(n1))
                return n1;
        
        // No common ancestor.
        return null;
    }

    public String toString() {
        return "Family Tree:\n\n" + root;
    }

    public static void main(String[] args) {
    try {
        FamilyTree tree = new FamilyTree();
        System.out.println("Tree:\n" + tree + "\n**************\n");

        // Helper-ish pattern: do each test inside its own try/catch so one failure doesn't stop the rest.
        try {
            TreeNode a = tree.getMostRecentCommonAncestor("Bilbo", "Frodo");   // expect: Balbo
            System.out.println("MRCA(Bilbo, Frodo) = " + (a == null ? "none" : a.getName()));
        } catch (TreeException e) {
            System.out.println("MRCA(Bilbo, Frodo) error: " + e.getMessage());
        }

        try {
            TreeNode a = tree.getMostRecentCommonAncestor("Daisy", "Frodo");   // expect: Fosco
            System.out.println("MRCA(Daisy, Frodo) = " + (a == null ? "none" : a.getName()));
        } catch (TreeException e) {
            System.out.println("MRCA(Daisy, Frodo) error: " + e.getMessage());
        }

        try {
            TreeNode a = tree.getMostRecentCommonAncestor("Bilbo", "Lotho");   // expect: Mungo
            System.out.println("MRCA(Bilbo, Lotho) = " + (a == null ? "none" : a.getName()));
        } catch (TreeException e) {
            System.out.println("MRCA(Bilbo, Lotho) error: " + e.getMessage());
        }

        try {
            TreeNode a = tree.getMostRecentCommonAncestor("Lotho", "Frodo");   // expect: Balbo
            System.out.println("MRCA(Lotho, Frodo) = " + (a == null ? "none" : a.getName()));
        } catch (TreeException e) {
            System.out.println("MRCA(Lotho, Frodo) error: " + e.getMessage());
        }

        // Same person on both sides — with the "ancestors only" definition this returns the parent.
        try {
            TreeNode a = tree.getMostRecentCommonAncestor("Bilbo", "Bilbo");   // expect: Bungo
            System.out.println("MRCA(Bilbo, Bilbo) = " + (a == null ? "none" : a.getName()));
        } catch (TreeException e) {
            System.out.println("MRCA(Bilbo, Bilbo) error: " + e.getMessage());
        }

        // Unknown name should trigger your TreeException
        try {
            TreeNode a = tree.getMostRecentCommonAncestor("Bilbo", "NotAName");
            System.out.println("MRCA(Bilbo, NotAName) = " + (a == null ? "none" : a.getName()));
        } catch (TreeException e) {
            System.out.println("MRCA(Bilbo, NotAName) error (expected): " + e.getMessage());
        }

    } catch (IOException x) {
        System.out.println("IO trouble: " + x.getMessage());
    } catch (TreeException x) {
        System.out.println("Input file trouble: " + x.getMessage());
    }
}

}
