package org.fineract.acknowledgement.zeebe;

import org.springframework.stereotype.Component;

@Component
public class ZeebeVariables {

    private ZeebeVariables() {
    }

    public static final String TRANSACTION_ID = "transactionId";
    public static final String DELIVERY_STATUS = "notification-request";
    public static final String DELIVERY_MESSAGE = "deliveryMessage";
}