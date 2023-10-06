package org.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SolidityFunctionKeeper {
    private final SolidityAST ast;
    private final List<SolidityNode> importantFunctions = new ArrayList<>();

    public SolidityFunctionKeeper(SolidityAST ast) {
        this.ast = ast;
    }

    private void findAllImportantFunctions() {
        System.out.println("Finding all important functions");
        this.findFunctionsWithTransferCalls();
        this.findFunctionsWithTXOrigin();
        this.findFunctionsWithDeligateCall();
        this.importantFunctions.addAll(this.addNewFunctionsFromImportantFunctions(this.importantFunctions));
    }

    private List<SolidityNode> addNewFunctionsFromImportantFunctions(List<SolidityNode> inputFunctions) {
        List<SolidityNode> newFunctions = new ArrayList<>();
        for (SolidityNode function : inputFunctions) {
            List<String> functionNames = this.getUsedFunctionNames(function);
            for (String functionName : functionNames) {
                SolidityNode foundedFunction = this.findFunctionByName(functionName);
                if (foundedFunction != null) {
                    newFunctions.add(foundedFunction);
                    this.ast.removeNode(foundedFunction);
                }
            }
        }
        if (newFunctions.size() > 0) {
            newFunctions.addAll(this.addNewFunctionsFromImportantFunctions(newFunctions));
        }
        return newFunctions;
    }

    private List<String> getUsedFunctionNames(SolidityNode function) {
        List<String> functionNames = new ArrayList<>();
        // todo: find used function names in function

        while (true) {
            SolidityNode foundedNode = ast.findNode(function.getText());
            if (foundedNode == null) {
                break;
            }
            SolidityNode parent = foundedNode.getParent();
            System.out.println("founded token usage parent: " + parent.getText());
            functionNames.add(parent.getText());
        }

        return functionNames;
    }

    private SolidityNode findFunctionByName(String functionName) {
        return this.ast.findExistInNode("function" + functionName);
    }

    private void findFunctionsWithTransferCalls() {
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

    private void findFunctionsWithTXOrigin() {
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

    private void findFunctionsWithDeligateCall() {
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

    private SolidityNode getFunction(SolidityNode node) {
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
