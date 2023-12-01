package org.example;

import java.util.ArrayList;
import java.util.List;

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

    private String getBodyText() {
        // todo: fix body text
        return this.node.getText();
    }

    private String getFunctionText() {
        return this.name + "(" +
                this.getArgsText() + ") =\n" +
                this.getBodyText() + "\n";
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
