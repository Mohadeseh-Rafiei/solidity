package org.example;

import org.antlr.v4.runtime.tree.ParseTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SolidityNode {
    private final ParseTree node;
    private final List<SolidityNode> children = new ArrayList<>();

    public SolidityNode(ParseTree node) {
        this.node = node;
    }

    public ParseTree getNode() {
        return node;
    }

    public void addChild(SolidityNode child) {
        children.add(child);
    }

    public boolean removeChild(SolidityNode node) {
        for (SolidityNode child : children) {
            if (Objects.equals(node.getText(), child.getText())) {
                return children.remove(child);
            }
        }
        for (SolidityNode child : children) {
            if (child.removeChild(node)) {
                return true;
            }
        }
        return false;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();

        if (children.isEmpty()) {
            return node.getText();
        }
        // Call getText recursively on children until there are no more children
        for (SolidityNode child : children) {
            sb.append(child.getText());
        }

        return sb.toString();
    }

    public void addChildFromParseTree(ParseTree child) {
        SolidityNode childNode = new SolidityNode(child);
        for (int i = 0; i < child.getChildCount(); i++) {
            childNode.addChildFromParseTree(child.getChild(i));
        }
        addChild(childNode);
    }
}