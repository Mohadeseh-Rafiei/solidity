package org.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SolidityFunctionKeeper {
    private final SolidityAST ast;
    private final List<SolidityNode> importantFunctions = new ArrayList<>();
    private final List<SolidityNode> allFunctions = new ArrayList<>();

    public SolidityFunctionKeeper(SolidityAST ast) {
        this.ast = ast;
    }

    private void extractAllFunctions() {
        while (true) {
            SolidityNode foundedNode = ast.findNode("function");
            if (foundedNode == null) {
                break;
            }
            SolidityNode parent = foundedNode.getParent();
            allFunctions.add(parent);
            System.out.println("founded  function node: " + parent.getText());
            ast.removeNode(parent);
        }
    }

    private void findAllImportantFunctions() {
        System.out.println("Finding all important functions");
        this.findFunctionsWithTransferCalls();
        this.findFunctionsWithTXOrigin();
        this.findFunctionsWithDeligateCall();
        this.extractAllFunctions();
        System.out.println("AST: " + this.ast.getText());
        if (this.allFunctions.size() != 0) {
            if (this.allFunctions.get(0).getParent().getParent().getChildren().size() > 1) {
                System.out.println("function node parent name: " + this.allFunctions.get(0).getParent().getParent().getChildren().get(1).getText());
            }
        }
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
        for(SolidityNode func : allFunctions) {
            String funcName = func.getChildren().get(1).getText();
            System.out.println("searching for usage of function: " + funcName);
            if (function.findExistInNode(funcName) != null) {
                functionNames.add(funcName);
                System.out.println("adding function name: " + funcName);
            }
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
            if (function != null) {
                System.out.println("founded function with .send( text: " + function.getText());
                this.importantFunctions.add(function);
                this.ast.removeNode(function);
            }
            else {
                System.out.println("function is null");
                this.ast.removeNode(foundedNode);
            }
        }
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode(".call{");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            if (function != null) {
                System.out.println("founded function with .call{ text: " + function.getText());
                this.importantFunctions.add(function);
                this.ast.removeNode(function);
            }
            else {
                System.out.println("function is null");
                this.ast.removeNode(foundedNode);
            }
        }
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode(".transfer(");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            if (function != null) {
                System.out.println("founded function with .transfer( text: " + function.getText());
                this.importantFunctions.add(function);
                this.ast.removeNode(function);
            }
            else {
                System.out.println("function is null");
                this.ast.removeNode(foundedNode);
            }
        }
    }

    private void findFunctionsWithTXOrigin() {
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode("tx.origin");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            if (function != null) {
                System.out.println("founded function with tx.origin: " + function.getText());
                this.importantFunctions.add(function);
                this.ast.removeNode(function);
            }
            else {
                System.out.println("function is null");
                this.ast.removeNode(foundedNode);
            }
        }
    }

    private void findFunctionsWithDeligateCall() {
        while (true) {
            SolidityNode foundedNode = ast.findExistInNode("delegatecall");
            if (foundedNode == null) {
                break;
            }
            SolidityNode function = getFunction(foundedNode);
            if (function != null) {
                System.out.println("founded function with delegatecall: " + function.getText());
                this.importantFunctions.add(function);
                this.ast.removeNode(function);
            }
            else {
                System.out.println("function is null");
                this.ast.removeNode(foundedNode);
            }
        }
    }

    private SolidityNode getFunction(SolidityNode node) {
        if (node == null) {
            return null;
        }
        if(!(node.getChildren().isEmpty()) && node.getChildren().get(0).getText().equals("function")) {
            return node;
        }
        return getFunction(node.getParent());
    }

    private void addImportantFunctionsToAST() {
        for (SolidityNode importantFunction : importantFunctions) {
            String parentName = importantFunction.getParent().getParent().getChildren().get(1).getText();
            System.out.println("Adding function to ast: " + parentName);
            SolidityNode foundedNode = this.ast.findNode(parentName).getParent();
            if(foundedNode != null) {
                foundedNode.addChild(importantFunction);
            }
        }
    }

    public SolidityAST getModifiedTree() {
        findAllImportantFunctions();
        this.addImportantFunctionsToAST();
        return this.ast;
    }

    public List<SolidityNode> getImportantFunctions() {
        findAllImportantFunctions();
        return this.importantFunctions;
    }
}
