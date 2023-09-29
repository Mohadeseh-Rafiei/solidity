package org.example;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SolidityPreprocessor {
    public static String preprocessSolidity(String solidityCode) {
        SolidityLexer lexer = new SolidityLexer(CharStreams.fromString(solidityCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SolidityParser parser = new SolidityParser(tokens);
        ParseTree tree = parser.sourceUnit(); // Obtain the root of the parse tree after parsing

        // Step 1: Make AST
        SolidityAST ast = new SolidityAST(tree);

        // Step 2: Handle modifiers and continuation sections
        SolidityModifierListener modifierListener = new SolidityModifierListener(ast);
        ParseTreeWalker.DEFAULT.walk(modifierListener, tree);
        ast = modifierListener.getModifiedTree();// Update modifiedTree

        // Step 3: Remove events and emits
        SolidityEventEmitRemover eventEmitRemover = new SolidityEventEmitRemover(ast);
        ParseTreeWalker.DEFAULT.walk(eventEmitRemover, tree);
        ast = eventEmitRemover.getModifiedTree(); // Update modifiedTree

        // Step 4: Remove pure, view, and constant functions
        SolidityFunctionRemover functionRemover = new SolidityFunctionRemover(ast);
        ParseTreeWalker.DEFAULT.walk(functionRemover, tree);
        ast = functionRemover.getModifiedTree(); // Update modifiedTree

        // Step 5: Remove interfaces
        SolidityInterfaceRemover interfaceRemover = new SolidityInterfaceRemover(ast);
        ParseTreeWalker.DEFAULT.walk(interfaceRemover, tree);
        ast = interfaceRemover.getModifiedTree(); // Update modifiedTree

        SolidityAssemblyRemover assemblyRemover = new SolidityAssemblyRemover(ast);
        ParseTreeWalker.DEFAULT.walk(assemblyRemover, tree);
        ast = assemblyRemover.getModifiedTree();

        SolidityTokenRemover tokenRemover = new SolidityTokenRemover(ast);
        ParseTreeWalker.DEFAULT.walk(tokenRemover, tree);
        ast = tokenRemover.getModifiedTree();

        // Step 6: Keep functions with important features
        cleanCode codeCleaner = new cleanCode(ast);
        ast = codeCleaner.getModifiedTree(); // Update modifiedTree

        SolidityCommentRemover commentRemover = new SolidityCommentRemover(ast);
        ast = commentRemover.getModifiedTree();

        return ast.getText();
    }


    public static void main(String[] args) {
        // Sample Solidity code
        String filePath = "/Users/mohadese.rafiei/IdeaProjects/solidity/src/main/java/org/example/SpankChain.sol";
        String destinationPath = "/Users/mohadese.rafiei/IdeaProjects/solidity/src/main/java/org/example/spankChainOut.sol";
        try {
            String solidityCode = new String(Files.readAllBytes(Paths.get(filePath)));
            String modifiedCode = preprocessSolidity(solidityCode);

            System.out.println(modifiedCode);
            Files.write(Paths.get(destinationPath), modifiedCode.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
