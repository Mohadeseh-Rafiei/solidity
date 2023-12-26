package org.example;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SolidityPreprocessor {
    public static SolidityAST preprocessSolidity(String solidityCode, Boolean SkipModification) {
        SolidityLexer lexer = new SolidityLexer(CharStreams.fromString(solidityCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SolidityParser parser = new SolidityParser(tokens);
        ParseTree tree = parser.sourceUnit(); // Obtain the root of the parse tree after parsing

        // Step 1: Make AST
        SolidityAST ast = new SolidityAST(tree);

        if (SkipModification) {
            return ast;
        }

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

        SolidityConstantRemover constantRemover = new SolidityConstantRemover(ast);
        ast = constantRemover.getModifiedTree();

        SolidityCryptoGraphyRemover cryptoGraphyRemover = new SolidityCryptoGraphyRemover(ast);
        ast = cryptoGraphyRemover.getModifiedTree();

        cleanCode codeCleaner = new cleanCode(ast);
        ast = codeCleaner.getModifiedTree(); // Update modifiedTree

        SolidityCommentRemover commentRemover = new SolidityCommentRemover(ast);
        ast = commentRemover.getModifiedTree();

        // Step 6: Keep functions with important features
        SolidityFunctionKeeper functionKeeper = new SolidityFunctionKeeper(ast);
        // todo: fix it and get ast from it
        functionKeeper.getModifiedTree();

        return ast;
    }

    public static String translateToMCRL2(SolidityAST solidityAST) throws Exception {
        // Step 1: Make AST
        MCRL2AST ast = new MCRL2AST(solidityAST);

        // Step 2: Translate Types
        MCRL2DataTypesTranslator mcrl2IntTranslator = new MCRL2DataTypesTranslator(ast);
        ast = mcrl2IntTranslator.getModifiedTree();

        // Step 3: Translate functions
        MCRL2FunctionTranslator mcrl2FunctionTranslator = new MCRL2FunctionTranslator(ast);
        ast = mcrl2FunctionTranslator.getModifiedTree();

        return Prettifier.prettify(ast);
    }


    public static void main(String[] args) {
        // Sample Solidity code
        String filePath = "src/main/java/org/example/DoS.sol";
        String destinationPath = "src/main/java/org/example/DoS.mcrl2";
        try {
            String solidityCode = new String(Files.readAllBytes(Paths.get(filePath)));

            // todo: skip modification will be false for the final version
            SolidityAST ast = preprocessSolidity(solidityCode, false);

            String translatedCode = translateToMCRL2(ast);

            Files.write(Paths.get(destinationPath), translatedCode.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

/// data
//  functions
//  harness
