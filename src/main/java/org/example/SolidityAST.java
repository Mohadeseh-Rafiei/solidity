package org.example;

import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class SolidityAST {
    private final SolidityNode root;

    public SolidityAST(ParseTree root) {
        this.root = new SolidityNode(root, null);
        this.root.addChildren(root);
    }

    public SolidityNode findNode(String text) {
        return root.findNode(text);
    }

    public SolidityNode findExistInNode(String text) {
        return root.findExistInNode(text);
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

    public SolidityNode findNodeWithDelimiter(String text) {
        return root.findNodeWithDelimiter(text);
    }

    public List<SolidityNode> findAllNodes(String text) {
        return root.findAllNodes(text);
    }
}
