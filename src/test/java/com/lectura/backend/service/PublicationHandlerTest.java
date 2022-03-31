package com.lectura.backend.service;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PublicationHandlerTest {
    @Test
    public void parseXmlTest() throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        var publicationHandler = new PublicationHandler();
        ClassLoader classLoader = getClass().getClassLoader();
        saxParser.parse(classLoader.getResource("9788420418100.onix").getFile(), publicationHandler);

        var result = publicationHandler.getPublication();

        writeByte(result.getMedia().getFile());
    }

    private static void writeByte(byte[] bytes)
    {
        try {
            var file = new File("C:\\Users\\Ricky\\Pictures\\imageDB.jpg");
            // Initialize a pointer
            // in file using OutputStream
            OutputStream os = new FileOutputStream(file);

            // Starts writing the bytes in it
            os.write(bytes);
            System.out.println("Successfully"
                    + " byte inserted");

            // Close the file
            os.close();
        }
        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}