package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MCRL2DataTypesTranslator {
    private MCRL2AST ast;

    public MCRL2DataTypesTranslator(MCRL2AST ast) {
        this.ast = ast;
    }

    public MCRL2AST getModifiedTree() throws Exception {
        this.translateInt();
        this.translateBool();
        this.translateAddress();
        return this.ast;
    }

    private void translateInt() {
        List<String> intTypes = new ArrayList<>(Arrays.asList("int8", "int16", "int32", "int64", "int128", "int256", "uint8", "uint16", "uint32", "uint64", "uint128", "uint256"));
        for (String intType : intTypes) {
            while (true) {
                MCRL2Node foundedNode = this.ast.findNode(intType);
                if (foundedNode == null) {
                    break;
                }

                foundedNode.replaceText("Int");
                System.out.println("replace text for "  + intType + " to " + foundedNode.getAbstractText());
            }
        }
    }

    private void translateBool() {
        List<String> boolTypes = new ArrayList<>(List.of("bool"));
        for (String boolType : boolTypes) {
            while (true) {
                MCRL2Node foundedNode = this.ast.findNode(boolType);
                if (foundedNode == null) {
                    break;
                }

                foundedNode.replaceText("Bool");
                System.out.println("replace text for "  + boolType + " to " + foundedNode.getAbstractText());
            }
        }
    }

    private String getAddressDataSort() {
        return "sort Address;\n" +
                "cons Normal, Attacker, Owner, null, Contract : Address ;\n" +
                "map equal : Address#Address -> Bool;\n" +
                "var ad1,ad2: Address;\n" +
                "eqn\n" +
                "equal(Normal, Normal) = true ;\n" +
                "equal(Normal, Attacker) = false ;\n" +
                "equal(Normal, Owner) = false ;\n" +
                "equal(Normal, null) = false;\n" +
                "equal(Normal, Contract) = false;\n" +
                "equal(Attacker, Attacker) = true ;\n" +
                "equal(Attacker, Normal) = false ;\n" +
                "equal(Attacker, Owner) = false ;\n" +
                "equal(Attacker, null) = false;\n" +
                "equal(Attacker, Contract) = false ;\n" +
                "equal(Owner, Owner) = true;\n" +
                "equal(Owner, Normal) = false;\n" +
                "equal(Owner, Attacker) = false ;\n" +
                "equal(Owner, null) = false;\n" +
                "equal(Owner, Contract) = false;\n" +
                "equal(null, null) = true;\n" +
                "equal(null, Normal) = false;\n" +
                "equal(null, Attacker) = false;\n" +
                "equal(null, Owner) = false;\n" +
                "equal(null, Contract) = false;\n" +
                "equal(Contract, Contract) = true;\n" +
                "equal(Contract, Normal) = false ;\n" +
                "equal(Contract, Attacker) = false ;\n" +
                "equal(Contract, Owner) = false ;\n" +
                "equal(Contract, null) = false;\n" +
                "ad1 == ad2 = equal(ad1,ad2);\n";
    }

    private void translateAddress() throws Exception {
        List<String> addressTypes = new ArrayList<>(List.of("address"));
        List<MCRL2Node> contracts = new ArrayList<>();
        for (String addressType : addressTypes) {
            while (true) {
                MCRL2Node foundedNode = this.ast.findNode(addressType);
                if (foundedNode == null) {
                    break;
                }

                MCRL2Node contract = foundedNode.getParentWithTextInChildren("contract");
                if(contract != null && !contracts.contains(contract)) {
                    System.out.println("Add node to contracts: "  + contract.getText());
                    contracts.add(contract);
                }

                foundedNode.replaceText("Address");
                System.out.println("replace text for "  + addressType + " to " + foundedNode.getAbstractText());
            }
        }

        for (MCRL2Node contract : contracts) {
            MCRL2Node addressDefinition = new MCRL2Node(this.getAddressDataSort(), contract);
            contract.addChildren(addressDefinition, 3);
            System.out.println("contract children text: " + contract.getChildren().get(3).getAbstractText());
        }
    }
}
