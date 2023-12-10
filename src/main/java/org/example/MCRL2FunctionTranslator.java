package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MCRL2FunctionTranslator {
    private MCRL2AST ast;

    public MCRL2FunctionTranslator(MCRL2AST ast) {
        this.ast = ast;
    }

    public MCRL2AST getModifiedTree() throws Exception {
        this.translateFunction();
        return this.ast;
    }

    private String extractFunctionName(MCRL2Node node) {
        return node.getChildren().get(1).getText();
    }

    private void translateFunction() throws Exception{
        System.out.println("Translating functions");
        List<String> functions = new ArrayList<>(List.of("function"));

        for (String function : functions) {
            List<MCRL2Node> foundedNodes = this.ast.findAllNodes(function);
            for (MCRL2Node foundedNode : foundedNodes) {
                if (foundedNode == null) {
                    break;
                }
                String functionName = this.extractFunctionName(foundedNode.getParent());
                System.out.println("function name: " + functionName);
                // todo : how to calculate args and types?
                MCRL2Function translatedFunction = new MCRL2Function(functionName, new ArrayList<>(), new ArrayList<>(),foundedNode.getParent());

                MCRL2Node parent = foundedNode.getParent().getParent().getParent();
                int index = parent.getChildren().indexOf(foundedNode.getParent().getParent());
                this.ast.removeNode(foundedNode.getParent().getParent());
                parent.addChildren(translatedFunction.getFunctionMCRL2Node(parent), index);

            }
        }
    }

}


