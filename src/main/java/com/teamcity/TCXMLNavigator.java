package com.teamcity;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.w3c.dom.Document;


public class TCXMLNavigator {
    public static Document parseXMLString(String xmlContent) {
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(Charset.forName("UTF-8")));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            document.normalize();
        }

        return document;
    }
}
