package org.example;

public class MCRL2AST {
    private final MCRL2Node root;

    public MCRL2AST(SolidityAST ast) {
        this.root = MCRL2Node.convertToMCRL2(ast.getRoot(), null);
    }

    public String getText() {
        return root.getText();
    }

    public MCRL2Node getRoot() {
        return root;
    }

    public MCRL2Node findNode(String text) {
        return root.findNode(text);
    }
}
