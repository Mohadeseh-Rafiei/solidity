package org.example;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SolidityAssemblyRemover extends SolidityBaseListener{
    private final SolidityAST ast;

    public SolidityAssemblyRemover(SolidityAST ast) {
        this.ast = ast;
    }

    @Override
    public void enterAssemblyBlock(SolidityParser.AssemblyBlockContext ctx) {
        System.out.println("Enter assembly function definition, ctx is: " + ctx.getText());

        SolidityNode currentNode = new SolidityNode(ctx, null);
        System.out.println("Remove assembly node: " + ctx.getText());
        ast.removeNode(currentNode);
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    public SolidityAST getModifiedTree() {
        return this.ast;
    }
}
