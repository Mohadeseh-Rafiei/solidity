package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MCRL2Function {
    private final String name;

    private final List<String> args;

    private final List<String> types;

    private final MCRL2Node node;

    MCRL2Function(String name, List<String> args, List<String> types, MCRL2Node node) {
        this.name = name;
        this.args = args;
        this.types = types;
        this.node = node;
    }

    private MCRL2Node findFunctionBody() {
        return this.node.getChildren().get(this.node.getChildren().size() - 1);
    }

    private String translateRequire(MCRL2Node nodeWithRequire) {
        MCRL2Node foundedNode = nodeWithRequire.findNode("require");
        if (foundedNode == null) {
            return "";
        }
        // todo: fix parser to parse require statement
        if (foundedNode.getParent().findNode("<missing ';'>") != null){
            return "";
        }
        StringBuilder text = new StringBuilder();
        System.out.println("parent: " + foundedNode.getParent().getText());
        String condition = foundedNode.getParent().getChildren().get(2).getChildren().get(0).getChildren().get(0).getText();
        System.out.println("condition: " + condition);
        text.append("(").append(condition).append(") ->\n");
        // todo: make it dynamic by depth
        text.append("(").append(this.name).append("_firstSuccess").append(".EXAMPLE_FUNCTION_CALL!!!)\n");
        text.append("<> ").append(this.name).append("_firstFail").append(".EXAMPLE_FUNCTION_CALL!!!");
        return text.toString();
    }

    private String getBodyText() {
        StringBuilder text = new StringBuilder();
        MCRL2Node body = this.findFunctionBody();
        System.out.println("body: " + body.getText());
        for (MCRL2Node child : body.getChildren()) {
            if (Objects.equals(child.getText(), "{") || Objects.equals(child.getText(), "}")) {
                continue;
            }
            if (child.findNode("require") != null) {
                System.out.println("node with require statement founded:" + child.getText());
                text.append(this.translateRequire(child));
            }
        }
        return text.toString();
    }

    private String getFunctionText() {
        return this.name + "(" +
                this.getArgsText() + ") =\n" +
                this.getBodyText() + ";\n";
    }

    private String getArgsText() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < this.args.size(); i++) {
            if (i != 0) {
                text.append(", ");
            }
            text.append(this.args.get(i)).append(":").append(this.types.get(i));
        }
        return text.toString();
    }

    public MCRL2Node getFunctionMCRL2Node(MCRL2Node parent) {
        String functionText = this.getFunctionText();
        return new MCRL2Node(functionText, parent);
    }
}
