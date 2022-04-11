package com.lectura.backend.service;

import com.lectura.backend.entity.Publication;
import com.lectura.backend.model.CreateSalesRequest;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface ICantookService {
    boolean fullSynchronization();

    boolean deltaSynchronization(LocalDateTime dateTime) throws Exception;

    void synchronize(String isbn) throws Exception;

    boolean registrySale(CreateSalesRequest request) throws Exception;

    List<Publication> getPublications(InputStream body, boolean filtering) throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException;

    void updatePublications(List<Publication> listPublications);

    default String getQueryParam(String fullQueryString, String name) {
        return Arrays.stream(fullQueryString.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(c -> c[0], c -> URLDecoder.decode(c[1], StandardCharsets.UTF_8)))
                .get(name);
    }
}
