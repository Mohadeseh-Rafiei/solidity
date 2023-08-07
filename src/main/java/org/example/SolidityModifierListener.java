package org.example;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SolidityModifierListener extends SolidityBaseListener {

    private ParseTreeProperty<ParseTree> modifiedTreeProperty = new ParseTreeProperty<>();
    private ParseTree currentModifier;
    private ParseTree currentContinuationSection;

    public SolidityModifierListener(ParseTreeProperty<ParseTree> modifiedTreeProperty) {
        this.modifiedTreeProperty = modifiedTreeProperty;
    }

    @Override
    public void enterModifierDefinition(SolidityParser.ModifierDefinitionContext ctx) {
        // Mark the current modifier definition
        currentModifier = ctx;
    }

    @Override
    public void enterIfStatement(SolidityParser.IfStatementContext ctx) {
        // Mark the current continuation section (if any)
        currentContinuationSection = ctx;
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    // Add other methods to handle other continuation sections like 'else', 'while', etc.

    public void modifyParseTree(ParseTree originalTree, ParseTree modifiedTree) {
        modifiedTreeProperty.put(originalTree, modifiedTree);
    }

    public ParseTree getModifiedTree(ParseTree currentParseTree) {
        this.modifyParseTree(currentParseTree, currentModifier);
        return modifiedTreeProperty.get(currentParseTree);
    }
}