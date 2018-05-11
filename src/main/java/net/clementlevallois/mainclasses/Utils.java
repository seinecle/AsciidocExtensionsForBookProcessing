/*
 * Copyright 2017 Clement Levallois
 * http://wwww.clementlevallois.net
 */
package net.clementlevallois.mainclasses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author LEVALLOIS
 */
public class Utils {

    public static Set<String> imageBookFieldsParser(String line) {
        
        if (line.contains("Structured vs unstructured")){
            System.out.println("stop");
        }

        Set<String> fields = new HashSet();
        String bookProperties = line.substring(line.indexOf("["), line.length() - 1);
        int indexBook = bookProperties.indexOf("book=\"");
        if (indexBook < 0) {
            return fields;
        }
        bookProperties = bookProperties.substring(indexBook, bookProperties.length());
        bookProperties = bookProperties.substring(bookProperties.indexOf("\""), bookProperties.length());
        List<String> fieldsList = Arrays.asList(bookProperties.split(";|,"));
        for (String field : fieldsList) {
            fields.add(field.replaceAll("\"", "").trim());
        }
        return fields;
    }

}
