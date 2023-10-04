package org.example;

import java.util.ArrayList;
import java.util.List;

public class SolidityFunctionKeeper {
    private final SolidityAST ast;
    private final List<SolidityNode> importantFunctions = new ArrayList<>();

    public SolidityFunctionKeeper(SolidityAST ast) {
        this.ast = ast;
    }

    public void findAllImportantFunctions() {
        System.out.println("Finding all important functions");
        findFunctionsWithTransferCalls();
        findFunctionsWithTXOrigin();
        findFunctionsWithDeligateCall();
    }

    public void findFunctionsWithTransferCalls() {
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode(".send(");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            System.out.println("founded function with .send( text: " + function.getText());
            this.importantFunctions.add(function);
            this.ast.removeNode(function);
        }
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode(".call{");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            System.out.println("founded function with .call{ text: " + function.getText());
            this.importantFunctions.add(function);
            this.ast.removeNode(function);
        }
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode(".transfer(");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            System.out.println("founded function with .transfer( text: " + function.getText());
            this.importantFunctions.add(function);
            this.ast.removeNode(function);
        }
    }

    public void findFunctionsWithTXOrigin() {
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode("tx.origin");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            System.out.println("founded function with tx.origin: " + function.getText());
            this.importantFunctions.add(function);
            this.ast.removeNode(function);
        }
    }

    public void findFunctionsWithDeligateCall() {
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode("delegatecall");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            System.out.println("founded function with delegatecall: " + function.getText());
            this.importantFunctions.add(function);
            this.ast.removeNode(function);
        }
    }

    public SolidityNode getFunction(SolidityNode node) {
        if(node.getChildren().get(0).getText().equals("function")) {
            return node;
        }
        return getFunction(node.getParent());
    }

    public SolidityAST getModifiedTree() {
        return this.ast;
    }

    public List<SolidityNode> getImportantFunctions() {
        findAllImportantFunctions();
        return this.importantFunctions;
    }
}
