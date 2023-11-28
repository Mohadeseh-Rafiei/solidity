package org.example;

import java.util.List;

public class MCRL2AST {
    private final MCRL2Node root;

    public MCRL2AST(SolidityAST ast) {
        this.root = MCRL2Node.convertToMCRL2(ast.getRoot(), null);
    }

    public String getText() {
        return root.getText();
    }

    public void removeNode(MCRL2Node node) {
        System.out.println("Removing node: " + node.getText());
        root.removeChild(node);
    }

    public MCRL2Node getRoot() {
        return root;
    }

    public MCRL2Node findNode(String text) {
        return root.findNode(text);
    }

    public List<MCRL2Node> findAllNodes(String text) {
        return root.findAllNodes(text);
    }
}
