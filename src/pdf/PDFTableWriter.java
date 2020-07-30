package pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import dialogs.Warning;
import item.ItemCounter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class PDFTableWriter {

    private String title;
    private ArrayList<ItemCounter> tableData;
    private String filename;

    public PDFTableWriter(String filename) {

        this.filename = filename;
        //String dest = "C:/Us ers/dsidler/Desktop/test.pdf";
        //this.filename = dest;

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTableData(ArrayList<ItemCounter> data) {
        this.tableData = data;
    }


    public void writeContent() {
        Document document = new Document();

        FileOutputStream outputFile = null;
        try {
            outputFile = new FileOutputStream(this.filename);
        }
        catch (FileNotFoundException e) {
            Warning.display("Datei \"" + this.filename +"\" konnte nicht geöffnet werden");
            return;
        }

        try {
            PdfWriter.getInstance(document,outputFile);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        document.open();


        addTitle(document);
        addTable(document);


        document.close();



    }

    public void addTitle(Document document) {

        document.addAuthor("FarmInventory");
        document.addTitle(this.title);
        Paragraph titleLine = new Paragraph(title, new Font(Font.FontFamily.HELVETICA, 18, Font.NORMAL));
        addEmptyLine(titleLine, 1);

        try {
            document.add(titleLine);
        } catch (DocumentException e) {
            e.printStackTrace();
        }


    }

    public void addTable(Document document) {
        PdfPTable table = new PdfPTable(new float[] {1,4,2,1,5});

        final String FONT = "resources/FreeSans.ttf";
        final String TEXT = "\u25A2";

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font textFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        BaseFont bf = null;

        try {
            bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            //e.printStackTrace();
            System.out.println("Error Creating Font");
        } catch (IOException e) {
            System.out.println("Error loading Font File");
            //e.printStackTrace();
        }
        Font f = new Font(bf, 12);


        PdfPCell c0 = new PdfPCell(new Phrase(""));
        c0.setBorder(Rectangle.BOTTOM);
        table.addCell(c0);

        PdfPCell c1 = new PdfPCell(new Phrase("Ware", headerFont));
        c1.setBorder(Rectangle.BOTTOM);
        //c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase("Menge", headerFont));
        c2.setBorder(Rectangle.BOTTOM);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c2);

        PdfPCell c3 = new PdfPCell(new Phrase(" ", headerFont));
        c3.setBorder(Rectangle.BOTTOM);
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c3);

        PdfPCell c4 = new PdfPCell(new Phrase("Einheit", headerFont));
        c4.setBorder(Rectangle.BOTTOM);
        //c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c4);
        table.setHeaderRows(1);

        for (ItemCounter itemCounter : tableData) {
            PdfPCell c0Insert = new PdfPCell(new Phrase(TEXT,f));
            c0Insert.setBorder(Rectangle.NO_BORDER);
            table.addCell(c0Insert);

            PdfPCell c1Insert = new PdfPCell(new Phrase(itemCounter.getItem().itemName().get(), f));
            c1Insert.setBorder(Rectangle.NO_BORDER);
            table.addCell(c1Insert);

            String amountText = itemCounter.getAmount().toString() ;
            PdfPCell c2Insert = new PdfPCell(new Phrase(amountText, f));
            c2Insert.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c2Insert.setBorder(Rectangle.NO_BORDER);
            table.addCell(c2Insert);

            PdfPCell c3Insert = new PdfPCell(new Phrase(" ", f));
            c3Insert.setBorder(Rectangle.NO_BORDER);
            table.addCell(c3Insert);


            String unitText = itemCounter.getItem().getPriceUnit();
            PdfPCell c4Insert = new PdfPCell(new Phrase(unitText, f));
            c4Insert.setBorder(Rectangle.NO_BORDER);
            table.addCell(c4Insert);
        }

        boolean b = false;
        for(PdfPRow r: table.getRows()) {
            for(PdfPCell c: r.getCells()) {
                c.setBackgroundColor(b ? BaseColor.LIGHT_GRAY : BaseColor.WHITE);
            }
            b = !b;
        }

        try {
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }





}
