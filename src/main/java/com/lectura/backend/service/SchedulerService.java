package com.lectura.backend.service;

import io.quarkus.scheduler.Scheduled;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class SchedulerService {
    private static final Logger logger = Logger.getLogger(SchedulerService.class);

    @Inject
    ICantookService cantookService;

    @Inject
    IWooCommerceService wooCommerceService;

    @Scheduled(cron = "0 15 00 * * ?")
    public void process() throws Exception {
        var dateTime = LocalDateTime.now();
        logger.info("Running Synchronization... " + dateTime);
        logger.info("Delta Datetime... " + dateTime.minusDays(1));
        //cantookService.deltaSynchronization(dateTime.minusDays(1));
        //wooCommerceService.synchronization();
    }
}
