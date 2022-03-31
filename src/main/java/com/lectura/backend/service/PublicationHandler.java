package com.lectura.backend.service;

import com.lectura.backend.entity.Media;
import com.lectura.backend.entity.Publication;
import com.lectura.backend.entity.Publisher;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Objects;

public class PublicationHandler extends DefaultHandler {
    private static final String ID = "RecordReference";
    private static final String PRODUCT = "Product";
    private static final String ISBN = "ProductIdentifier";
    private static final String ID_VALUE = "IDValue";
    private static final String COLLECTION = "Collection";
    private static final String PRODUCT_FORM_DETAIL = "ProductFormDetail";
    private static final String TECHNICAL_PROTECTION = "EpubTechnicalProtection";
    private static final String TITLE = "TitleDetail";
    private static final String TITLE_TEXT = "TitleText";
    private static final String AUTHOR = "Contributor";
    private static final String CONTRIBUTOR_ROLE = "ContributorRole";
    private static final String PERSON_NAME = "PersonName";
    private static final String LANGUAGE = "Language";
    private static final String LANGUAGE_ROLE = "LanguageRole";
    private static final String LANGUAGE_CODE = "LanguageCode";
    private static final String TEXT_CONTENT = "TextContent";
    private static final String TEXT_TYPE = "TextType";
    private static final String TEXT = "Text";
    private static final String PUBLISHER_IDENTIFIER = "PublisherIdentifier";
    private static final String PUBLISHER_TYPE = "IDTypeName";
    private static final String PUBLISHER_NAME = "PublisherName";
    private static final String PUBLISHING_STATUS = "PublishingStatus";
    private static final String PUBLISHING_DATE = "PublishingDate";
    private static final String PUBLISHING_DATE_ROLE = "PublishingDateRole";
    private static final String DATE = "Date";
    private static final String SALES_RIGHTS = "SalesRights";
    private static final String COUNTRIES_INCLUDED = "CountriesIncluded";
    private static final String PRODUCT_SUPPLY = "ProductSupply";
    private static final String MARKET_PUBLISHING_STATUS = "MarketPublishingStatus";
    private static final String MARKET_DATE = "MarketDate";
    private static final String MARKET_DATE_ROLE = "MarketDateRole";
    private static final String SUPPORTING_RESOURCE = "SupportingResource";
    private static final String RESOURCE_CONTENT_TYPE = "ResourceContentType";
    private static final String FEATURE_VALUE = "FeatureValue";
    private static final String RESOURCE_LINK = "ResourceLink";
    private static final String SUBJECT = "Subject";
    private static final String SUBJECT_SCHEME = "SubjectSchemeIdentifier";
    private static final String SUBJECT_CODE = "SubjectCode";

    private String mainElement;
    private String pathXml;
    private boolean isMedia;

