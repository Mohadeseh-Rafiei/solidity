package org.example;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;
import java.util.List;

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

    public ParseTree getModifiedTree(ParseTree currentParseTree) {
        return modifiedTreeProperty.get(currentParseTree);
    }
}
