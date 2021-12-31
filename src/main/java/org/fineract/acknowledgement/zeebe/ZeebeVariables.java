package org.fineract.acknowledgement.zeebe;

import org.springframework.stereotype.Component;

@Component
public class ZeebeVariables {

    private ZeebeVariables() {
    }

    public static final String TRANSACTION_ID = "transactionId";
    public static final String DELIVERY_STATUS = "deliveryStatus";
    public static final String DELIVERY_MESSAGE = "deliveryMessage";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String MESSAGE_INTERNAL_ID = "internalId";
    public static final String MESSAGE_TO_SEND = "deliveryMessage";
    public static final String MESSAGE_PROVIDER_ID = "providerId";
    public static final String ACKNOWLEDGEMENT_MESSAGE = "acknowledgementMessage";
    public static final String ACKNOWLEDGEMENT_MESSAGE_VERIFIED = "acknowledgementMessageVerified";
}