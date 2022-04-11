package com.lectura.backend.service;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//@QuarkusTest
public class CantookServiceAPITest {

    @Inject
    @RestClient
    private CantookServiceAPI myRemoteService;

    @Test
    public void getPublicationsTest() {
        //var result = myRemoteService.getPublications();
    }

    //@Test
    public void getPublicationsDeltaTest() throws Exception {
        LocalDateTime currentDateTime = LocalDateTime.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        String formattedDateTime = currentDateTime.format(formatter);
        Response result = myRemoteService.getDeltaPublications(formattedDateTime, null, null);
        String body = result.readEntity(String.class);
        var document = getDocument(body);

        var link = result.getLink("next");
    }

    private Document getDocument(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        return doc;
    }

    @Test
    public void getPublicationTest() {
        var result = myRemoteService.getPublication("9789500745772");
    }

    //@Test
    public void simulationSaleTest() {
        //format=epub&cost=6900&protection=acs4&country=BO&currency=BOB&price_type=41
        var isbn = "9788467059885";
        var format = "epub";
        var cost = 6900;
        var protection = "acs4";
        var country = "BO";
        var currency = "BOB";
        var priceType = "41";
        var result = myRemoteService.simulateSale(isbn, format, cost, protection, country, currency, priceType);
    }
}
