package com.lectura.backend.service.impl;

import com.lectura.backend.entity.Publication;
import com.lectura.backend.repository.PublicationRepository;
import com.lectura.backend.service.ICantookService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@QuarkusTest
public class CantookServiceTest {

    @Inject
    ICantookService cantookService;

    @Inject
    PublicationRepository repository;

    @Test
    public void fullSynchronization() {
        var result = cantookService.fullSynchronization();
    }

    @Test
    public void deltaSynchronization() throws Exception {
        cantookService.deltaSynchronization(LocalDateTime.now().minusMinutes(60));
    }

    @Test
    public void getQueryParam() {
        var result = cantookService.getQueryParam("start=MTQyMDQ1MTQyNTAwMCBMSUJSNzI%3D", "start");
        assertEquals("MTQyMDQ1MTQyNTAwMCBMSUJSNzI=", result);

        result = cantookService.getQueryParam("from=2022-02-06T02%3A27%3A07%2B00%3A00&start=2&to=2022-02-07T07%3A27%3A17%2B00%3A00", "start");
        assertEquals("2", result);
    }

    @Test
    @Transactional
    public void getPublications() throws XPathExpressionException, ParserConfigurationException, IOException, TransformerException, SAXException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("tests2Onix.xml").getFile());
        InputStream body = new FileInputStream(file);
        var publications = cantookService.getPublications(body, false);
        cantookService.updatePublications(publications);
        //repository.persist(publications);
    }

    @Test
    @Transactional
    public void getPublications2() throws XPathExpressionException, ParserConfigurationException, IOException, TransformerException, SAXException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("publicacionesOnix.xml").getFile());
        InputStream body = new FileInputStream(file);
        var publications = cantookService.getPublications(body, true);
        repository.persist(publications);
    }

    @Test
    public void comparationsTest() {
        var status = "04";
        var countries = "AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW";
        var rigths = "AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW";

        var result = status.equals("04") && countries.contains("BO") && rigths.contains("BO") && countries.indexOf("BO")>0;

        var publication1 = new Publication();
        publication1.setPublishingStatus("04");
        publication1.setMarketPublishingStatus("04");
        publication1.setSalesRights("AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW");
        publication1.setMarketCountries("AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW");

        var publication2 = new Publication();
        publication2.setPublishingStatus("04");
        publication2.setMarketPublishingStatus("04");
        publication2.setSalesRights("AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW");
        publication2.setMarketCountries("AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW");

        var publication3 = new Publication();
        publication3.setPublishingStatus("04");
        publication3.setMarketPublishingStatus("04");
        publication3.setSalesRights("AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW");
        publication3.setMarketCountries("AE AF AG AI AN AO AQ AR AS AT AU AW BB BD BE BF BG BH BI BJ BL BM BN BO BR BS BT BV BW BZ CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CX CY CZ DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FR GA GB GD GH GL GM GN GQ GR GS GT GU GW GY HK HM HN HR HT HU ID IE IL IN IO IQ IR IS IT JM JO JP KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LK LR LS LT LU LY MA MF MG MH ML MM MN MO MP MR MS MT MU MV MW MX MY MZ NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY QA RO RW SA SB SC SD SE SG SH SI SK SL SN SO SR ST SV SY SZ TC TD TF TG TH TJ TK TL TM TN TO TT TV TW TZ UG UM US UY UZ VC VE VG VI VN VU WF WS YE ZA ZM ZW");

        result = "04".equals(publication1.getPublishingStatus()) && "04".equals(publication1.getMarketPublishingStatus()) &&
                publication1.getSalesRights().indexOf("BO") > 0 && publication1.getMarketPublishingStatus().indexOf("BO") > 0;

        var publications = Arrays.asList(publication1, publication2, publication3);

        var filtered = publications.stream().filter(p -> "04".equals(p.getPublishingStatus()) && "04".equals(p.getMarketPublishingStatus()) &&
                p.getSalesRights().indexOf("BO") > 0 && p.getMarketCountries().indexOf("BO") > 0).collect(Collectors.toList());
    }
}