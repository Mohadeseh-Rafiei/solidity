package org.example;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Objects;

public class SolidityEventEmitRemover extends SolidityBaseListener {
    private ParseTree currentModifier;

    private ParseTreeProperty<ParseTree> modifiedTreeProperty;

    public SolidityEventEmitRemover(ParseTreeProperty<ParseTree> modifiedTreeProperty) {
        this.modifiedTreeProperty = modifiedTreeProperty;
    }

    @Override
    public void enterEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        // Exclude event declarations from the modified AST
        modifiedTreeProperty.put(ctx, null);
    }

    @Override
    public void exitEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        currentModifier = ctx;
    }

    @Override
    public void enterEmitStatement(SolidityParser.EmitStatementContext ctx) {
        // Exclude emit statements from the modified AST
        modifiedTreeProperty.put(ctx, null);
    }

    @Override
    public void exitEmitStatement(SolidityParser.EmitStatementContext ctx) {
        currentModifier = ctx;
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    @Override
    public void exitFunctionDefinition(SolidityParser.FunctionDefinitionContext ctx) {
        // Modify the function's children in the modified AST
        List<ParseTree> modifiedChildren = removeNulls(ctx.children);
        ctx.children = List.of(modifiedChildren.toArray(new ParseTree[0]));
        currentModifier = ctx;
    }

    // Helper method to remove nulls from a list of children
    private List<ParseTree> removeNulls(List<ParseTree> children) {
        children.removeIf(Objects::isNull);
        return children;
    }

    public void modifyParseTree(ParseTree originalTree, ParseTree modifiedTree) {
        modifiedTreeProperty.put(originalTree, modifiedTree);
    }

    public ParseTree getModifiedTree(ParseTree currentParseTree) {
        modifyParseTree(currentParseTree, currentModifier);
        return modifiedTreeProperty.get(currentParseTree);
    }
}

