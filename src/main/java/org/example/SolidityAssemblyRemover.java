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
    public void enterAssemblyBlock(SolidityParser.AssemblyBlockContext ctx) {}


    public SolidityAST getModifiedTree() {
        while (true) {
            SolidityNode foundedNode = ast.findNode("assembly");
            if (foundedNode == null) {
                break;
            }
            SolidityNode parent = foundedNode.getParent();
            System.out.println("founded assembly node: " + parent.getText());
            ast.removeNode(parent);
        }
        return this.ast;
    }
}
