package org.example;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolidityEventEmitRemover extends SolidityBaseListener {
    private final SolidityAST ast;

    private Map<String, SolidityNode> events;


    public SolidityEventEmitRemover(SolidityAST ast) {
        this.ast = ast;
    }

    @Override
    public void enterEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        System.out.println("Enter event definition, ctx is: " + ctx.getText());

        SolidityNode currentNode = new SolidityNode(ctx);

//        // add event to list
//        System.out.println("added node to list:" + currentNode.getChildren().get(0).getText());
//        events.put(currentNode.getChildren().get(0));

        // Exclude event declarations from the modified AST
        ast.removeNode(currentNode);
    }

    @Override
    public void exitEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        System.out.println("Exit event definition");
    }

    @Override
    public void enterEmitStatement(SolidityParser.EmitStatementContext ctx) {
        // Exclude emit statements from the modified AST
        SolidityNode currentNode = new SolidityNode(ctx);
        ast.removeNode(currentNode);
    }

    @Override
    public void exitEmitStatement(SolidityParser.EmitStatementContext ctx) {
        System.out.println("Exit emit definition");
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    public SolidityAST getModifiedTree() {
        return this.ast;
    }
}

