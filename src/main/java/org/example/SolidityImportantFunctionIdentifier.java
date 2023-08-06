package org.example;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SolidityImportantFunctionIdentifier extends SolidityBaseListener {

    private Set<String> importantFunctions = new HashSet<>();
    private final ParseTreeProperty<ParseTree> modifiedTreeProperty;

    public SolidityImportantFunctionIdentifier(ParseTreeProperty<ParseTree> modifiedTreeProperty) {
        this.modifiedTreeProperty = modifiedTreeProperty;
        this.importantFunctions = new HashSet<>();
    }

    @Override
    public void exitFunctionCall(SolidityParser.FunctionCallContext ctx) {
        // Check if the function call has important features
        String functionName = ctx.expression().identifier().getText();
        if (hasImportantFeatures(ctx)) {
            // Mark the function with important features
            importantFunctions.add(functionName);
            // Recursively analyze the body of the called function to identify other important functions
            SolidityImportantFunctionIdentifier recursiveFunctionIdentifier = new SolidityImportantFunctionIdentifier(modifiedTreeProperty);
            ParseTreeWalker.DEFAULT.walk(recursiveFunctionIdentifier, ctx);
            importantFunctions.addAll(recursiveFunctionIdentifier.getImportantFunctions());

            // Store the modified tree for this important function
            modifiedTreeProperty.put(ctx, ctx);
        }
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    public Set<String> getImportantFunctions() {
        return importantFunctions;
    }

    @Override
    public void enterFunctionCall(SolidityParser.FunctionCallContext ctx) {
        // Check if the function call has important features
        String functionName = ctx.expression().identifier().getText();
        if (hasImportantFeatures(ctx)) {
            // Mark the function with important features
            importantFunctions.add(functionName);
            // Recursively analyze the body of the called function to identify other important functions
            SolidityImportantFunctionIdentifier recursiveFunctionIdentifier = new SolidityImportantFunctionIdentifier(modifiedTreeProperty);
            ParseTreeWalker.DEFAULT.walk(recursiveFunctionIdentifier, ctx);
            importantFunctions.addAll(recursiveFunctionIdentifier.getImportantFunctions());
        }
    }

    private boolean hasImportantFeatures(SolidityParser.FunctionCallContext ctx) {
        // Helper method to check if the function call has important features
        // You need to implement this method according to your requirement.
        // For example, if it contains `transfer`, `send`, etc.
        // It should recursively traverse the function call and its arguments to look for important features.
        // If any important feature is found, return true.
        // Otherwise, return false.
        return false;
    }

    public ParseTree getModifiedTree(ParseTree currentParseTree) {
        return modifiedTreeProperty.get(currentParseTree);
    }

}
