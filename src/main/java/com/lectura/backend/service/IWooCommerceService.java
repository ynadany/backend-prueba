package com.lectura.backend.service;

import com.lectura.backend.model.OrderDto;
import com.lectura.backend.model.SimulateSaleResponse;

import javax.transaction.*;
import java.net.URI;

public interface IWooCommerceService {
    boolean synchronization();

    SimulateSaleResponse simulateSale(Long productId, Double price) throws Exception;

    String registerSale(OrderDto order) throws Exception;

    URI getDownloadUrl(String orderId, String uname) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException;
}
