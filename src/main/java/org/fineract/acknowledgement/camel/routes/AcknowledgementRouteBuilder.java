package org.fineract.acknowledgement.camel.routes;

import io.zeebe.client.ZeebeClient;
import org.json.JSONObject;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class AcknowledgementRouteBuilder extends ErrorHandlerRouteBuilder {
    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${messagegateway.fineract-platform-tenantid}")
    private String fineractPlatformTenantId;

    @Value("${messagegateway.fineract-tenant-app-key}")
    private String fineractTenantAppKey;

    final Logger logger = LoggerFactory.getLogger(AcknowledgementRouteBuilder.class);

    @Override
    public void configure() {}

}
