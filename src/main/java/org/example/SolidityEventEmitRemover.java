package org.example;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolidityEventEmitRemover extends SolidityBaseListener {
    private final SolidityAST ast;

    private final List<String> events = new ArrayList<>();


    public SolidityEventEmitRemover(SolidityAST ast) {
        this.ast = ast;
    }
    @Override
    public void enterEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        System.out.println("Enter event definition, ctx is: " + ctx.getText());

        SolidityNode currentNode = new SolidityNode(ctx, null);

        // add event to list
        System.out.println("added node to list:" + extractFunctionName(currentNode.getText()));
        events.add(extractFunctionName(currentNode.getText()));

        // Exclude event declarations from the modified AST
        ast.removeNode(currentNode);
    }

    private static String extractFunctionName(String input) {
        String functionName = null;
        Pattern pattern = Pattern.compile("event(\\w+)\\(");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            functionName = matcher.group(1);
        }
        return functionName;
    }

    @Override
    public void exitEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        System.out.println("Exit event definition");
    }

    @Override
    public void enterEmitStatement(SolidityParser.EmitStatementContext ctx) {
        // Exclude emit statements from the modified AST
        SolidityNode currentNode = new SolidityNode(ctx, null);
        ast.removeNode(currentNode);
    }

    @Override
    public void exitEmitStatement(SolidityParser.EmitStatementContext ctx) {
        System.out.println("Exit emit definition");
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    private void findAndRemoveAllEventCalls() {
        for(String event : events) {
            while (true) {
                SolidityNode foundedNode = ast.findNode(event);
                if (foundedNode == null) {
                    break;
                }
                SolidityNode parent = foundedNode.getParent().getParent();
                System.out.println("Usage of event: " + parent.getText());
                ast.removeNode(parent);
            }
        }
    }

    public SolidityAST getModifiedTree() {
        findAndRemoveAllEventCalls();
        return ast;
    }
}

