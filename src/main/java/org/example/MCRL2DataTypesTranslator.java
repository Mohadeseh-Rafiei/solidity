package org.example;

import java.util.*;

public class MCRL2DataTypesTranslator {
    private MCRL2AST ast;

    public MCRL2DataTypesTranslator(MCRL2AST ast) {
        this.ast = ast;
    }

    public MCRL2AST getModifiedTree() throws Exception {
        this.translateInt();
        this.translateBool();
        this.translateAddress();
        this.translateMapping();
        this.translateStruct();
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

    public String getMappingDefinition(String mappingType, String mappedType){
        return "sort mapping ;\n" +
                "cons empty : mapping ;\n" +
                "add : " + mappingType + "#" + mappedType + "#mapping -> mapping;\n" +
                "map\n" +
                "retValue : " + mappingType + "#mapping -> " + mappedType + " ;\n" +
                "search : " + mappingType + "#mapping -> Bool ;\n" +
                "update: " + mappingType + "#" + mappedType + "#mapping -> mapping ;\n" +
                "var\n" +
                "array : mapping ;\n" +
                "b, d : " + mappedType + " ;\n" +
                "a, c : " + mappingType + ";\n" +
                "eqn\n" +
                "retValue(a, empty) = -1 ;\n" +
                "retValue(a, add(c, b, array)) = if(a == c, b, retValue(a, array)) ;\n" +
                "search(a, empty) = false;\n" +
                "search(a, add(c, b, array)) = if(a == c, true, search(a, array));\n" +
                "update(a, b, empty) = empty;\n" +
                "update(a, b, add(c, d, array)) = if(a == c, add(a, b, array),add(c, d, update(a, b, array)));\n";
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

    private void translateMapping() throws Exception {
        List<String> mappings = new ArrayList<>(List.of("mapping"));
        List<MCRL2ContractMapping> contracts = new ArrayList<>();
        for (String mapping : mappings) {
                List<MCRL2Node> foundedNodes = this.ast.findAllNodes(mapping);
            for (MCRL2Node foundedNode : foundedNodes) {
                if (foundedNode == null) {
                    break;
                }

                MCRL2Node contract = foundedNode.getParentWithTextInChildren("contract");
                if (contract != null) {
                    String mappingType = foundedNode.getParent().getChildren().get(2).getAbstractText();
                    String mappedType = foundedNode.getParent().getChildren().get(4).getAbstractText();
                    System.out.println("Mapping and mappedType is: " + mappingType + " " + mappedType);
                    MCRL2ContractMapping contractMapping = new MCRL2ContractMapping(contract, mappingType, mappedType);
                    boolean exist = false;
                    for (MCRL2ContractMapping mcrl2ContractMapping : contracts) {
                        if (contractMapping.isEqual(mcrl2ContractMapping)) {
                            exist = true;
                        }
                    }

                    if (!exist) {
                        System.out.println("Add node to contracts: " + contract.getText());
                        contracts.add(contractMapping);
                    }
                }

            }
        }

        for (MCRL2ContractMapping contractMapping : contracts) {
            MCRL2Node addressDefinition = new MCRL2Node(this.getMappingDefinition(contractMapping.getMappingType(), contractMapping.getMappedType()), contractMapping.getContract());
            contractMapping.getContract().addChildren(addressDefinition, 3);
            System.out.println("contract children text: " + contractMapping.getContract().getChildren().get(3).getAbstractText());
        }
    }

    private void translateStruct() throws Exception{
        List<String> structs = new ArrayList<>(List.of("struct"));

        for (String struct : structs) {
            List<MCRL2Node> foundedNodes = this.ast.findAllNodes(struct);
            for (MCRL2Node foundedNode : foundedNodes) {
                if (foundedNode == null) {
                    break;
                }

                MCRL2StructSort structSort = new MCRL2StructSort(foundedNode.getParent());
                this.ast.removeNode(foundedNode.getParent().getParent());
                structSort.addStructDefinitionToContract();
            }
        }
    }

    private void addHarness() throws Exception {
        List<String> functions = new ArrayList<>(List.of("function"));
        List<MCRL2ContractMapping> contracts = new ArrayList<>();
        for (String function : functions) {
            List<MCRL2Node> foundedNodes = this.ast.findAllNodes(function);
            for (MCRL2Node foundedNode : foundedNodes) {
                if (foundedNode == null) {
                    break;
                }

                MCRL2Node contract = foundedNode.getParentWithTextInChildren("contract");
                if (contract != null) {
                    String mappingType = foundedNode.getParent().getChildren().get(2).getAbstractText();
                    String mappedType = foundedNode.getParent().getChildren().get(4).getAbstractText();
                    System.out.println("Mapping and mappedType is: " + mappingType + " " + mappedType);
                    MCRL2ContractMapping contractMapping = new MCRL2ContractMapping(contract, mappingType, mappedType);
                    boolean exist = false;
                    for (MCRL2ContractMapping mcrl2ContractMapping : contracts) {
                        if (contractMapping.isEqual(mcrl2ContractMapping)) {
                            exist = true;
                        }
                    }

                    if (!exist) {
                        System.out.println("Add node to contracts: " + contract.getText());
                        contracts.add(contractMapping);
                    }
                }

            }
        }

        for (MCRL2ContractMapping contractMapping : contracts) {
            MCRL2Node addressDefinition = new MCRL2Node(this.getMappingDefinition(contractMapping.getMappingType(), contractMapping.getMappedType()), contractMapping.getContract());
            contractMapping.getContract().addChildren(addressDefinition, 3);
            System.out.println("contract children text: " + contractMapping.getContract().getChildren().get(3).getAbstractText());
        }
    }
}


