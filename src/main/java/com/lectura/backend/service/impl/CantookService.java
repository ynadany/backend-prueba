package com.lectura.backend.service.impl;

import com.lectura.backend.entity.Migration;
import com.lectura.backend.entity.Publication;
import com.lectura.backend.entity.SynchronizationEnum;
import com.lectura.backend.model.CreateSalesRequest;
import com.lectura.backend.repository.PriceRepository;
import com.lectura.backend.repository.PublicationRepository;
import com.lectura.backend.repository.PublisherRepository;
import com.lectura.backend.service.CantookServiceAPI;
import com.lectura.backend.service.ICantookService;
import com.lectura.backend.service.PriceHandler;
import com.lectura.backend.service.PublicationHandler;
import com.lectura.backend.util.XmlUtils;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@ApplicationScoped
public class CantookService implements ICantookService {
    private static final Logger logger = Logger.getLogger(CantookService.class);
    private static boolean isProcessing = false;
    private static LocalDateTime deltaDateTime = null;

    private static final String xpathProducts = "/ONIXMessage//Product[PublishingDetail/PublishingStatus/text()='04' " +
            "and contains(PublishingDetail/SalesRights/Territory/CountriesIncluded/text(),'BO') " +
            "and contains(ProductSupply/Market/Territory/CountriesIncluded/text(), 'BO')]";
    private static final String xpathPrices = "ProductSupply/SupplyDetail//Price[Territory/CountriesIncluded/text()='BO']";
    private static final List<String> languages = List.of("spa", "eng");
    private static final int PAGES_TO_PROCESS = 70;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private static DocumentBuilder documentBuilder;
    private static XPath xPath = XPathFactory.newInstance().newXPath();
    private static PublicationHandler publicationHandler = new PublicationHandler();
    private static PriceHandler priceHandler = new PriceHandler();
    private static SAXParser saxParser;

    @Inject
    @RestClient
    CantookServiceAPI cantookServiceAPI;

    @Inject
    PublicationRepository repository;

    @Inject
    PriceRepository priceRepository;

    @Inject
    PublisherRepository publisherRepository;

    @Override
    public boolean fullSynchronization() {
        if (isProcessing) return true;

        try {
            int i = 0;
            boolean check = true;
            while (check) {
                logger.info("Process Full Synchro > Chunk: " + i++);
                check = processFullSync();
            }
        } finally {
            isProcessing = false;
        }
        return false;
    }

    @Transactional
    @TransactionConfiguration(timeout = 1000) // 15min
    public boolean processFullSync() {
        isProcessing = true;
        AtomicInteger migrated = new AtomicInteger(0);
        Multi.createFrom().emitter(e -> emitMultiFull(e))
                .onFailure().recoverWithCompletion()
                .subscribe().with((p) -> {
                            updatePublishers((List<Publication>) p);
                            repository.persist((List<Publication>) p);
                            logger.info("Publications migrated: " + ((List<?>) p).size());
                            migrated.incrementAndGet();
                        }, ex -> logger.error(ex.getMessage(), ex),
                        () -> logger.info("Completed pages: " + migrated.get()));
        return isProcessing;
    }

    @Override
    public boolean deltaSynchronization(LocalDateTime dateTime) throws Exception {
        if (isProcessing) return true;

        deltaDateTime = dateTime;
        try {
            int i = 0;
            boolean check = true;
            while (check) {
                logger.info("Process Delta Synchro > Chunk: " + i++);
                check = processDeltaSync();
            }
        } finally {
            deltaDateTime = null;
            isProcessing = false;
        }
        return false;
    }

    @Transactional
    @TransactionConfiguration(timeout = 1000) // 15min
    public boolean processDeltaSync() {
        isProcessing = true;
        AtomicInteger migrated = new AtomicInteger(0);
        Multi.createFrom().emitter(e -> emitMultiDelta(e))
                .onFailure().recoverWithCompletion()
                .subscribe().with((p) -> {
                            updatePublishers((List<Publication>) p);
                            updatePublications((List<Publication>) p);
                            logger.info("Publications migrated: " + ((List<?>) p).size());
                            migrated.incrementAndGet();
                        }, ex -> logger.error(ex.getMessage(), ex),
                        () -> logger.info("Completed pages: " + migrated.get()));
        return isProcessing;
    }

