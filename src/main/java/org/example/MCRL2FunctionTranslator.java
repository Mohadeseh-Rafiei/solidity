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

    private void translateFunction() throws Exception{
        List<String> functions = new ArrayList<>(List.of("function"));

        for (String function : functions) {
            List<MCRL2Node> foundedNodes = this.ast.findAllNodes(function);
            for (MCRL2Node foundedNode : foundedNodes) {
                if (foundedNode == null) {
                    break;
                }

                // todo : how to calculate args and types?
                // MCRL2Function translatedFunction = new MCRL2Function(foundedNode.getParent());
                this.ast.removeNode(foundedNode.getParent().getParent());
                // todo : add function node instead of prev node
            }
        }
    }

}


