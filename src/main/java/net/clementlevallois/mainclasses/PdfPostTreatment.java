/*
 * Copyright 2017 Clement Levallois
 * http://wwww.clementlevallois.net
 */
package net.clementlevallois.mainclasses;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author LEVALLOIS
 */
public class PdfPostTreatment {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws DocumentException, FileNotFoundException, IOException {
        
        String timeStamp = LocalDate.now().getDayOfYear()+ "-"+String.valueOf(Math.random()).substring(0, 5);
        String bookBuildDir = args[0];
        String collatedAdocsWithExtraPages = args[1].replace(".adoc", ".pdf");
        String bookId = args[2];
        String extension = "";
        if (bookId.equals("fundamentals")) {
            extension = "-1";
        }
        if (bookId.equals("advanced")) {
            extension = "-2";
        }
        if (bookId.equals("datom")) {
            extension = "-3";
        }

        Path pathCover = Paths.get(bookBuildDir + "/" + "cover" + extension + ".pdf");
        Path pathBlankPage = Paths.get(bookBuildDir + "/" + "blank-page.pdf");
        Path pathBackCover = Paths.get(bookBuildDir + "/" + "back-cover" + extension + ".pdf");
        Path pathFrontPages = Paths.get(bookBuildDir + "/" + "front-pages" + extension + ".pdf");
        Path pathLastPages = Paths.get(bookBuildDir + "/" + "last-pages" + extension + ".pdf");
        Path pathBook = Paths.get(bookBuildDir + "/" + collatedAdocsWithExtraPages);
        Path pathFinal = Paths.get(bookBuildDir + "/" + "entire-book-final-"+timeStamp+".pdf");

        PdfReader cover = new PdfReader(pathCover.toString());
        PdfReader backCover = new PdfReader(pathBackCover.toString());
        PdfReader book = new PdfReader(pathBook.toString());
        PdfReader frontPages = new PdfReader(pathFrontPages.toString());
        PdfReader lastPages = new PdfReader(pathLastPages.toString());
        PdfReader blankPage = new PdfReader(pathBlankPage.toString());
        
        int pagesNumber = book.getNumberOfPages();
        book.selectPages(IntStream.rangeClosed(2, pagesNumber).boxed().collect(Collectors.toList()));
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(pathFinal.toFile()));
        document.open();

        copy.addDocument(cover);
        copy.addDocument(blankPage);
        copy.addDocument(frontPages);
        copy.addDocument(book);

        System.out.println("page number so far: " + copy.getPageNumber());

        //inserting a blank page if the last page of the index is an even number
        if (copy.getPageNumber() % 2 == 0) {
            copy.addDocument(new PdfReader(pathBlankPage.toString()));
        }
        copy.addDocument(lastPages);
        copy.addDocument(new PdfReader(pathBlankPage.toString()));
        copy.addDocument(backCover);

        System.out.println("page number so far: " + copy.getPageNumber());
        document.close();
        cover.close();
        book.close();
    }
}
