/*
 * Copyright 2017 Clement Levallois
 * http://wwww.clementlevallois.net
 */
package net.clementlevallois.mainclasses;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LEVALLOIS
 */
public class FinalizeBookPages {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Adding title, preface... to the chapters");
        String bookBuildDir = args[0];
        String assembledDocsName = args[1];
        String assembledDocsNameWithExtraPages = args[2];

        Path assembledDocs = Paths.get(bookBuildDir + "/subdir/" + assembledDocsName);
        Path firstPages = Paths.get(bookBuildDir + "/" + "first-pages.adoc");
        Path lastPages = Paths.get(bookBuildDir + "/" + "last-pages.adoc");
        
        List<String> contentAssembledDocs = Files.readAllLines(assembledDocs);
        List<String> contentFirstPages = Files.readAllLines(firstPages);
        List<String> contentLastPages = Files.readAllLines(lastPages);
        List<String> entireBook = new ArrayList();
        entireBook.addAll(contentFirstPages);
        entireBook.addAll(contentAssembledDocs);
        entireBook.addAll(contentLastPages);
        
        
        Path out = Paths.get(bookBuildDir + "/subdir/" + assembledDocsNameWithExtraPages);
        Path writtenFile = Files.write(out,entireBook,Charset.forName("UTF-8"));
        System.out.println("entire book in adoc format written as a file here: "+writtenFile);
        
    }
    
}
