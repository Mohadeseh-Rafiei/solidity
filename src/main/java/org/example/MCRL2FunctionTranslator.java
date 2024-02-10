package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MCRL2FunctionTranslator {
    private MCRL2AST ast;

    public MCRL2FunctionTranslator(MCRL2AST ast) {
        this.ast = ast;
    }

    public MCRL2AST getModifiedTree() throws Exception {
        this.translateFunction();
        this.removeContractVariables();
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
                MCRL2Node functionNode = this.getFunctionNode(foundedNode);
                replaceValueAndAddressInMSG(functionNode);

                List<String> args = getFunctionArgs(functionNode);
                System.out.println("args: " + args);
                List<String> types = getArgsTypes(functionNode);
                System.out.println("types: " + types);
                MCRL2Function translatedFunction = new MCRL2Function(functionName, args, types,foundedNode.getParent().getParent());

                MCRL2Node parent = functionNode.getParent();
                int index = parent.getChildren().indexOf(functionNode);

                parent.addChildren(translatedFunction.getFunctionMCRL2Node(parent), index);
                this.ast.removeNode(functionNode);
            }
        }
    }

    private MCRL2Node getFunctionNode(MCRL2Node node) {
        if (node.getChildren().size() > 0 && Objects.equals(node.getChildren().get(0).getText(), "function")) {
            System.out.println("function node for function: " + node.getParent().getText());
            return node.getParent();
        }
        return this.getFunctionNode(node.getParent());
    }

    private void replaceValueAndAddressInMSG(MCRL2Node node) throws Exception {
        boolean valueExist = false;
        boolean addrExist = false;
        while (true) {
            MCRL2Node foundedNode = node.findNode("msg.value");
            if(foundedNode == null) {
                break;
            }

            valueExist = true;
            foundedNode.replaceText("value");
            foundedNode.setChildren(new ArrayList<>());
        }

        while (true) {
            MCRL2Node foundedNode = node.findNode("msg.sender");
            if(foundedNode == null) {
                break;
            }

            addrExist = true;
            foundedNode.replaceText("addr");
            foundedNode.setChildren(new ArrayList<>());
        }

        if(addrExist) {
            MCRL2Node arg = new MCRL2Node("", node);
            MCRL2Node typeArg = new MCRL2Node("Address", arg);
            MCRL2Node addrArg = new MCRL2Node("addr", arg);
            List<MCRL2Node> children = new ArrayList<>();
            children.add(typeArg);
            children.add(addrArg);
            arg.setChildren(children);
            node.getChildren().get(1).addChildren(arg, 1);
            if(node.getChildren().get(1).getChildren().size() > 3) {
                MCRL2Node comma = new MCRL2Node(",", node);
                node.getChildren().get(1).addChildren(comma, 3);
            }
            System.out.println("msg.addr in args: " + node.getChildren().get(1).getChildren().get(1).getText());
        }
        if(valueExist) {
            MCRL2Node arg = new MCRL2Node("", node);
            MCRL2Node typeArg = new MCRL2Node("Int", arg);
            MCRL2Node valueArg = new MCRL2Node("value", arg);
            List<MCRL2Node> children = new ArrayList<>();
            children.add(typeArg);
            children.add(valueArg);
            arg.setChildren(children);
            node.getChildren().get(1).addChildren(arg, 1);
            if(node.getChildren().get(1).getChildren().size() > 3) {
                MCRL2Node comma = new MCRL2Node(",", node);
                node.getChildren().get(1).addChildren(comma, 2);
            }
            System.out.println("msg.value in args: " + node.getChildren().get(1).getText());
        }
    }

    private List<String> getFunctionArgs(MCRL2Node node) {
        List<String> args = new ArrayList<>();
        args.add("balance");
        args.addAll(getContractVariables(node.getParent()));
        List<MCRL2Node> children = node.getChildren().get(1).getChildren();
        System.out.println("children for  extracting args: " + node.getChildren().get(1).getText());

        for (int i = 0; i < children.size(); i++) {
            if(Objects.equals(children.get(i).getText(), ",") || Objects.equals(children.get(i).getText(), "(") || Objects.equals(children.get(i).getText(), ")")) {
                continue;
            }

            System.out.println("args: " + children.get(i).getText());
            args.add(children.get(i).getChildren().get(1).getText());
        }

        return args;
    }

    private List<String> getArgsTypes(MCRL2Node node) {
        List<String> types = new ArrayList<>();
        types.add("Int");
        types.addAll(getContractVariableTypes(node.getParent()));
        List<MCRL2Node> children = node.getChildren().get(1).getChildren();

        for (int i = 0; i < children.size(); i++) {
            if(Objects.equals(children.get(i).getText(), ",") || Objects.equals(children.get(i).getText(), "(") || Objects.equals(children.get(i).getText(), ")")) {
                continue;
            }

            types.add(children.get(i).getChildren().get(0).getText());
        }

        return types;
    }

    private List<String> getContractVariables(MCRL2Node node) {
        MCRL2Node contractNode = this.getContractNode(node);
        List<MCRL2Node> children = contractNode.getChildren();
        List<String> args = new ArrayList<>();

        for (int i = 3; i < children.size(); i++) {
            if(children.get(i).getText().contains("function")) {
                continue;
            }

            if(children.get(i).getChildren().size() == 0) {
                continue;
            }

            int size = children.get(i).getChildren().get(0).getChildren().size();
            if(size < 2) {
                continue;
            }

            String text = children.get(i).getChildren().get(0).getChildren().get(1).getText();
            if (text.equals("public")) {
                text = children.get(i).getChildren().get(0).getChildren().get(2).getText();
            }
            System.out.println("contract variable: " + text);
            args.add(text);

        }

        return  args;
    }

    private MCRL2Node getContractNode(MCRL2Node node) {
        if (node.getChildren().size() > 0 && Objects.equals(node.getChildren().get(0).getText(), "contract")) {
            return node;
        }
        return this.getContractNode(node.getParent());
    }

    private List<String> getContractVariableTypes(MCRL2Node node) {
        MCRL2Node contractNode = this.getContractNode(node);
        List<MCRL2Node> children = contractNode.getChildren();
        List<String> types = new ArrayList<>();

        for (int i = 3; i < children.size(); i++) {
            if(children.get(i).getText().contains("function")) {
                continue;
            }

            if(children.get(i).getChildren().size() == 0) {
                continue;
            }

            if(children.get(i).getChildren().get(0).getChildren().size() < 2) {
                continue;
            }

            MCRL2Node typeNode = children.get(i).getChildren().get(0).getChildren().get(0);
            if(typeNode.getText().contains("mapping")) {
                System.out.println("contract variable type: mapping");
                types.add("mapping");
            } else {
                System.out.println("contract variable type: " + typeNode.getText());
                types.add(typeNode.getText());
            }
        }

        return types;
    }

    private void removeContractVariables() {
        MCRL2Node contractNode = this.getContractNode(this.ast.getRoot().getChildren().get(0));
        List<MCRL2Node> children = contractNode.getChildren();

        for (int i = 3; i < children.size(); i++) {
            System.out.println("contract variable to check for remove: " + children.get(i).getText());
            if(children.get(i).getText().contains("function")) {
                continue;
            }

            if(children.get(i).getChildren().size() == 0) {
                continue;
            }

            if(children.get(i).getChildren().get(0).getChildren().size() < 2) {
                continue;
            }
            System.out.println("contract variable to remove: " + children.get(i).getText());
            this.ast.removeNode(children.get(i));
        }
    }
}


