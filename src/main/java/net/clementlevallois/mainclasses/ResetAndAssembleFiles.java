/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.clementlevallois.mainclasses;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author C. Levallois
 */
public class ResetAndAssembleFiles {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, MalformedURLException {

        String bookBuildDir = args[0];
        String assembledDocs = args[1];
        String bookChaptersDir = args[2];
        String bookIdArg = args[3];

        new ResetAndAssembleFiles().reset(bookBuildDir, assembledDocs);
        new ResetAndAssembleFiles().assemble(bookBuildDir, assembledDocs, bookChaptersDir, bookIdArg);
        new ResetAndAssembleFiles().indexOperations(bookBuildDir, assembledDocs, bookChaptersDir, bookIdArg);

    }

    private void reset(String bookBuildDir, String assembledDocs) throws IOException {
        System.out.println("deleting previous assembled-docs.adoc");

        Path path = Paths.get(bookBuildDir + "/subdir/" + assembledDocs);
        boolean result = Files.deleteIfExists(path);
        if (result) {
            System.out.println("previous version of \"assembled-docs.adoc\" is deleted");
        }

    }

    private void assemble(String bookBuildDir, String assembledDocs, String bookChaptersDir, String bookId) throws IOException {
        System.out.println("assembling the chapters");

        List<String> chaptersForBook = null;

        if (bookId.equals("fundamentals")) {
            //reading the list of chapters to include in chapters-fundamentals.txt
            chaptersForBook = Files.readAllLines(Paths.get(bookBuildDir + "/" + "chapters-fundamentals.txt"), Charset.forName("UTF-8"));
        }
        if (bookId.equals("advanced")) {
            //reading the list of chapters to include in chapters-fundamentals.txt
            chaptersForBook = Files.readAllLines(Paths.get(bookBuildDir + "/" + "chapters-advanced.txt"), Charset.forName("UTF-8"));
        }
        if (bookId.equals("datom")) {
            //reading the list of chapters to include in chapters-fundamentals.txt
            chaptersForBook = Files.readAllLines(Paths.get(bookBuildDir + "/" + "chapters-datom.txt"), Charset.forName("UTF-8"));
        }
        Path assembledDocsFullPath = Paths.get(bookBuildDir + "/subdir/" + assembledDocs);
        Files.createFile(assembledDocsFullPath);
        int chapterNumber = 1;

        for (String chapter : chaptersForBook) {

            Path chapterFullPath = Paths.get(bookChaptersDir + "/" + chapter);

            StringBuilder sb = new StringBuilder();

            List<String> lines = Files.readAllLines(chapterFullPath, Charset.forName("UTF-8"));

            boolean skipEndSection = false;
            boolean skipHeaderSection = true;
            boolean skipSlidesLines;
            boolean keepTitle;
            boolean keepPicForBook = false;

            for (String line : lines) {

                skipSlidesLines = line.startsWith("//ST");
                keepTitle = line.startsWith("= ");

                // deleting the main part of the title
                if (keepTitle) {
                    if (line.contains(":")) {
                        line = "= " + chapterNumber + ". " + StringUtils.capitalize(line.substring(line.indexOf(":") + 1, line.length()).trim());
                    } else {
                        line = "= " + chapterNumber + ". " + StringUtils.capitalize(line.substring(line.indexOf("=") + 1, line.length()).trim());
                    }
                    line = line + "\n";
                    chapterNumber++;
                }

                if (line.toLowerCase().startsWith("== the end")) {
                    skipEndSection = true;
                }

                if (line.contains("image::")) {
                    Set<String> imageBookFieldsParser = Utils.imageBookFieldsParser(line);
                    keepPicForBook = imageBookFieldsParser.contains("keep");
                }

                if (line.startsWith("==")) {
                    skipHeaderSection = false;
                }
                if ((!skipHeaderSection & !skipEndSection & !skipSlidesLines) | keepTitle | keepPicForBook) {
                    sb.append(line);
                    sb.append("\n");
                }
            }
            // this introduces a page break
            sb.append("\n<<<\n\n");

            try (BufferedWriter writer = Files.newBufferedWriter(assembledDocsFullPath, Charset.forName("UTF-8"), StandardOpenOption.APPEND)) {
                writer.write(sb.toString());
            }
        }
    }

