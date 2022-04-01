package com.lectura.backend;

import com.lectura.backend.entity.Price;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wildfly.common.Assert;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.BadRequestException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BasicTests {
    @Test
    public void formatTest() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String formattedDateTime = currentDateTime.format(formatter);
        System.out.println("Formatted LocalDateTime : " + formattedDateTime);
    }

    @Test
    public void xpathXmlTest() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("publicacionesXml.xml").getFile());
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/publications//publication[(language='es' or languge='us') and formats/format/costs/cost[country='esp']]";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
    }

    @Test
    public void xpathOnixTest() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("publicacionesOnix.xml").getFile());
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "/ONIXMessage//Product[(DescriptiveDetail/Language/LanguageCode ='spa' or 'eng') and contains(PublishingDetail/SalesRights/Territory/CountriesIncluded/text(),' BO ') and contains(ProductSupply/Market/Territory/CountriesIncluded/text(), ' BO ')]";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        System.out.println("Quantity: " + nodeList.getLength());
//        System.out.println("XML: " + nodeToString((Element) nodeList.item(0)));

        var node = nodeList.item(0);
        node.normalize();
        String expression2 = "ProductSupply/SupplyDetail//Price[Territory/CountriesIncluded/text()='BO']";
        NodeList prices = (NodeList) xPath.compile(expression2).evaluate(node, XPathConstants.NODESET);
        System.out.println("Quantity: " + prices.getLength());
//        System.out.println("XML: " + nodeToString((Element) prices.item(0)));
    }

    private Document getDocument(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        return doc;
    }

    @Test
    public void TestXMLWeb() throws Exception {
        String s =
                "<p>" +
                        "  <media type='audio' id='au008093' rights='aaa' />" +
                        "    <title>title</title>" +
                        "  this is a test." +
                        "</p>";
        InputStream is = new ByteArrayInputStream(s.getBytes());

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse(is);

        Element rootElement = d.getDocumentElement();
        System.out.println(nodeToString(rootElement));
    }

    private static String nodeToString(Element node) throws Exception {
        StringWriter sw = new StringWriter();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));

        return sw.toString();
    }

    @Test
    public void convertTest() {
        double price = 3.99D;
        double exchangeRate = 6.96D;

        double finalPrice = price * exchangeRate;
        System.out.println("Final Price: " + finalPrice);
        double finalPriceRound = (double) Math.round(finalPrice * 100)/100;
        System.out.println("Final Price Rounded: " + finalPriceRound);
    }

    @Test
    public void integerPriceTest() {
        var price = new Price();
        price.setPriceAmount(56.89D);
        var result = price.getIntegerPriceAmount();
        System.out.println("Integer price: " + result);
        Assert.assertTrue(result == 5689);

        price.setPriceAmount(0.89D);
        result = price.getIntegerPriceAmount();
        System.out.println("Integer price: " + result);
        Assert.assertTrue(result == 89);

        price.setPriceAmount(1.8D);
        result = price.getIntegerPriceAmount();
        System.out.println("Integer price: " + result);
        Assert.assertTrue(result == 180);

        price.setPriceAmount(225.99D);
        result = price.getIntegerPriceAmount();
        System.out.println("Integer price: " + result);
        Assert.assertTrue(result == 22599);
    }

    @Test
    public void datetimeTest() {
        var datetime = LocalDateTime.parse("2022-04-01 13:41:31", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (datetime.plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Tiempo expirado de descarga.");
        }
    }
}
