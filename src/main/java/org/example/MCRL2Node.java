package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCRL2Node {
    private String text;
    private MCRL2Node parent;
    private List<MCRL2Node> children = new ArrayList<>();

    public MCRL2Node(String text, MCRL2Node  parent) {
        this.text = text;
        this.parent = parent;
    }

    public String getAbstractText() {
        return this.text;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();

        if (children.isEmpty()) {
            return this.text;
        }
        // Call getText recursively on children until there are no more children
        for (MCRL2Node child : children) {
            sb.append(child.getText());
        }

        return sb.toString();
    }

    public void setChildren(List<MCRL2Node> children) {
        this.children = children;
    }

    public List<MCRL2Node> getChildren() {
        return this.children;
    }

    public static MCRL2Node convertToMCRL2(SolidityNode node, MCRL2Node parent) {
        if(node == null) {
            return null;
        }

        MCRL2Node convertedNode =  new MCRL2Node(node.getAbstractText(), parent);
        List<SolidityNode> children = node.getChildren();
        List<MCRL2Node> convertedChildren = new ArrayList<>();
        for (SolidityNode child : children) {
            convertedChildren.add(MCRL2Node.convertToMCRL2(child, convertedNode));
        }
        convertedNode.setChildren(convertedChildren);
        return convertedNode;
    }

    public String prettyText() {
        StringBuilder sb = new StringBuilder();

        if (children.isEmpty()) {
            String text =  this.text;
            if(Objects.equals(text, ";")) {
                return ";\n";
            }
            if(Objects.equals(text, "{")) {
                return "{\n";
            }
            if(Objects.equals(text, "}")) {
                return "}\n";
            }
            return text;
        }
        // Call getText recursively on children until there are no more children
        for (MCRL2Node child : children) {
            if(sb.length() > 0 && sb.charAt(sb.length()-1) != ' ') {
                sb.append(' ');
            }
            sb.append(child.prettyText());
        }

        return sb.toString();
    }

    public MCRL2Node findNode(String text) {
        if (Objects.equals(this.text, text)) {
            return this;
        }
        for (MCRL2Node child : children) {
            MCRL2Node temp = child.findNode(text);
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }

    public MCRL2Node getParent() {
        return this.parent;
    }

    public void replaceText(String text) {
        this.text = text;
    }

    public MCRL2Node getParentWithTextInChildren(String text) {
        if(this.parent == null) {
            return null;
        }

        List<MCRL2Node> children = this.parent.getChildren();

        for (MCRL2Node child : children) {
            if (Objects.equals(child.getAbstractText(), text)) {
                return this.parent;
            }
        }

        return this.parent.getParentWithTextInChildren(text);
    }

    public void addChildren(MCRL2Node node, Integer index) throws Exception {
        if(this.children.size() < index) {
            throw new Exception("Children size is less than index required.");
        }
        this.children.add(index, node);
    }

    public List<MCRL2Node> findAllNodes(String text) {
        List<MCRL2Node> nodes = new ArrayList<>();
        if (Objects.equals(this.text, text)) {
            nodes.add(this);
            return nodes;
        }
        for (MCRL2Node child : children) {
            List<MCRL2Node> temp = child.findAllNodes(text);
            if (temp != null) {
                nodes.addAll(temp);
            }
        }
        return nodes;
    }
}