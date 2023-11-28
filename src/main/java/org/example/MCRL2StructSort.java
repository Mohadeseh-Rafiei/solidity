package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MCRL2StructSort {
    private final MCRL2Node contract;
    private final String structName;
    private final List<String> variableNames = new ArrayList<>();
    private final List<String> variableTypes = new ArrayList<>();

    MCRL2StructSort(MCRL2Node struct){
        this.contract = struct.getParentWithTextInChildren("contract");
        this.structName = struct.getChildren().get(1).getAbstractText();
        this.extractVariables(struct);
    }

    private void extractVariables(MCRL2Node struct) {
        List<MCRL2Node> children = struct.getChildren();
        for (int i = 3; i < children.size(); i++) {
            if(Objects.equals(children.get(i).getText(), ";") || Objects.equals(children.get(i).getText(), "}")) {
                continue;
            }

            this.variableTypes.add(children.get(i).getChildren().get(0).getText());
            this.variableNames.add(children.get(i).getChildren().get(1).getText());

        }
    }

    private String getZeroValue(String type) {
        switch (type){
            case "Bool": return "false";
            case "Int": return "-2";
            default: return "null";
        }
    }

    public void addStructDefinitionToContract() throws Exception {
        StringBuilder mapDefinitions = new StringBuilder();
        for (int i = 0; i < variableNames.size(); i++) {
            StringBuilder var = new StringBuilder();
            String numberOnly= variableTypes.get(i).replaceAll("[^0-9]", "");
            String type = variableTypes.get(i).replaceAll("[^a-zA-Z]+", "");
            if (numberOnly.equals("")) {
                numberOnly = "1";
            }
            for (int j = 0; j < Integer.parseInt(numberOnly); j++) {
                var.append(type);
                if(j < Integer.parseInt(numberOnly) - 1) {
                    var.append('#');
                }
            }
            mapDefinitions.append("get").append(variableNames.get(i).substring(0, 1).toUpperCase()).append(variableNames.get(i).substring(1)).append(" : ").append(structName).append("-> ").append(var).append(";\n");
        }

        StringBuilder varDefinitions = new StringBuilder();
        for (int i = 0; i < variableNames.size(); i++) {
            StringBuilder var = new StringBuilder();
            String numberOnly= variableTypes.get(i).replaceAll("[^0-9]", "");
            String type = variableTypes.get(i).replaceAll("[^a-zA-Z]+", "");
            if (numberOnly.equals("")) {
                numberOnly = "1";
            }
            for (int j = 0; j < Integer.parseInt(numberOnly); j++) {
                var.append(type);
                if(j < Integer.parseInt(numberOnly) - 1) {
                    var.append('#');
                }
            }
            varDefinitions.append(variableNames.get(i)).append(" : ").append(var).append(";\n");
        }

        StringBuilder variableTypesFormat = new StringBuilder();
        for (int i = 0; i < variableTypes.size(); i++) {
            StringBuilder var = new StringBuilder();
            String numberOnly= variableTypes.get(i).replaceAll("[^0-9]", "");
            String type = variableTypes.get(i).replaceAll("[^a-zA-Z]+", "");
            if (numberOnly.equals("")) {
                numberOnly = "1";
            }
            for (int j = 0; j < Integer.parseInt(numberOnly); j++) {
                var.append(type);
                if(j < Integer.parseInt(numberOnly) - 1) {
                    var.append('#');
                }
            }
            if(i == variableTypes.size() - 1) {
                variableTypesFormat.append(var);
            } else {
                variableTypesFormat.append(var).append("#");
            }
        }

        StringBuilder args = new StringBuilder("(");
        for (int i = 0; i < variableNames.size(); i++) {
            if(i == variableNames.size() - 1) {
                args.append(variableNames.get(i)).append(")");
            } else {
                args.append(variableNames.get(i)).append(", ");
            }
        }

        StringBuilder eqnDefinitions = new StringBuilder();
        for (int i = 0; i < variableNames.size(); i++) {
            eqnDefinitions.append("get").append(variableNames.get(i).substring(0, 1).toUpperCase()).append(variableNames.get(i).substring(1)).append(" (emp").append(structName).append(") = ").append(this.getZeroValue(variableTypes.get(i))).append(";\n");
            eqnDefinitions.append("get").append(variableNames.get(i).substring(0, 1).toUpperCase()).append(variableNames.get(i).substring(1)).append(" (add").append(structName).append(args).append(") = ").append(variableNames.get(i)).append(";\n");
        }


        String definition =  "sort " + structName + ";\n" +
                "cons emp"+ structName +" : " + structName + ";\n" +
                "add"+ structName +" : " + variableTypesFormat + " -> "+ structName +";\n" +
                "map\n" + mapDefinitions +
                "var\n" + varDefinitions +
                "eqn\n" + eqnDefinitions ;

        contract.addChildren(new MCRL2Node(definition, contract), 3);
    }

 }
