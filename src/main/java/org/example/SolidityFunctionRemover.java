package org.example;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SolidityFunctionRemover extends SolidityBaseListener {

    private final ParseTreeProperty<ParseTree> modifiedTreeProperty;
    private final Set<String> excludedModifiers = new HashSet<>(Arrays.asList("pure", "view", "constant"));

    public SolidityFunctionRemover(ParseTreeProperty<ParseTree> modifiedTreeProperty) {
        this.modifiedTreeProperty = modifiedTreeProperty;
    }


    @Override
    public void enterFunctionDefinition(SolidityParser.FunctionDefinitionContext ctx) {
        // Check if the function has excluded modifiers
        if (hasExcludedModifier(ctx)) {
            // Exclude functions with excluded modifiers from the modified AST
            modifiedTreeProperty.put(ctx, null);
        } else {
            // Include other functions in the modified AST
            modifiedTreeProperty.put(ctx, ctx);
        }
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    private boolean hasExcludedModifier(SolidityParser.FunctionDefinitionContext ctx) {
        SolidityParser.ModifierListContext modifierListContext = ctx.modifierList();
        if (modifierListContext != null) {
            for (int i = 0; i < modifierListContext.getChildCount(); i++) {
                ParseTree child = modifierListContext.getChild(i);
                if (child instanceof ParserRuleContext) {
                    ParserRuleContext childContext = (ParserRuleContext) child;
                    String modifierName = childContext.getText();
                    if (excludedModifiers.contains(modifierName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public ParseTree getModifiedTree(ParseTree currentParseTree) {
        return modifiedTreeProperty.get(currentParseTree);
    }

    // Helper method to traverse the original parse tree and construct the modified parse tree
    public void constructModifiedTree(ParseTree originalNode, ParseTree modifiedNode) {
        if (originalNode.getChildCount() != modifiedNode.getChildCount()) {
            throw new IllegalArgumentException("Children count mismatch between original and modified parse trees.");
        }

        for (int i = 0; i < originalNode.getChildCount(); i++) {
            ParseTree originalChild = originalNode.getChild(i);
            ParseTree modifiedChild = modifiedNode.getChild(i);

            modifiedTreeProperty.put(modifiedChild, modifiedChild);

            constructModifiedTree(originalChild, modifiedChild);
        }
    }
}
