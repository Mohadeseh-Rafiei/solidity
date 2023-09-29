package org.example;

import org.antlr.v4.runtime.tree.TerminalNode;

public class SolidityCommentRemover extends SolidityBaseListener{
    private final SolidityAST ast;
    public SolidityCommentRemover(SolidityAST ast) {
        this.ast = ast;
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    public void removeComments () {
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode("///");
            if (foundedNode == null) {
                break;
            }
            System.out.println("founded comment node: " + foundedNode.getText());
            ast.removeNode(foundedNode);
        }
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode("/*");
            if (foundedNode == null) {
                break;
            }
            System.out.println("founded comment node: " + foundedNode.getText());
            ast.removeNode(foundedNode);
        }
    }

    public SolidityAST getModifiedTree() {
        this.removeComments();
        return this.ast;
    }
}