    public void indexOperations(String bookBuildDir, String assembledDocs, String bookChaptersDir, String bookId) throws IOException {
        System.out.println("index operations");

        Path assembledDocsFullPath = Paths.get(bookBuildDir + "/subdir/" + assembledDocs);
        List<String> readAllLines = Files.readAllLines(assembledDocsFullPath, Charset.forName("UTF-8"));
        Map<String,String> indexEntriesDoubleBrackets = new HashMap();
        Map<String,String> indexEntriesTripleBrackets = new HashMap();
        readAllLines.parallelStream().forEach(line -> {
            if (!line.contains("(((")) {
                while (line.contains("((")) {
                    String term = line.substring(line.indexOf("((") + 2, line.indexOf("))"));
                    System.out.println("term identified with (( )) is: " + term);

                    indexEntriesDoubleBrackets.put(term,term);
                    line = line.substring(line.indexOf("))"), line.length());
                }
            } else {
                while (line.contains("(((")) {
                    String term = line.substring(line.indexOf("(((") + 3, line.indexOf(")))"));
                    System.out.println("term identified with ((( ))) is: " + term);
                    String ante = line.substring(0, line.indexOf("((("));
                    int indexStarLast  = ante.lastIndexOf("*");
                    if (indexStarLast > 0){
                        int indexStarFirst = ante.substring(0,ante.lastIndexOf("*")-1).lastIndexOf("*");
                        indexEntriesTripleBrackets.put(ante.substring(indexStarFirst+1, indexStarLast),term);
                    }
                    else{
                        indexEntriesTripleBrackets.put(term, term);
                    }
                    line = line.substring(line.indexOf(")))"), line.length());
                }

            }
        });

        readAllLines = Files.readAllLines(assembledDocsFullPath, Charset.forName("UTF-8"));

        StringBuilder sb = new StringBuilder();

        for (String line : readAllLines) {
            for (Map.Entry<String,String> indexEntryMap : indexEntriesDoubleBrackets.entrySet()) {
                String indexEntryKey = indexEntryMap.getKey();
                String indexEntryValue = indexEntryMap.getValue();
                if (line.toLowerCase().contains(indexEntryKey.toLowerCase())) {
                    // adding brakets (( ))  only to terms which don't have ones already
                    if (!line.toLowerCase().contains("((" + indexEntryKey.toLowerCase() + "))")) {
                        // dealing with indexing of terms appearing in capitalized forms and non capitalized
                        if (line.contains(StringUtils.capitalize(indexEntryKey))) {
                            line = line.replace(StringUtils.capitalize(indexEntryKey), StringUtils.capitalize(indexEntryKey) + " (((" + indexEntryKey + ")))");
                        } else if (line.contains(indexEntryKey)) {
                            line = line.replace(indexEntryKey, "((" + indexEntryKey + "))");
                        }
                        System.out.println("line with term (( )) put in index: " + line);
                    }
                }
            }
            for (Map.Entry<String,String> indexEntryMap : indexEntriesTripleBrackets.entrySet()) {
                String indexEntryKey = indexEntryMap.getKey();
                String indexEntryValue = indexEntryMap.getValue();
                if (line.toLowerCase().contains(indexEntryKey.toLowerCase())) {
                    // adding brakets ((( )))  only to terms which don't have ones already
                    if (!line.toLowerCase().contains("(((" + indexEntryValue.toLowerCase() + ")))")) {
                        // dealing with indexing of terms appearing in capitalized forms and non capitalized
                        if (line.contains(StringUtils.capitalize(indexEntryKey))) {
                            line = line.replace(StringUtils.capitalize(indexEntryKey), StringUtils.capitalize(indexEntryKey) + " (((" + indexEntryValue + ")))");
                        } else if (line.contains(indexEntryKey)) {
                            line = line.replace(indexEntryKey, indexEntryKey + " (((" + indexEntryValue + ")))");
                        }
                        System.out.println("line with term ((( ))) put in index: " + line);
                    }
                }
            }
            sb.append(line);
            sb.append("\n");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(assembledDocsFullPath, Charset.forName("UTF-8"), StandardOpenOption.CREATE)) {
            writer.write(sb.toString());
        }
    }

}
