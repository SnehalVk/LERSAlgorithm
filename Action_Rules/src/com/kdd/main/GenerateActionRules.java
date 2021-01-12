package com.kdd.main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class GenerateActionRules {
    private Map<ArrayList<String>, ArrayList<String>> actionRules = new HashMap<>();
    private Map<ArrayList<String>, ArrayList<Integer>> ruleSuppMap= new HashMap<>();


    public  int minSupport;
    public int minConfidence;
    public String decisionValueInitial;
    public String decisionValueEnd;
    private HashSet<String> stableAttribute;
    private HashSet<String> flexibleAttrSet;

    public ArrayList<String> attributeNames= new ArrayList<>();
    public HashMap<String, HashSet<String>> attributeMap= new HashMap<>();

    public HashMap<String, ArrayList<String>> data= new HashMap<>();
    public HashMap<String, HashSet<String>> attributeValueRows= new HashMap<>();


    private LERSAlgorithm LERSAlgorithm;


    public HashSet<String> getUniqueValues(String attributeName){
        HashSet<String> uniqueValues= null;
        if(attributeMap!=null && attributeMap.size()>0){
            HashSet<String> temp=attributeMap.get(attributeName);
            if(temp!=null && temp.size()>0){
                uniqueValues=temp;
                return uniqueValues;
            }
        }
        return uniqueValues;
    }


    public void setFlexibleAttributes(HashSet<String> stableAttributes)
    {
        this.stableAttribute=stableAttributes;
        flexibleAttrSet= new HashSet<>();
        for(String s:attributeNames){
            if(!stableAttributes.contains(s)){
                flexibleAttrSet.add(s);
            }
        }

    }

    public void setSupportAndConfidence(int minSupport, int confidence){
        this.minConfidence=minConfidence;
        this.minSupport=minSupport;
    }


    public void setDecisionAttributes(String decisionAttInitial, String decisionAttEnd) {
        this.decisionValueInitial=decisionAttInitial;
        this.decisionValueEnd=decisionAttEnd;
    }



    public void readFile(File attributeFile, File dataFile) {
        System.out.println("Loading the Data");
        String line;
        int count = 0;
        String[] rowValues;

        HashSet<String> attSet;

        try {
            BufferedReader reader = new BufferedReader((new FileReader(attributeFile)));
            while((line = reader.readLine()) != null) {
                for(String att : line.split(",|\t")) {
                    attributeNames.add(att);
                }
            }
        }catch(IOException e) {
            System.out.println("IO Exception during loading");
            System.out.println(e.getMessage());
        }

        try {

            BufferedReader reader = new BufferedReader((new FileReader(dataFile)));

            while((line = reader.readLine()) != null) {
                count++;
                rowValues = line.split(",|\t");
                data.put("x" + count, new ArrayList<>(Arrays.asList(rowValues)));

                for(int i = 0; i < rowValues.length; i++) {

                    if(rowValues[i].equals("?")) {
                        continue;
                    }
                    String currentValue = rowValues[i];
                    attSet = attributeMap.get(attributeNames.get(i));

                    if(attSet != null) {
                        attSet.add(currentValue);
                        attributeMap.put(attributeNames.get(i), attSet);

                    }else {
                        attSet = new HashSet<>();
                        attSet.add(currentValue);
                        attributeMap.put(attributeNames.get(i), attSet);
                    }

                    String key = attributeNames.get(i) + currentValue;

                    HashSet<String> rows = new HashSet<>();
                    if(attributeValueRows.containsKey(key)) {
                        rows = attributeValueRows.get(key);
                    }
                    rows.add("x" + count);
                    attributeValueRows.put(key, rows);
                }
            }

        } catch(IOException e) {
            System.out.println("IO Exception during reading");
            System.out.println(e.getMessage());
        }
    }



//     Finding Action Rules using LERS Algorithm

    public void findActionRulesUsingLERS() {

        Map<String, HashSet<HashSet<String>>> certainRules = LERSAlgorithm.getCertainRules();
        HashSet<HashSet<String>> toValueSets  = certainRules.get(decisionValueEnd);

        List<String> stableAttributeList;
        List<String> stableAttNamesList;

        if(toValueSets != null && !toValueSets.isEmpty()) {
            Iterator<HashSet<String>> toValIt = toValueSets.iterator();

            while(toValIt.hasNext()) {
                List<String>  initialAndStableOccurences;

                HashSet<String> toValSet = toValIt.next();
                ArrayList<String> toAttNames = new ArrayList<>();
                stableAttNamesList = new ArrayList<>();
                stableAttributeList = findStableAttri(toValSet);

                for(String attribute : stableAttributeList) {
                    stableAttNamesList.add(getAttributeName(attribute));
                }

                for(String attribute : toValSet) {
                    toAttNames.add(getAttributeName(attribute));
                }

                ArrayList<String> stableSupportSearch = new ArrayList<>();
                stableSupportSearch.addAll(stableAttributeList);
                stableSupportSearch.add(decisionValueInitial);
                initialAndStableOccurences = findRows(stableSupportSearch);

                for(Map.Entry<String, HashSet<String>> entry : attributeMap.entrySet()) {

                    if(stableAttNamesList.contains(entry.getKey()) || entry.getKey().equals(getAttributeName(decisionValueEnd)))
                        continue;

                    for(String attVal : entry.getValue()) {
                        if(toValSet.contains(entry.getKey() + attVal))
                            continue;
                        filterAndAddRules(stableAttributeList, entry.getKey(), attVal, initialAndStableOccurences, toAttNames, toValSet);

                    }
                }
            }
        }
    }

    public void filterAndAddRules(List<String> stableAtt, String attrKey, String attVal, List<String>
            initAndStabValOccur, ArrayList<String> toAttNames, HashSet<String> toValSet){

        ArrayList<String> attributeTest = new ArrayList<>();
        attributeTest.addAll(stableAtt);
        attributeTest.add(attrKey + attVal);
        List<String> attriTestOccur = findRows(attributeTest);
        ArrayList<String> toActionSet;

        if(!attriTestOccur.isEmpty() && initAndStabValOccur.containsAll(attriTestOccur)) {
            if(!toAttNames.contains(attrKey)) {
                toActionSet = new ArrayList<>(toValSet);
                toActionSet.add(attrKey + attVal);
                addRule(attributeTest, toActionSet);

            }else {
                toActionSet = new ArrayList<>(toValSet);
                addRule(attributeTest, toActionSet);
            }
        }
    }

    private void addRule(ArrayList<String> fromAction, ArrayList<String> toAction) {
        ArrayList<String> tempFrom = new ArrayList<>();
        ArrayList<String> tempTo = new ArrayList<>();

        tempFrom.addAll(fromAction);
        tempFrom.removeAll(getStableAttri(fromAction));
        tempTo.addAll(toAction);
        tempTo.removeAll(getStableAttri(toAction));

        boolean isValid = true;

        for(Map.Entry<ArrayList<String>, ArrayList<String>> entry : actionRules.entrySet()) {
            if(entry.getKey().equals(fromAction) && entry.getValue().equals(toAction)){
                isValid = false;
            }
        }

        if(checkForSupAndConf(fromAction, toAction)) {
            if(isValid){
                actionRules.put(fromAction, toAction);
            }
        }

    }

    public void printRules() {

        ArrayList<String> fromAction;
        ArrayList<String> fromNames;


        ArrayList<String> toNames;
        ArrayList<String> toAction;


        List<String> stable;
        NumberFormat formatter = new DecimalFormat("#0.00");
        StringBuilder sb= new StringBuilder();

//        sb.append("NEW");
//        sb.append("\n");
//        sb.append("ACTION RULES: ");
//        sb.append("\n");

        StringBuilder re= new StringBuilder();
        Path file = Paths.get("output.txt");

        try  {
            BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.APPEND);
            if(actionRules.isEmpty()) {
                sb.append("No valid rules found");
                sb.append("\n");
                writer.write(sb.toString());
            }
            else {
                for (Map.Entry<ArrayList<String>, ArrayList<String>> entry : actionRules.entrySet()) {

                    fromAction = new ArrayList<>();
                    toAction = new ArrayList<>();


                    fromNames = new ArrayList<>();
                    toNames = new ArrayList<>();

                    fromAction.addAll(entry.getKey());
                    toAction.addAll(entry.getValue());

                    stable = getStableAttri(toAction);

                    for (String attribute : stable) {
                        if (re.toString().isEmpty()) {
                            re.append("[(");
                            re.append(attribute);
                            re.append(")");
                        } else {
                            re.append("^");
                            re.append("(");
                            re.append(attribute);
                            re.append(")");
                        }
                    }
                    fromAction.removeAll(stable);
                    toAction.removeAll(stable);


                    for (String name : fromAction) {
                        fromNames.add(getAttributeName(name));
                    }

                    for (String name : toAction) {
                        toNames.add(getAttributeName(name));
                    }

                    for (String name : toNames) {
                        if (fromNames.contains(name)) {
                            String fromValue = fromAction.get(fromNames.indexOf(name));
                            String toValue = toAction.get(toNames.indexOf(name));

                            if (re.toString().isEmpty()) {
                                if (fromValue.equals(toValue)) {
                                    re.append("[(");
                                    re.append(toValue);
                                    re.append(")");
                                } else {
                                    re.append("[(");
                                    re.append(name);
                                    re.append(", ");
                                    re.append(fromValue);
                                    re.append("-->");
                                    re.append(toValue);
                                    re.append(")");
                                }
                            } else {
                                if (fromValue.equals(toValue)) {
                                    re.append("^(");
                                    re.append(toValue);
                                    re.append(")");
                                } else {
                                    re.append("^(");
                                    re.append(name);
                                    re.append(", ");
                                    re.append(fromValue);
                                    re.append("-->");
                                    re.append(toValue);
                                    re.append(")");
                                }
                            }
                        } else {
                            if (re.toString().isEmpty()) {
                                re.append("[(");
                                re.append(name);
                                re.append(", ");
                                re.append("-->");
                                re.append(toAction.get(toNames.indexOf(name)));
                                re.append(")");
                            } else {
                                re.append("^(");
                                re.append(name);
                                re.append(", ");
                                re.append("-->");
                                re.append(toAction.get(toNames.indexOf(name)));
                                re.append(")");
                            }
                        }
                    }

                    re.append("] --> (");
                    re.append(getAttributeName(decisionValueInitial));
                    re.append(", ");
                    re.append(decisionValueInitial);
                    re.append("-->");
                    re.append(decisionValueEnd);
                    re.append(")");


                    ArrayList<Integer> suppConf = ruleSuppMap.get(entry.getKey());
                    re.append(" SUPPORT: ");
                    re.append(suppConf.get(0));
                    re.append(" CONFIDENCE: ");
                    re.append(formatter.format((suppConf.get(1))));
                    re.append("%");
                    re.append("\n");
                    sb.append(re.toString());
                    writer.write(sb.toString());
                    sb = new StringBuilder();
                    re = new StringBuilder();
                }
            }
            System.out.println("Closing File");
            writer.close();
        }catch (IOException error) {
            System.out.println(error.getStackTrace());

        }
    }

    public boolean checkForSupAndConf(ArrayList<String> fromAction, ArrayList<String> toAction) {
        boolean isValidRule = true;
        int supportStart = 0;
        int supportFromStart = 0;
        int supportEnd = 0;
        int supportToEnd = 0;
        int support;
        int confidence;
        HashSet<String> fromAttIntersection = new HashSet<>();
        HashSet<String> toAttIntersection = new HashSet<>();


        fromAttIntersection=findSupportSet(fromAction, fromAttIntersection);
        supportStart=fromAttIntersection.size();
        fromAttIntersection=findSupportSetForDecision(attributeValueRows.get(decisionValueInitial), fromAttIntersection);
        supportFromStart = fromAttIntersection.size();

        toAttIntersection=findSupportSet(toAction,toAttIntersection);
        supportEnd = toAttIntersection.size();
        toAttIntersection=findSupportSetForDecision(attributeValueRows.get(decisionValueEnd), toAttIntersection);
        supportToEnd = toAttIntersection.size();

        support=supportFromStart < supportToEnd? supportFromStart:supportToEnd;
        confidence=supportStart == 0 || supportEnd == 0?0:(supportFromStart/supportStart) * (supportToEnd/supportEnd) * 100;

        if(support < minSupport || confidence < minConfidence) {
            isValidRule = false;
        }

        if(isValidRule) {
            ArrayList<Integer> suppConf = new ArrayList<>();
            suppConf.add(support);
            suppConf.add(confidence);
            ruleSuppMap.put(fromAction, suppConf);
        }

        return isValidRule;
    }

    private HashSet<String> findSupportSetForDecision(HashSet<String> strings, HashSet<String> fromAttIntersection) {

        HashSet<String> remove= new HashSet<>();
        if(!fromAttIntersection.isEmpty()){
            for(String s: fromAttIntersection){
                if(!strings.contains(s)){
                    remove.add(s);
                }
            }
        }
        fromAttIntersection.removeAll(remove);
        return fromAttIntersection;
    }

    private HashSet<String> findSupportSet(ArrayList<String> fromAction, HashSet<String> fromAttIntersection) {


        for(String attribute : fromAction) {
            HashSet<String> temp = attributeValueRows.get(attribute);
            if(fromAttIntersection.isEmpty()) {
                fromAttIntersection.addAll(temp);
            }else {
                HashSet<String> notCommon = new HashSet<>();
                for(String potentialLine : fromAttIntersection) {
                    if(!temp.contains(potentialLine))
                        notCommon.add(potentialLine);
                }

                fromAttIntersection.removeAll(notCommon);

                if(fromAttIntersection.isEmpty())
                    break;
            }
        }
        return fromAttIntersection;
    }

    public List<String> findRows(List<String> attVal){

        HashSet<String> notCommon;
        HashSet<String> current;
        ArrayList<String> commonSet = new ArrayList<>();


        for(String attribute : attVal) {
            current = attributeValueRows.get(attribute);

            if(commonSet.isEmpty()) {
                commonSet.addAll(current);
            }else {
                notCommon = new HashSet<>();
                for(String potentialLine : commonSet) {
                    if(!current.contains(potentialLine))
                        notCommon.add(potentialLine);
                }

                commonSet.removeAll(notCommon);
                if(commonSet.isEmpty()) {
                    break;
                }
            }
        }

        return commonSet;
    }

    private List<String> findStableAttri(HashSet<String> attSet) {
        ArrayList<String> attributeNames = new ArrayList<>();

        for(String attribute: attSet) {
            if(stableAttribute.contains(getAttributeName(attribute)))
                attributeNames.add(attribute);
        }

        return attributeNames;
    }


    private List<String> getStableAttri(ArrayList<String> set) {
        ArrayList<String> header = new ArrayList<>();

        for(String attribute: set) {
            if(stableAttribute.contains(getAttributeName(attribute)))
                header.add(attribute);
        }

        return header;
    }


    public String getAttributeName(String value) {
        String attName = "";

        for(String name : attributeNames) {
            if(value.startsWith(name)) {
                attName = name;
                break;
            }
        }

        return attName;
    }


    public void generateActionRules(){
        LERSAlgorithm = new LERSAlgorithm(decisionValueInitial, decisionValueEnd, attributeValueRows);
        LERSAlgorithm.executeAlgorithm();
        findActionRulesUsingLERS();
        printRules();
    }

}
