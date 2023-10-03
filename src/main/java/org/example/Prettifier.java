package org.example;
public class Prettifier {
    public static String prettify(SolidityAST ast) {
        SolidityNode root = ast.getRoot();
        return root.prettyText();
    }
}