    @Override
    public void synchronize(String isbn) throws Exception {
        Response response = cantookServiceAPI.getPublication(isbn);
        var iStreamXml = response.readEntity(InputStream.class);

        var publications = getPublications(iStreamXml, false);
        updatePublications(publications);
    }

    @Override
    public boolean registrySale(CreateSalesRequest request) throws Exception {
        return false;
    }

    private void emitMultiFull(MultiEmitter<? super List<Publication>> emitter) {
        try {
            var migration = Migration.builder()
                    .dateTime(LocalDateTime.now())
                    .type(SynchronizationEnum.FULL)
                    .finished(false).build();

            int countPublications = 0;
            int countPages = 0;
            InputStream iStreamXml;
            Response response = null;
            List<Publication> publications;
            String startValue = null;
            Link link;
            var lastMigration = Migration.findLast(SynchronizationEnum.FULL);
            if (Objects.nonNull(lastMigration) && lastMigration.getId() > 0) {
                logger.info("Next Link >> Resume Link: " + lastMigration.getLastUrl());
                link = Link.valueOf(lastMigration.getLastUrl());
                startValue = getQueryParam(link.getUri().getRawQuery(), "start");
                lastMigration.setFinished(true);
            }
            do {
                try {
                    response = cantookServiceAPI.getFullPublications(startValue);
                    if (response.getStatus() == 200) {
                        if (Objects.nonNull(lastMigration) && !lastMigration.isPersistent()) {
                            lastMigration.persistAndFlush();
                        }
                        iStreamXml = response.readEntity(InputStream.class);
                        publications = getPublications(iStreamXml, true);
                        countPublications = countPublications + publications.size();
                        countPages = countPages + 1;
                        emitter.emit(publications);
                        link = response.getLink("next");
                        migration.setDateTime(LocalDateTime.now());
                        migration.setLastUrl(link.toString());
                        migration.setCount(countPublications);
                        migration.persistAndFlush();
                        logger.info("Next Link >> Query String: " + link.getUri().getRawQuery());
                        startValue = getQueryParam(link.getUri().getRawQuery(), "start");
                    }
                } catch (WebApplicationException ex) {
                    if (ex.getResponse().getStatus() == 404) {
                        logger.warn("Full Synchro Emitter finished: " + ex.getMessage());
                        migration.setFinished(true);
                        migration.persistAndFlush();
                        isProcessing = false;
                    } else {
                        throw ex;
                    }
                } catch (Exception ex) {
                    logger.warn("Full Synchro Emitter unexpected error: " + ex.getMessage(), ex);
                    throw ex;
                }
            } while (isProcessing && countPages < PAGES_TO_PROCESS);
            emitter.complete();
        } catch (Exception ex) {
            logger.error("Full Synchro Emitter Error, " + ex.getMessage(), ex);
            isProcessing = false;
            emitter.fail(ex);
        }
    }

