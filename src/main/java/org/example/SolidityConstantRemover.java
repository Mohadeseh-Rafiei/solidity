package org.example;

import org.antlr.v4.runtime.tree.TerminalNode;

public class SolidityConstantRemover extends SolidityBaseListener{
    private final SolidityAST ast;
    public SolidityConstantRemover(SolidityAST ast) {
        this.ast = ast;
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    public void removeConstants () {
        while (true) {
            SolidityNode foundedNode = ast.findNode("constant");
            if (foundedNode == null) {
                break;
            }
            SolidityNode parent = foundedNode.getParent();
            System.out.println("founded constant node parent: " + parent.getText());
            ast.removeNode(parent);
        }
    }

    public SolidityAST getModifiedTree() {
        this.removeConstants();
        return this.ast;
    }
}
