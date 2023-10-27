package org.example;
public class Prettifier {
    public static String prettify(MCRL2AST ast) {
        MCRL2Node root = ast.getRoot();
        return root.prettyText();
    }
}