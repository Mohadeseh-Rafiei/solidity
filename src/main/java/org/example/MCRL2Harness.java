package org.example;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCRL2Harness {
Map <String, Map<String, List<String>>> functionInfo = new HashMap<>();
    public void MCRL2Harness(MCRL2Node function) {
        String functionName = extractFunctionName(function.getText());
        List<String> arguments = new ArrayList<>();
        List<String> returnType = extractFunctionReturn(function);

        if(function.findNode("msg.value") != null) {
            arguments.add("value");
        }

        if(function.findNode("msg.sender") != null) {
            arguments.add("addr");
        }

        Map<String, List<String>> functionInf = functionInfo.get(functionName);
        if(functionInf == null) {
            functionInf = new HashMap<>();
            functionInf.put("arguments", arguments);
            functionInf.put("return", returnType);
        }

        functionInfo.put(functionName, functionInf);
    }

    private String getTranslatedHarness() {
        String harness = "harness = \n";

        for (String functionName : functionInfo.keySet()) {
            Boolean haveValue = false;
            Boolean haveAddr = false;

            for (int i = 0; i < functionInfo.get(functionName).get("arguments").size(); i++) {
                if(Objects.equals(functionInfo.get(functionName).get("arguments").get(i), "value")) {
                    haveValue = true;
                }
            }
            for (int i = 0; i < functionInfo.get(functionName).get("arguments").size(); i++) {
                if(Objects.equals(functionInfo.get(functionName).get("arguments").get(i), "addr")) {
                    haveAddr = true;
                }
            }

            if(haveValue) {
                harness = harness + "sum value:Int.(value < 2 && value > 0) -> \n(";
            } else {

            }

            if(haveAddr) {
                harness = harness + "sum addr:Address.(addr == Normal || addr == Attacker) -> \n";
            } else {

            }


        }
        return harness;
    }

    private static String extractFunctionName(String input) {
        String functionName = null;
        Pattern pattern = Pattern.compile("function(\\w+)\\(");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            functionName = matcher.group(1);
        }
        return functionName;
    }

    private static List<String> extractFunctionArguments(MCRL2Node input) {
        String s = "";
        List<String> array = new ArrayList<>();
        array.add(s);
        return array;
    }

    private static List<String >extractFunctionReturn(MCRL2Node input) {
        String s = "";
        List<String> array = new ArrayList<>();
        array.add(s);
        return array;
    }

    public static String translateSolidityLineToMCRL2(MCRL2Node node) {

        // Translate function declaration
        if (node.getText().matches("^function\\s+[a-zA-Z_][a-zA-Z0-9_]*\\(.*\\)\\s*\\{\\s*")) {
            // Extract function name
            Pattern pattern = Pattern.compile("^function\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\(.*\\)\\s*\\{\\s*");
            Matcher matcher = pattern.matcher(node.getText());
            if (matcher.find()) {
                String functionName = matcher.group(1);
                return "proc " + functionName + "() =\n";
            }
        }

        // Translate require statement
        if (node.getText().matches("^require\\(.*\\);")) {
            // Extract the condition inside require
            Pattern pattern = Pattern.compile("^require\\((.*)\\);");
            Matcher matcher = pattern.matcher(node.getText());
            if (matcher.find()) {
                String condition = matcher.group(1);
                return "    require(" + condition + ") <> delta;";
            }
        }

        // Translate if statement
        if (node.getText().matches("^if\\(.*\\)\\s*\\{\\s*")) {
            // Extract the condition inside if
            Pattern pattern = Pattern.compile("^if\\((.*)\\)\\s*\\{\\s*");
            Matcher matcher = pattern.matcher(node.getText());
            if (matcher.find()) {
                String condition = matcher.group(1);
                return "    if (" + condition + ") ->";
            }
        }

        // Translate else statement
        if (node.getText().matches("^else\\s*\\{\\s*")) {
            return "    <> delta";
        }

        // Translate closing brace
        if (node.getText().equals("}")) {
            return "    <> delta;";
        }

        // For other lines, return them as they are
        return "    " + node.getText() + " <> delta;";
    }
}
