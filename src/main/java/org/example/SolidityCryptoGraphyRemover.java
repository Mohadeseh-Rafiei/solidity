package org.example;

public class SolidityCryptoGraphyRemover {
    private final SolidityAST ast;
    public SolidityCryptoGraphyRemover(SolidityAST ast) {
        this.ast = ast;
    }

    private void removeCryptoGraphyCommands() {
        while (true) {
            SolidityNode foundedNode = ast.findNode("keccak256");
            if (foundedNode == null) {
                break;
            }
            SolidityNode parent = foundedNode.getParent().getParent();
            System.out.println("founded keccak256 node: " + parent.getText());
            ast.removeNode(parent);
        }
        while (true) {
            SolidityNode foundedNode = ast.findNode("abi.encodePacked");
            if (foundedNode == null) {
                break;
            }
            SolidityNode parent = foundedNode.getParent().getParent();
            System.out.println("founded abi.encodePacked node: " + parent.getText());
            ast.removeNode(parent);
        }
    }

    public SolidityAST getModifiedTree() {
        this.removeCryptoGraphyCommands();
        return this.ast;
    }
}
