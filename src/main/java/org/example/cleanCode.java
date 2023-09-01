package org.example;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.TerminalNode;

public class cleanCode extends SolidityBaseListener {
    private SolidityAST ast;
    private final List<String> functions =  new ArrayList<>();

    public cleanCode(SolidityAST ast) {
        this.ast = ast;
    }

    public SolidityAST getModifiedTree() {
        this.removeEmptyContracts();
        return this.ast;
    }

    @Override
    public void enterTerminal(TerminalNode node) {

    }

    public void removeEmptyContracts() {
        while (true) {
            SolidityNode foundedNode = this.ast.findExistInNode("{}");
            if(foundedNode == null) {
                return;
            }
            this.ast.removeNode(foundedNode);
        }
    }
}
