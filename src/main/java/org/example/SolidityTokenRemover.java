package org.example;

import org.antlr.v4.runtime.tree.TerminalNode;

public class SolidityTokenRemover extends SolidityBaseListener{
    private final SolidityAST ast;

    public SolidityTokenRemover(SolidityAST ast) {
        this.ast = ast;
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    @Override
    public void enterContractDefinition(SolidityParser.ContractDefinitionContext ctx) {}

    public void removeToken(String name) {
        while (true) {
            SolidityNode foundedNode = ast.findNodeWithDelimiter("is " + name);
            if (foundedNode == null) {
                break;
            }
            System.out.println("founded isToken node: " + foundedNode.getText());
            this.removeToken(foundedNode.getChildren().get(1).getText());
            ast.removeNode(foundedNode);
        }
    }

    public SolidityAST getModifiedTree() {
        this.removeToken("Token");
        return this.ast;
    }
}
