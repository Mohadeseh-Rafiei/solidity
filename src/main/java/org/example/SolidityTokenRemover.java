package org.example;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

public class SolidityTokenRemover extends SolidityBaseListener{
    private final SolidityAST ast;
    List<String> tokenNames  = new ArrayList<>();

    public SolidityTokenRemover(SolidityAST ast) {
        this.ast = ast;
    }

    @Override
    public void enterContractDefinition(SolidityParser.ContractDefinitionContext ctx) {}

    public void removeTokenUsages() {
        for (String tokenName : this.tokenNames) {
            while (true) {
                SolidityNode foundedNode = ast.findNode(tokenName);
                if (foundedNode == null) {
                    break;
                }
                SolidityNode parent = foundedNode.getParent();
                System.out.println("founded token usage parent: " + parent.getText());
                ast.removeNode(parent);
            }
        }
    }

    public void removeToken(String name) {
        this.tokenNames.add(name);
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
        this.removeTokenUsages();
        return this.ast;
    }
}
