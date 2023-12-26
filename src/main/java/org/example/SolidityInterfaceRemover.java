package org.example;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Map;

public class SolidityInterfaceRemover extends SolidityBaseListener {

    private final  SolidityAST ast;
    private Map<String, SolidityNode> interfaces;

    public SolidityInterfaceRemover(SolidityAST ast) {
        this.ast = ast;
    }

    public SolidityAST getModifiedTree() {
        return this.ast;
    }

    @Override
    public void enterFunctionDefinition(SolidityParser.FunctionDefinitionContext ctx) {
        SolidityNode currentNode = new SolidityNode(ctx, null);
        // Remove all function definitions within the interface
        System.out.println("Enter function definition, ctx is: " + ctx.getText());
        if(currentNode.findExistInNode("{") == null) {
            System.out.println("Removing interface function definition: " + currentNode.getText());
            ast.removeNode(currentNode);
        }
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }
}
