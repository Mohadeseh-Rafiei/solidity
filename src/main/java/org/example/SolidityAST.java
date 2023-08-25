package org.example;

import org.antlr.v4.runtime.tree.ParseTree;

public class SolidityAST {
    private final SolidityNode root;

    public SolidityAST(ParseTree root) {
        this.root = new SolidityNode(root, null);
        this.root.addChildren(root);
    }

    public SolidityNode findNode(String text) {
        return root.findNode(text);
    }

    public SolidityNode getRoot() {
        return root;
    }

    public String getText() {
        return root.getText();
    }

    public void removeNode(SolidityNode node) {
        System.out.println("Removing node: " + node.getText());
        root.removeChild(node);
    }
}