    private void emitMultiDelta(MultiEmitter<? super List<Publication>> emitter) {
        try {
            var formattedDateTime = deltaDateTime.format(formatter);
            logger.info("Delta DateTime: " + formattedDateTime);

            var migration = Migration.builder()
                    .dateTime(LocalDateTime.now())
                    .type(SynchronizationEnum.DELTA)
                    .finished(false).build();

            int countPublications = 0;
            int countPages = 0;
            InputStream iStreamXml;
            Response response = null;
            List<Publication> publications;
            String startValue = null;
            String fromValue = formattedDateTime;
            String toValue = null;
            Link link;
            var lastMigration = Migration.findLast(SynchronizationEnum.DELTA);
            if (Objects.nonNull(lastMigration) && lastMigration.getId() > 0) {
                logger.info("Next Link >> Resume Link: " + lastMigration.getLastUrl());
                link = Link.valueOf(lastMigration.getLastUrl());
                startValue = getQueryParam(link.getUri().getRawQuery(), "start");
                fromValue = getQueryParam(link.getUri().getRawQuery(), "from");
                toValue = getQueryParam(link.getUri().getRawQuery(), "to");
                lastMigration.setFinished(true);
            }
            do {
                try {
                    response = cantookServiceAPI.getDeltaPublications(fromValue, startValue, toValue);
                    if (response.getStatus() == 200) {
                        if (Objects.nonNull(lastMigration) && !lastMigration.isPersistent()) {
                            lastMigration.persistAndFlush();
                        }
                        iStreamXml = response.readEntity(InputStream.class);
                        publications = getPublications(iStreamXml, true);
                        countPublications = countPublications + publications.size();
                        countPages = countPages + 1;
                        emitter.emit(publications);

                        link = response.getLink("next");
                        migration.setDateTime(LocalDateTime.now());
                        migration.setLastUrl(link.toString());
                        migration.setCount(countPublications);
                        migration.persistAndFlush();
                        logger.info("Next Link >> Query String: " + link.getUri().getRawQuery());
                        startValue = getQueryParam(link.getUri().getRawQuery(), "start");
                        fromValue = getQueryParam(link.getUri().getRawQuery(), "from");
                        toValue = getQueryParam(link.getUri().getRawQuery(), "to");
                    }
                } catch (WebApplicationException ex) {
                    if (ex.getResponse().getStatus() == 404) {
                        logger.warn("Delta Synchro Emitter finished: " + ex.getMessage());
                        migration.setFinished(true);
                        migration.persistAndFlush();
                        isProcessing = false;
                    } else {
                        throw ex;
                    }
                } catch (Exception ex) {
                    logger.warn("Delta Synchro Emitter unexpected error: " + ex.getMessage(), ex);
                    throw ex;
                }
            } while (isProcessing && countPages < PAGES_TO_PROCESS);
            emitter.complete();
        } catch (Exception ex) {
            logger.error("Delta Synchro Emitter Error, " + ex.getMessage(), ex);
            isProcessing = false;
            emitter.fail(ex);
        }
    }

    public void updatePublications(List<Publication> listPublications) {
        logger.debug("Total publications: " + listPublications.size());
        updatePublishers(listPublications);
        var noActivePublications = listPublications.stream()
                .filter(p -> (!"04".equals(p.getPublishingStatus()) || !"04".equals(p.getMarketPublishingStatus())))
                .map(p -> {
                    Publication pub = repository.findById(p.getId());
                    if (Objects.nonNull(pub)) {
                        pub.setPublishingStatus(p.getPublishingStatus());
                        pub.setMarketPublishingStatus(p.getMarketPublishingStatus());
                        pub.setUpdated(false);
                        return pub;
                    } else {
                        return (Publication) null;
                    }
                })
                .filter(p -> Objects.nonNull(p))
                .collect(Collectors.toList());
        logger.info("Total No Active publications: " + noActivePublications.size());
        if (noActivePublications.size() > 0) {
            repository.persist(noActivePublications);
        }

        var activePublications = listPublications.stream()
                .filter(p -> ("04".equals(p.getPublishingStatus()) && "04".equals(p.getMarketPublishingStatus()) &&
                        p.getSalesRights().contains("BO") && p.getMarketCountries().contains("BO")))
                .map(p -> {
                    var publicationDb = repository.findById(p.getId());
                    if (Objects.isNull(publicationDb)) {
                        return p;
                    } else {
                        publicationDb.getMedia().setFile(p.getMedia().getFile());
                        publicationDb.getMedia().setPath(p.getMedia().getPath());
                        publicationDb.getPrices()
                                .forEach(price -> priceRepository.delete(price));
                        publicationDb.setPrices(p.getPrices());
                        publicationDb.setPublisher(p.getPublisher());
                        publicationDb.setMarketDate(p.getMarketDate());
                        publicationDb.setPublishingStatus(p.getPublishingStatus());
                        publicationDb.setMarketPublishingStatus(p.getMarketPublishingStatus());
                        publicationDb.setPublishingDate(p.getPublishingDate());
                        publicationDb.setMarketCountries(p.getMarketCountries());
                        publicationDb.setSalesRights(p.getSalesRights());
                        publicationDb.setLanguage(p.getLanguage());
                        publicationDb.setAuthor(p.getAuthor());
                        publicationDb.setTextContent(p.getTextContent());
                        publicationDb.setTitle(p.getTitle());
                        publicationDb.setProductFormDetail(p.getProductFormDetail());
                        publicationDb.setUpdated(false);
                        return publicationDb;
                    }
                }).collect(Collectors.toList());
        logger.info("Total Active publications: " + activePublications.size());
        if (activePublications.size() > 0) {
            repository.persist(activePublications);
        }
    }

