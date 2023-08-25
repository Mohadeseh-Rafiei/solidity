package org.example;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolidityFunctionRemover extends SolidityBaseListener {

    private final SolidityAST ast;
    private List<String> functions = new ArrayList<>();

    public SolidityFunctionRemover(SolidityAST ast) {
        this.ast = ast;
    }


    @Override
    public void enterFunctionDefinition(SolidityParser.FunctionDefinitionContext ctx) {
        System.out.println("Enter function definition, ctx is: " + ctx.getText());

        String mod = ctx.modifierList().getText();
        if(mod.equals("pure") || mod.equals("view") || mod.equals("constant") || mod.equals("externalconstant") || mod.equals("constantreturns") || mod.equals("externalconstantreturns")) {
            SolidityNode currentNode = new SolidityNode(ctx, null);
            System.out.println("added node to list:" + extractFunctionName(currentNode.getText()));
            functions.add(extractFunctionName(currentNode.getText()));
            ast.removeNode(currentNode);
        }
    }

    private static String extractFunctionName(String input) {
        String functionName = null;
        Pattern pattern = Pattern.compile("function(\\w+)\\(");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            functionName = matcher.group(1);
        }
        return functionName;
    }

    @Override
    public void exitFunctionDefinition(SolidityParser.FunctionDefinitionContext ctx) {
	     System.out.println("Exit event definition");
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    // Helper method to traverse the original parse tree and construct the modified parse tree
    public SolidityAST getModifiedTree() {
        findAndRemoveAllEventCalls();
        return this.ast;
    }

    private void findAndRemoveAllEventCalls() {
        for(String function : functions) {
            while (true) {
                SolidityNode foundedNode = ast.findNode(function);
                if (foundedNode == null) {
                    break;
                }
                SolidityNode parent = foundedNode.getParent().getParent();
                System.out.println("Usage of function: " + parent.getText());
                ast.removeNode(parent);
            }
        }
    }
}
