package org.example;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SolidityInterfaceRemover extends SolidityBaseListener {

    private ParseTreeProperty<ParseTree> modifiedTreeProperty;

    public SolidityInterfaceRemover(ParseTreeProperty<ParseTree> modifiedTreeProperty) {
        this.modifiedTreeProperty = modifiedTreeProperty;
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

    @Override
    public void enterTerminal(TerminalNode node) {

    }
}
