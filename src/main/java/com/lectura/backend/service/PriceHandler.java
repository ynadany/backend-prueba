package com.lectura.backend.service;

import com.lectura.backend.entity.Price;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PriceHandler extends DefaultHandler {
    private static final String PRICE_AMOUNT = "PriceAmount";
    private static final String CURRENCY_CODE = "CurrencyCode";
    private static final String COUNTRIES_INCLUDED = "CountriesIncluded";
    private static final String PRICE_DATE_ROLE = "PriceDateRole";
    private static final String PRICE_DATE = "Date";
    private static final String PRICE_TYPE = "PriceType";

    private Price price;
    private StringBuilder elementValue;

    @Override
    public void characters(char[] ch, int start, int length) {
        if (elementValue == null) {
            elementValue = new StringBuilder();
        } else {
            elementValue.append(ch, start, length);
        }
    }

    @Override
    public void startDocument() {
        price = new Price();
    }

    @Override
    public void startElement(String uri, String lName, String qName, Attributes attr) {
        switch (qName) {
            case PRICE_AMOUNT:
            case CURRENCY_CODE:
            case COUNTRIES_INCLUDED:
            case PRICE_DATE_ROLE:
            case PRICE_DATE:
            case PRICE_TYPE:
                elementValue = new StringBuilder();
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case PRICE_AMOUNT:
                price.setPriceAmount(Double.parseDouble(elementValue.toString()));
                break;
            case CURRENCY_CODE:
                price.setCurrencyCode(elementValue.toString());
                break;
            case COUNTRIES_INCLUDED:
                price.setCountryCode(elementValue.toString());
                break;
            case PRICE_DATE_ROLE:
                price.setRole(Byte.parseByte(elementValue.toString()));
                break;
            case PRICE_DATE:
                price.setDate(Integer.parseInt(elementValue.toString()));
                break;
            case PRICE_TYPE:
                price.setType(elementValue.toString());
                break;
        }
    }

    public Price getPrice() {
        return price;
    }
}
