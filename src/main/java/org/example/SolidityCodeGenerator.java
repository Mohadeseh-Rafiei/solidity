package org.example;

import java.util.Set;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Set;

public class SolidityCodeGenerator extends SolidityBaseListener {
    private StringBuilder modifiedCodeBuilder;
    private Set<String> importantFunctions;

    public SolidityCodeGenerator(Set<String> importantFunctions) {
        this.modifiedCodeBuilder = new StringBuilder();
        this.importantFunctions = importantFunctions;
    }

    @Override
    public void enterFunctionDefinition(SolidityParser.FunctionDefinitionContext ctx) {
        String functionName = ctx.identifier().getText();
        if (importantFunctions.contains(functionName)) {
            // Only include functions that are marked as important
            modifiedCodeBuilder.append(ctx.getText()).append("\n");
        }
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        modifiedCodeBuilder.append(node.getText());
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    public String getModifiedCode() {
        return modifiedCodeBuilder.toString();
    }
}
