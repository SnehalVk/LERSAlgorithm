package com.kdd.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class LERSAlgorithm {
    private HashSet<String> decisionSetInit;

    private HashSet<String> decisionSetTo;

    private Map<String, HashSet<HashSet<String>>> certainRules = new HashMap<>();

    private Map<HashSet<String>, HashSet<String>> attributeValues = new HashMap<>();

    private Map<HashSet<String>, Integer> supportRules = new HashMap<>();

    private ArrayList<String> possibleRules = new ArrayList<>();

    private String decisionValueEnd;

    private String decisionValueStart;



    public LERSAlgorithm(String decisionValueInitial, String decisionValueTo, Map<String, HashSet<String>> attributeValues) {

        this.decisionSetInit = attributeValues.get(decisionValueInitial);
        this.decisionSetTo = attributeValues.get(decisionValueTo);

        this.decisionValueStart = decisionValueInitial;
        this.decisionValueEnd = decisionValueTo;

        HashSet<String> tempSet;
        for(Map.Entry<String, HashSet<String>> entry : attributeValues.entrySet()) {
            tempSet = new HashSet<>();
            tempSet.add(entry.getKey());
            this.attributeValues.put(tempSet, entry.getValue());
        }

    }


//     LERS Algorithm loops until no unmarked sets found.Each iteration finds out certain rules and
//     then from the set of possible rules combines the attributes and again make them marked if possible.

    public void executeAlgorithm() {

        HashSet<String> removeKeySet;
        HashMap<HashSet<String>, HashSet<String>> valueMap = new HashMap<>();
        File output = new File("output.txt");
        int iteration = 0;

        if(output.exists())
            output.delete();

        Path file = Paths.get("output.txt");

        try  {
            BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE);

            removeKeySet = new HashSet<>();
            removeKeySet.add(decisionValueStart);
            attributeValues.remove(removeKeySet);
            removeKeySet.clear();
            removeKeySet.add(decisionValueEnd);
            attributeValues.remove(removeKeySet);

            //Continue the loop until there are no more attribute values to combine
            while(!attributeValues.isEmpty()) {
                valueMap.clear();
                valueMap.putAll(attributeValues);
                iteration++;
                includeAttributes(iteration,writer);

                for(Map.Entry<HashSet<String>, HashSet<String>> entry : valueMap.entrySet()) {
                    if (decisionSetInit.containsAll(entry.getValue())) {
                        includeCertainRule(entry.getKey(), entry.getValue().size(), decisionValueStart);
                        attributeValues.remove(entry.getKey());
                    }else if(decisionSetTo.containsAll(entry.getValue())){
                        includeCertainRule(entry.getKey(), entry.getValue().size(), decisionValueEnd);
                        attributeValues.remove(entry.getKey());
                    }else {
                        includePossibleRule(entry.getValue(), entry.getKey());
                    }
                }

                includeRules(writer);
                combineAttributes(iteration);
            }
            writer.close();
        } catch (IOException error) {
            System.out.println(error.getStackTrace());

        }
    }


    private void combineAttributes(int iteration) {

        HashMap<HashSet<String>, HashSet<String>> updatedAttributeValues = new HashMap<>();
        HashMap<HashSet<String>, HashSet<String>> oldAttributeValues = new HashMap<>();

        HashSet<String> outerKey;
        HashSet<String> outerValue;
        HashSet<String> newKey;
        HashSet<String> commonRows;

        updatedAttributeValues.putAll(attributeValues);

        oldAttributeValues.putAll(attributeValues);

        for(Map.Entry<HashSet<String>, HashSet<String>> entry : attributeValues.entrySet()) {
            outerKey = entry.getKey();
            outerValue = entry.getValue();

            updatedAttributeValues.remove(entry.getKey());
            oldAttributeValues.remove(entry.getKey());

            for(Map.Entry<HashSet<String>, HashSet<String>> entryRemain : oldAttributeValues.entrySet()) {
                newKey = new HashSet<>();
                newKey.addAll(outerKey);
                newKey.addAll(entryRemain.getKey());

                if(updatedAttributeValues.containsKey(newKey))
                    continue;

                if(newKey.size() != (iteration+1))
                    continue;

                commonRows= calculateIntersection(outerValue, entryRemain.getValue());

                if(commonRows.size() > 0) {
                    boolean isASubset = checkIfSubsetOfCertainRules(newKey, certainRules);
                    if(!isASubset) {
                        updatedAttributeValues.put(newKey, commonRows);
                    }
                }
            }
        }

        attributeValues = updatedAttributeValues;
    }

    //check if a set is a subset of existing certain rule to stop from continuing
    private boolean checkIfSubsetOfCertainRules(HashSet<String> newSetKey, Map<String, HashSet<HashSet<String>>> certainRules) {
        boolean isAlreadySubset=false;
        for(Map.Entry<String, HashSet<HashSet<String>>> ruleSets : certainRules.entrySet()) {
            Iterator<HashSet<String>> setsIterator = ruleSets.getValue().iterator();

            while(setsIterator.hasNext()) {
                if(newSetKey.containsAll(setsIterator.next()))
                    isAlreadySubset = true;
            }
        }
        return isAlreadySubset;
    }

    private HashSet<String> calculateIntersection(HashSet<String> firstSetValue, HashSet<String> value) {
        Iterator<String> iterator=  firstSetValue.iterator();
        HashSet<String> commonRows= new HashSet<>();
        String temp;
        while(iterator.hasNext()) {
            temp = iterator.next();
            if(value.contains(temp)) {
                commonRows.add(temp);
            }
        }
        return commonRows;
    }

    private void includeAttributes(int iteration, BufferedWriter writer ) throws IOException {
        StringBuilder sb= new StringBuilder();
        sb.append("\n");
        sb.append("IterationNumber");
        sb.append(iteration);
        sb.append("\n");
        sb.append("Sets:");
        sb.append("\n");

        for(Map.Entry<HashSet<String>, HashSet<String>> entry : attributeValues.entrySet()) {
            sb.append(entry.getKey().toString());
            sb.append(":");
            sb.append(entry.getValue().toString());
            sb.append("\n");
        }
        sb.append("[");
        sb.append(decisionValueStart);
        sb.append("]: ");
        sb.append(decisionSetInit.toString());
        sb.append("\n");
        sb.append("[");
        sb.append(decisionValueEnd);
        sb.append("]: ");
        sb.append("\n");
        System.out.println(sb.toString());
        writer.write(sb.toString());

    }

    public Map<String, HashSet<HashSet<String>>> getCertainRules(){
        return certainRules;
    }


    private void includeRules(BufferedWriter writer) throws IOException {
        StringBuilder sb= new StringBuilder();
        sb.append("\n");
        sb.append("CERTAIN RULES :");
        sb.append("\n");


        HashSet<String> setOfValues;

        for(Map.Entry<String, HashSet<HashSet<String>>> entry : certainRules.entrySet()) {
            Iterator<HashSet<String>> valueIterator= entry.getValue().iterator();

            while(valueIterator.hasNext()) {
                setOfValues = new HashSet<>();
                setOfValues.addAll(valueIterator.next());
                sb.append(setOfValues.toString());
                sb.append(" --> ");
                sb.append(entry.getKey());
                setOfValues.add(entry.getKey());
                sb.append("  SUPPORT: ");
                sb.append(supportRules.get(setOfValues).toString());
                sb.append("  CONFIDENCE: 100%");
                sb.append("\n");
            }
        }

        sb.append("\n");
        sb.append("POSSIBLE RULES :");
        sb.append("\n");

        for(int i = 0; i<possibleRules.size(); i++) {
            sb.append(possibleRules.get(i));
        }

        writer.write(sb.toString());
        possibleRules = new ArrayList<>();
    }


    private void includeCertainRule(HashSet<String> value, int support, String key) {
        HashSet<HashSet<String>> tempSet;
        HashSet<String> supportSet = new HashSet<>();

        if(certainRules.containsKey(key)) {
            tempSet = certainRules.get(key);
            tempSet.add(value);
            certainRules.put(key, tempSet);
        }else{
            tempSet = new HashSet<>();
            tempSet.add(value);
            certainRules.put(key, tempSet);
        }

        supportSet.addAll(value);
        supportSet.add(key);
        supportRules.put(supportSet, support);
    }


    private void includePossibleRule(HashSet<String> value, HashSet<String> key) {
        int startValueSupport = 0;
        int endValueSupport = 0;
        float confidence;
        StringBuilder sb= new StringBuilder();
        NumberFormat formatter = new DecimalFormat("#0.00");

        String[] valueArray=value.toArray(new String[value.size()]);
        for(String currOccurence : valueArray) {
            if(decisionSetInit.contains(currOccurence)) {
                startValueSupport++;
            }else if(decisionSetTo.contains(currOccurence)) {
                endValueSupport++;
            }
        }

        if(startValueSupport > 0) {
            String[] keyString = key.toArray(new String[key.size()]);
            sb.append(keyString[0]);
            for(int i = 1; i < keyString.length; i++) {
                sb.append("^");
                sb.append(keyString[i]);

            }
            sb.append(" --> ");
            sb.append(decisionValueStart);
            sb.append( "  SUPPORT:");
            sb.append( startValueSupport);

            confidence = ((float)startValueSupport/value.size() * 100);
            sb.append(" CONFIDENCE:" );
            sb.append(formatter.format((confidence)));
            sb.append("%");
            sb.append("\n");
            possibleRules.add(sb.toString());
        }

        sb= new StringBuilder();
        if(endValueSupport > 0) {
            String[] keyString = key.toArray(new String[key.size()]);
            sb.append(keyString[0]);
            for(int i = 1; i < keyString.length; i++) {
                sb.append("^");
                sb.append(keyString[i]);
            }
            sb.append(" --> ");
            sb.append(decisionValueEnd);
            sb.append(" SUPPORT:" );
            sb.append(endValueSupport);

            confidence = ((float)endValueSupport/value.size() * 100);
            sb.append(" CONFIDENCE:");
            sb.append(formatter.format((confidence)));
            sb.append("%");
            sb.append("\n");
            possibleRules.add(sb.toString());
        }
    }

}