    public List<Publication> getPublications(InputStream inputStream, boolean filtering) throws ParserConfigurationException,
            SAXException, IOException, XPathExpressionException {
        var xmlDocument = getDocumentBuilder().parse(inputStream);
        var nodes = XmlUtils.asList(filtering ? (NodeList) xPath.compile(xpathProducts).evaluate(xmlDocument, XPathConstants.NODESET)
                : xmlDocument.getElementsByTagName("Product"));
        logger.info("Total node Products: " + nodes.size());

        List<Publication> publications = nodes.stream()
                .map(this::parsePublication)
                .filter(p -> languages.contains(p.getLanguage()))
                .collect(Collectors.toList());

        logger.info("Publications generated: " + publications.size());
        return publications;
    }

    private void updatePublishers(List<Publication> publications) {
        var publishers = publications.stream().map(p -> p.getPublisher()).distinct()
                .map(p -> {
                    var db = publisherRepository.findById(p.getId());
                    return Objects.isNull(db) ? p : db;
                }).collect(Collectors.toList());
        publisherRepository.persist(publishers);
        publications.forEach(p -> p.setPublisher(publishers.stream().filter(pu -> pu.getId().equals(p.getPublisher().getId())).findFirst().get()));
    }

    private Publication parsePublication(Node node) {
        try {
            node.normalize();
            var nodePrices = XmlUtils.asList((NodeList) xPath.compile(xpathPrices).evaluate(node, XPathConstants.NODESET));
            logger.debug("# Prices: " + nodePrices.size());

            AtomicReference<ByteArrayOutputStream> outputStream = new AtomicReference<>(new ByteArrayOutputStream());
            AtomicReference<DOMSource> xmlSource = new AtomicReference<>(new DOMSource(node));
            AtomicReference<StreamResult> outputTarget = new AtomicReference<>(new StreamResult(outputStream.get()));
            TransformerFactory.newInstance().newTransformer().transform(xmlSource.get(), outputTarget.get());
            InputStream is = new ByteArrayInputStream(outputStream.get().toByteArray());

            getSaxParser().parse(is, publicationHandler);
            var publication = publicationHandler.getPublication();


            publication.setPrices(new ArrayList<>());

            nodePrices.stream().forEach(n -> {
                try {
                    outputStream.set(new ByteArrayOutputStream());
                    xmlSource.set(new DOMSource(n));
                    outputTarget.set(new StreamResult(outputStream.get()));
                    TransformerFactory.newInstance().newTransformer().transform(xmlSource.get(), outputTarget.get());
                    InputStream isPrice = new ByteArrayInputStream(outputStream.get().toByteArray());
                    getSaxParser().parse(isPrice, priceHandler);
                    var price = priceHandler.getPrice();
                    price.setPublication(publication);
                    publication.getPrices().add(price);
                } catch (TransformerException ex) {
                    logger.error("Error on parsing Price: " + ex.getMessage(), ex);
                } catch (Exception ex) {
                    logger.error("Error on parsing Price: " + ex.getMessage(), ex);
                }
            });
            return publication;
        } catch (Exception ex) {
            logger.error("Error on parsing Publication. " + ex.getMessage(), ex);
        }
        return null;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (Objects.isNull(documentBuilder)) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        return documentBuilder;
    }

    private SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        if (Objects.isNull(saxParser)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser();
        }
        return saxParser;
    }
}
