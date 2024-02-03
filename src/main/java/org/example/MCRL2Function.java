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

    private String translateRequire(MCRL2Node nodeWithRequire, Integer requireCount, String successBody) {
        MCRL2Node foundedNode = nodeWithRequire.findNode("require");
        if (foundedNode == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        System.out.println("parent: " + foundedNode.getParent().getText());
        String condition = foundedNode.getParent().getChildren().get(2).getChildren().get(0).getChildren().get(0).getText();
        System.out.println("condition: " + condition);
        text.append("(").append(condition).append(") ->\n");

        String prefix = getPrefix(requireCount);
        text.append("(").append(this.name).append("_").append(prefix).append("Success").append(".(").append(successBody).append("))\n");
        text.append("<> ").append(this.name).append("_").append(prefix).append("Fail").append(".EXAMPLE_FUNCTION_CALL!!!");
        return text.toString();
    }

    private String getPrefix(Integer requireCount) {
        switch (requireCount) {
            case 1: return "first";
            case 2: return "second";
            case 3: return "third";
            case 4: return "fourth";
            case 5: return "fifth";
            case 6: return "sixth";
            case 7: return "seventh";
            case 8: return "eighth";
            case 9: return "ninth";
            default: return "none";
        }
    }

    private String getBodyText() {
        StringBuilder text = new StringBuilder();
        MCRL2Node body = this.findFunctionBody();
        System.out.println("body: " + body.getText());
        Integer RequireCount = 0;
        List<MCRL2Node> children = body.getChildren();
        for (int i = 0; i < children.size(); i++ ) {
            MCRL2Node child = children.get(i);
            System.out.println("child of require body for translation: " + child.getText());
            if (Objects.equals(child.getText(), "{") || Objects.equals(child.getText(), "}")) {
                continue;
            }
            if (child.findNode("require") != null) {
                RequireCount++;
                System.out.println("node with require statement founded:" + child.getText());
                // todo: find correct parent of require
                MCRL2Node parentOfRequire = child.getParent();
                System.out.println("parent of require: " + parentOfRequire.getText());
                String successBody = this.getSuccessBody(children , i, RequireCount);
                text.append(this.translateRequire(child, RequireCount, successBody));
                return text.toString();
            }
        }
        return text.toString();
    }

    private String getSuccessBody(List<MCRL2Node> children, int startIndex, int count) {
        StringBuilder text = new StringBuilder();
        for (int i = startIndex + 1; i < children.size(); i++) {
            MCRL2Node child = children.get(i);
            if (this.transferCallExists(child)) {
                System.out.println("transfer call founded:" + child.getText());
                text.append(this.translateTransferCall(child));
            }
            if (child.findNode("require") != null) {
                count++;
                System.out.println("node with require statement founded:" + child.getText());
                // todo: find correct parent of require
                MCRL2Node parentOfRequire = child.getParent();
                System.out.println("parent of require: " + parentOfRequire.getText());
                String successBody = this.getSuccessBody(children , i, count);
                text.append(this.translateRequire(child, count, successBody));
                return text.toString();
            }
        }
        return text.toString();
    }

    private boolean transferCallExists(MCRL2Node node) {
        // todo: check other transfer calls
        return node.findNode("transfer") != null || node.findNode("send") != null || node.findNode("call") != null || node.findNode("transferFrom") != null;
    }

    private String translateTransferCall(MCRL2Node node) {
        StringBuilder text = new StringBuilder();
        // todo: test it after fix the grammar
        text.append("((").append("call_transfer_EmptyFallback").append(".EXAMPLE_TRANSFER_BODY!!!").append(") +\n")
                .append("call_transfer_NoFallback").append(".EXAMPLE_TRANSFER_BODY!!!").append(") +\n")
                .append("call_transfer_Fallback").append(".EXAMPLE_TRANSFER_BODY!!!").append("))\n");
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
