package org.example;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.ArrayList;
import java.util.List;

public class SolidityModifierListener extends SolidityBaseListener {

    private ParseTreeProperty<ParseTree> modifiedTreeProperty = new ParseTreeProperty<>();
    private List<ParseTree> modifiers = new ArrayList<>();

    public SolidityModifierListener(ParseTreeProperty<ParseTree> modifiedTreeProperty) {
        this.modifiedTreeProperty = modifiedTreeProperty;
    }

    @Override
    public void enterModifierDefinition(SolidityParser.ModifierDefinitionContext ctx) {
        // Mark the current modifier definition
        modifiers.add(ctx);
//        modifiedTreeProperty.put(ctx, ctx);
    }

    @Override
    public void enterIfStatement(SolidityParser.IfStatementContext ctx) {
        // Mark the current continuation section (if any)
//        currentContinuationSection = ctx;
        modifiedTreeProperty.put(ctx, ctx);
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    // Add other methods to handle other continuation sections like 'else', 'while', etc.

//    public void modifyParseTree(ParseTree originalTree, ParseTree modifiedTree) {
//        modifiedTreeProperty.put(originalTree, modifiedTree);
//    }

    public ParseTree getModifiedTree(ParseTree currentParseTree) {
//        this.modifyParseTree(currentParseTree, currentModifier);
        modifiedTreeProperty.put(currentParseTree, currentParseTree);
        return modifiedTreeProperty.get(currentParseTree);
    }
}