package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SolidityNode {
    private final ParseTree node;
    private SolidityNode parent;
    private final List<SolidityNode> children = new ArrayList<>();

    public SolidityNode(ParseTree node, SolidityNode parent) {
        this.node = node;
        this.parent = parent;
    }

    public void addChildren(ParseTree root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            this.addChildFromParseTree(root.getChild(i));
        }
    }

    public SolidityNode getParent() {
        return parent;
    }

    public void setParent(SolidityNode node) {
        this.parent = node;
    }

    public SolidityNode findNode(String text) {
        if (Objects.equals(node.getText(), text)) {
            return this;
        }
        for (SolidityNode child : children) {
            SolidityNode temp = child.findNode(text);
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }

    public boolean addAfter(String text, SolidityNode node) {
        for (int i = 0; i < children.size(); i++) {
            if (Objects.equals(children.get(i).getText(), text)) {
                node.setParent(this);
                children.add(i+1, node);
                return true;
            }
        }
        for (SolidityNode child: children) {
            if (child.addAfter(text, node)) {
                return true;
            }
        }
        return false;
    }

    public ParseTree getNode() {
        return node;
    }

    public void addChild(SolidityNode child) {
        children.add(child);
    }

    public List<SolidityNode> getChildren() {
        return children;
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
        SolidityNode childNode = new SolidityNode(child, this);
        for (int i = 0; i < child.getChildCount(); i++) {
            childNode.addChildFromParseTree(child.getChild(i));
        }
        addChild(childNode);
    }

    public static SolidityNode makeNodeFromString(String text) {
        SolidityLexer lexer = new SolidityLexer(CharStreams.fromString(text));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SolidityParser parser = new SolidityParser(tokens);
        ParseTree tree = parser.sourceUnit();
        return new SolidityNode(tree, null);
    }
}