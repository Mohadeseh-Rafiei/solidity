package org.example;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolidityModifierListener extends SolidityBaseListener {

    private final SolidityAST ast;

    private final List<String> modifiersWithoutArgNames = new ArrayList<>();

    private final List<String> modifiersWithArgNames = new ArrayList<>();

    private final List<SolidityNode> modifiersWithoutArgBodies = new ArrayList<>();

    private final List<SolidityNode> modifiersWithArgBodies = new ArrayList<>();

    private final List<List<SolidityNode>> modifierArgs = new ArrayList<>();

    public SolidityModifierListener(SolidityAST ast) {
        this.ast = ast;
    }

    @Override
    public void enterModifierDefinition(SolidityParser.ModifierDefinitionContext ctx) {
        System.out.println("Enter modifier definition, ctx is: " + ctx.getText());

        SolidityNode currentNode = new SolidityNode(ctx, null);
        currentNode.addChildren(ctx);

        String modifierNameWithoutArg = extractModifierNameWithoutArg(currentNode.getText());

        if (modifierNameWithoutArg != null) {
            // add modifier name to list
            System.out.println("added modifier without arg name to list:" + modifierNameWithoutArg);
            modifiersWithoutArgNames.add(modifierNameWithoutArg);

            // add modifier body to list
            System.out.println("added modifier without arg body to list:" + extractFunctionBody(currentNode).getText());
            modifiersWithoutArgBodies.add(extractFunctionBody(currentNode));
        } else {
            String modifierNameWithArg = extractModifierNameWithArg(currentNode.getText());
            System.out.println("added modifier with arg name to list:" + modifierNameWithArg);
            modifiersWithArgNames.add(modifierNameWithArg);

            // add modifier body to list
            System.out.println("added modifier with arg body to list:" + extractFunctionBody(currentNode).getText());
            modifiersWithArgBodies.add(extractFunctionBody(currentNode));

            modifierArgs.add(extractArgs(currentNode));
        }

        // Exclude modifier declarations from the modified AST
        ast.removeNode(currentNode);
    }

    private static String extractModifierNameWithoutArg(String input) {
        String functionName = null;
        Pattern pattern = Pattern.compile("modifier(\\w+)\\{");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            functionName = matcher.group(1);
        }
        return functionName;
    }

    private static String extractModifierNameWithArg(String input) {
        String functionName = null;
        Pattern pattern = Pattern.compile("modifier(\\w+)\\(");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            functionName = matcher.group(1);
        }
        return functionName;
    }

    private static SolidityNode extractFunctionBody(SolidityNode function) {
        SolidityNode Body = function.getChildren().get(function.getChildren().size() - 1);
        return Body.getChildren().get(1);
    }

    private static List<SolidityNode> extractArgs(SolidityNode input) {
        List<SolidityNode> argNames = new ArrayList<>();
        List<SolidityNode> args = input.getChildren().get(2).getChildren().get(1).getChildren();
        for (int i = 0; i < args.size(); i++) {
            if (i % 2 == 1) {
                argNames.add(args.get(i));
            }
        }
        return argNames;
    }

    private static List<SolidityNode> extractArgsFromUsage(SolidityNode input) {
        return input.getChildren().get(2).getChildren();
    }

    private void findAndRemoveWithoutArgsModifiers() {
        for(int i = 0; i < modifiersWithoutArgNames.size(); i++) {
            String modifier = modifiersWithoutArgNames.get(i);
            SolidityNode modifierBody = modifiersWithoutArgBodies.get(i);
            while (true) {
                SolidityNode foundedNode = ast.findNode(modifier);
                if (foundedNode == null) {
                    break;
                }
                SolidityNode parent = foundedNode.getParent().getParent().getParent();
                addConditionToFunction(parent, modifierBody.getText());
                System.out.println("Function with this modifier:" + parent.getText());
                ast.removeNode(foundedNode);
            }
        }
    }

    private void addConditionToFunction(SolidityNode function, String body) {
        String condition = makeProperCondition(body);
        SolidityNode conditionNode = SolidityNode.makeNodeFromString(condition);
        System.out.println("Try to add condition: " + conditionNode.getText());
        if(function.addAfter("{", conditionNode)) {
            System.out.println("Condition successfully added");
            return;
        }
        System.out.println("Condition nod added!!!");
    }

    private String makeProperCondition(String input) {
        return "if(!(" + input + ")) { return null; }";
    }

    private void findAndRemoveWithArgsModifiers() {
        for(int i = 0; i < modifiersWithArgNames.size(); i++) {
            String modifier = modifiersWithArgNames.get(i);
            SolidityNode modifierBody = modifiersWithArgBodies.get(i);
            List<SolidityNode> args = modifierArgs.get(i);
            while (true) {
                SolidityNode foundedNode = ast.findNode(modifier);
                if (foundedNode == null) {
                    break;
                }
                SolidityNode parent = foundedNode.getParent().getParent().getParent().getParent();
                List<SolidityNode> newArgs = extractArgsFromUsage(foundedNode.getParent());
                String modifierBodyText = changeBodyWithNewArgs(modifierBody.getText(), args, newArgs);
                addConditionToFunction(parent, modifierBodyText);
                System.out.println("Function with this modifier:" + parent.getText());
                ast.removeNode(foundedNode.getParent());
            }
        }
    }

    private String changeBodyWithNewArgs(String body, List<SolidityNode> args, List<SolidityNode> newArgs) {
        for (int i = 0; i < newArgs.size(); i++) {
            body = body.replace(args.get(i).getText(), newArgs.get(i).getText());
        }
        return body;
    }

    private void findAndRemoveAllModifiers() {
        findAndRemoveWithoutArgsModifiers();
        findAndRemoveWithArgsModifiers();
    }
    public SolidityAST getModifiedTree() {
        findAndRemoveAllModifiers();
        return ast;
    }
}