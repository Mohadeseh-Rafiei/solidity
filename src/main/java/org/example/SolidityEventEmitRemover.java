package org.example;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Objects;

public class SolidityEventEmitRemover extends SolidityBaseListener {
    private ParseTree modifiedTree;

    private SolidityAST ast;

    private ParseTreeProperty<ParseTree> modifiedTreeProperty = new ParseTreeProperty<>();

    public SolidityEventEmitRemover(ParseTree tree, SolidityAST ast) {
        this.modifiedTree = tree;
        this.ast = ast;
    }

    @Override
    public void enterEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        System.out.println("Enter event definition, ctx is: " + ctx.getText());

        // Exclude event declarations from the modified AST
        SolidityNode currentNode = new SolidityNode(ctx);
        ast.removeNode(currentNode);
    }

    @Override
    public void exitEventDefinition(SolidityParser.EventDefinitionContext ctx) {
        System.out.println("Exit event definition");
    }

    @Override
    public void enterEmitStatement(SolidityParser.EmitStatementContext ctx) {
        // Exclude emit statements from the modified AST
        SolidityNode currentNode = new SolidityNode(ctx);
        ast.removeNode(currentNode);
    }

    @Override
    public void exitEmitStatement(SolidityParser.EmitStatementContext ctx) {
        System.out.println("Exit emit definition");
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

//    @Override
//    public void exitFunctionDefinition(SolidityParser.FunctionDefinitionContext ctx) {
//        // Modify the function's children in the modified AST
//        List<ParseTree> modifiedChildren = removeNulls(ctx.children);
//        ctx.children = List.of(modifiedChildren.toArray(new ParseTree[0]));
////        modifiedTreeProperty.put(ctx, ctx);
//    }

    // Helper method to remove nulls from a list of children
    private List<ParseTree> removeNulls(List<ParseTree> children) {
        children.removeIf(Objects::isNull);
        return children;
    }

    public SolidityAST getModifiedTree() {
//        return modifiedTreeProperty.get(currentParseTree);
        return this.ast;
    }
}