    private Publication publication;
    private Publisher publisher;
    private StringBuilder elementValue;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (elementValue == null) {
            elementValue = new StringBuilder();
        } else {
            elementValue.append(ch, start, length);
        }
    }

    @Override
    public void startDocument() {
        publication = new Publication();
        publisher = new Publisher();
    }

    @Override
    public void startElement(String uri, String lName, String qName, Attributes attr) throws SAXException {
        switch (qName) {
            case PRODUCT:
                mainElement = PRODUCT;
                break;
            case ID:
                if (Objects.nonNull(mainElement) && mainElement.equals(PRODUCT)) {
                    elementValue = new StringBuilder();
                }
                break;
            case ISBN:
                if (Objects.nonNull(mainElement) && mainElement.equals(PRODUCT))
                    pathXml = PRODUCT + "|" + ISBN;
                break;
            case ID_VALUE:
                if ((Objects.nonNull(mainElement) && mainElement.equals(PRODUCT) && (PRODUCT + "|" + ISBN).equals(pathXml)) ||
                        Objects.nonNull(mainElement) && mainElement.equals(PUBLISHER_IDENTIFIER))
                    elementValue = new StringBuilder();
                break;
            case TITLE_TEXT:
            case PERSON_NAME:
            case CONTRIBUTOR_ROLE:
            case LANGUAGE_CODE:
            case LANGUAGE_ROLE:
            case TEXT_TYPE:
            case TEXT:
            case PUBLISHING_DATE_ROLE:
            case DATE:
            case COUNTRIES_INCLUDED:
            case MARKET_DATE_ROLE:
            case RESOURCE_CONTENT_TYPE:
            case FEATURE_VALUE:
            case RESOURCE_LINK:
            case MARKET_PUBLISHING_STATUS:
            case SUBJECT_SCHEME:
            case SUBJECT_CODE:
            case PUBLISHER_TYPE:
                elementValue = new StringBuilder();
                break;
            case PRODUCT_FORM_DETAIL:
                mainElement = PRODUCT_FORM_DETAIL;
                elementValue = new StringBuilder();
                break;
            case COLLECTION:
                mainElement = COLLECTION;
                break;
            case TITLE:
                if (Objects.isNull(mainElement)) {
                    mainElement = TITLE;
                }
                break;
            case AUTHOR:
                mainElement = AUTHOR;
                break;
            case LANGUAGE:
                mainElement = LANGUAGE;
                break;
            case TEXT_CONTENT:
                mainElement = TEXT_CONTENT;
                break;
            case PUBLISHER_NAME:
                mainElement = PUBLISHER_NAME;
                elementValue = new StringBuilder();
                break;
            case PUBLISHING_STATUS:
                mainElement = PUBLISHING_STATUS;
                elementValue = new StringBuilder();
                break;
            case PUBLISHING_DATE:
                mainElement = PUBLISHING_DATE;
                break;
            case SALES_RIGHTS:
                mainElement = SALES_RIGHTS;
                break;
            case PRODUCT_SUPPLY:
                mainElement = PRODUCT_SUPPLY;
                break;
            case MARKET_DATE:
                if (Objects.nonNull(mainElement) && mainElement.equals(PRODUCT_SUPPLY))
                    pathXml = PRODUCT_SUPPLY + "|" + MARKET_DATE;
                break;
            case SUPPORTING_RESOURCE:
                mainElement = SUPPORTING_RESOURCE;
                isMedia = false;
                break;
            case SUBJECT:
                mainElement = SUBJECT;
                break;
            case PUBLISHER_IDENTIFIER:
                mainElement = PUBLISHER_IDENTIFIER;
                break;
            case TECHNICAL_PROTECTION:
                mainElement = TECHNICAL_PROTECTION;
                elementValue = new StringBuilder();
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!Objects.isNull(mainElement)) {
            switch (mainElement) {
                case PRODUCT:
                    if (qName.equals(ID)) {
                        publication.setId(elementValue.toString());
                    } else if (qName.equals(ID_VALUE) && (PRODUCT + "|" + ISBN).equals(pathXml)) {
                        publication.setIsbn(elementValue.toString());
                        mainElement = null;
                    }
                    break;
                case PRODUCT_FORM_DETAIL:
                    var value = Objects.isNull(publication.getProductFormDetail())
                            ? elementValue.toString() : publication.getProductFormDetail() + "|" + elementValue.toString();
                    publication.setProductFormDetail(value);
                    mainElement = null;
                    break;
                case TECHNICAL_PROTECTION:
                    publication.setTechnicalProtection(elementValue.toString());
                    mainElement = null;
                    break;
                case COLLECTION:
                    mainElement = null;
                    break;
                case TITLE:
                    if (qName.equals(TITLE_TEXT)) {
                        publication.setTitle(elementValue.toString());
                        mainElement = null;
                    }
                    break;
                case AUTHOR:
                    if (qName.equals(PERSON_NAME)) {
                        publication.setAuthor(elementValue.toString());
                        mainElement = null;
                    } else if (qName.equals(CONTRIBUTOR_ROLE) && !elementValue.toString().equals("A01")) {
                        mainElement = null;
                    }
                    break;
                case LANGUAGE:
                    if (qName.equals(LANGUAGE_CODE)) {
                        publication.setLanguage(elementValue.toString());
                        mainElement = null;
                    } else if (qName.equals(LANGUAGE_ROLE) && !elementValue.toString().equals("01")) {
                        mainElement = null;
                    }
                    break;
                case TEXT_CONTENT:
                    if (qName.equals(TEXT)) {
                        publication.setTextContent(elementValue.toString());
                        mainElement = null;
                    } else if (qName.equals(TEXT_TYPE) && !elementValue.toString().equals("03")) {
                        mainElement = null;
                    }
                    break;
                case PUBLISHER_NAME:
                    publisher.setName(elementValue.toString());
                    mainElement = null;
                    break;
                case PUBLISHING_STATUS:
                    publication.setPublishingStatus(elementValue.toString());
                    mainElement = null;
                    break;
                case PUBLISHING_DATE:
                    if (qName.equals(DATE)) {
                        publication.setPublishingDate(Integer.parseInt(elementValue.toString()));
                        mainElement = null;
                    } else if (qName.equals(PUBLISHING_DATE_ROLE) && !elementValue.toString().equals("01")) {
                        mainElement = null;
                    }
                    break;
                case SALES_RIGHTS:
                    if (qName.equals(COUNTRIES_INCLUDED)) {
                        publication.setSalesRights(elementValue.toString());
                        mainElement = null;
                    }
                    break;
                case PRODUCT_SUPPLY:
                    if (qName.equals(COUNTRIES_INCLUDED)) {
                        if (elementValue.toString().contains("BO")) {
                            publication.setMarketCountries(elementValue.toString());
                        } else {
                            mainElement = null;
                        }
                    } else if (qName.equals(MARKET_PUBLISHING_STATUS)) {
                        publication.setMarketPublishingStatus(elementValue.toString());
                    } else if ((PRODUCT_SUPPLY + "|" + MARKET_DATE).equals(pathXml)) {
                        if (qName.equals(DATE)) {
                            publication.setMarketDate(Integer.parseInt(elementValue.toString()));
                            mainElement = null;
                        } else if (qName.equals(MARKET_DATE_ROLE) && !elementValue.toString().equals("01")) {
                            pathXml = null;
                        }
                    }
                    break;
                case SUPPORTING_RESOURCE:
                    if (qName.equals(RESOURCE_CONTENT_TYPE) && !elementValue.toString().equals("01")) {
                        mainElement = null;
                    } else if (qName.equals(FEATURE_VALUE) && elementValue.toString().equals("front_cover_medium")) {
                        isMedia = true;
                    } else if (!isMedia && qName.equals(FEATURE_VALUE) && !elementValue.toString().equals("front_cover_medium")) {
                        mainElement = null;
                    } else if (qName.equals(RESOURCE_LINK) && elementValue.toString().startsWith("https://")) {
                        var media = new Media();
                        media.setPath(elementValue.toString());
                        media.setPublication(publication);
                        publication.setMedia(media);
                        mainElement = null;
                        isMedia = false;
                    }
                    break;
                case SUBJECT:
                    if (qName.equals(SUBJECT_CODE)) {
                        publication.setSubjectBicCode(Objects.isNull(publication.getSubjectBicCode())
                                ? elementValue.toString() : publication.getSubjectBicCode() + "|" + elementValue.toString());
                        mainElement = null;
                    } else if (qName.equals(SUBJECT_SCHEME) && !elementValue.toString().equals("12")) {
                        mainElement = null;
                    }
                    break;
                case PUBLISHER_IDENTIFIER:
                    if (qName.equals(ID_VALUE)) {
                        publisher.setId(elementValue.toString());
                        mainElement = null;
                    } else if (qName.equals(PUBLISHER_TYPE) && !elementValue.toString().equals("DM_GLOBAL_ID")) {
                        mainElement = null;
                    }
                    break;
            }
        }

        switch (qName) {
            case PRODUCT_FORM_DETAIL:
            case COLLECTION:
            case AUTHOR:
            case LANGUAGE:
            case PRODUCT_SUPPLY:
            case SUPPORTING_RESOURCE:
            case SUBJECT:
            case PUBLISHER_IDENTIFIER:
                mainElement = null;
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (Objects.nonNull(publisher) && publisher.getId().length() > 0) {
            publication.setPublisher(publisher);
        }
    }

    public Publication getPublication() {
        return publication;
    }
}
