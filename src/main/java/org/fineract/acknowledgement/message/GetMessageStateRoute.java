package org.fineract.acknowledgement.message;

import io.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.json.JSONArray;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.Exchange.CORRELATION_ID;
import static org.fineract.acknowledgement.camel.config.CamelProperties.INTERNAL_ID;
import static org.fineract.acknowledgement.zeebe.ZeebeVariables.ACKNOWLEDGEMENT_MESSAGE;
import static org.fineract.acknowledgement.zeebe.ZeebeVariables.ACKNOWLEDGEMENT_MESSAGE_VERIFIED;


@Component
public class GetMessageStateRoute extends ErrorHandlerRouteBuilder {
    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${messagegatewayconfig.protocol}")
    private String protocol;

    @Value("${messagegatewayconfig.host}")
    private String address;

    @Value("${messagegatewayconfig.port}")
    private int port;

    @Value("${fineractconfig.tenantid}")
    private String tenantId;

    @Value("${fineractconfig.tenantidvalue}")
    private String tenantIdValue;

    @Value("${fineractconfig.tenantappkey}")
    private String tenantAppKey;

    @Value("${fineractconfig.tenantappvalue}")
    private String tenantAppKeyValue;

    @Value("${config.no}")
    private String no;

    final Logger logger = LoggerFactory.getLogger(GetMessageStateRoute.class);

    @Override
    public void configure() {
        String routeId = "get-message-state";
        from("direct:" + routeId)
                .id(routeId)
                .choice()
                .when(exchange -> {
                    return Integer.parseInt(exchange.getProperty(INTERNAL_ID).toString()) < 3;
                })
                .setHeader(tenantId, constant(tenantIdValue))
                .setHeader(tenantAppKey, constant(tenantAppKeyValue))
                .setBody(exchange -> {
                    JSONArray request = new JSONArray();
                    Long internalId = Long.parseLong(exchange.getProperty(INTERNAL_ID).toString());
                    request.put(internalId);
                    return request.toString();
                })
                .log("${body}")
                .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to(String.format("%s://%s:%d/sms/report/?bridgeEndpoint=true", protocol, address, port))
                .log(LoggingLevel.INFO, "Delivery Status Endpoint Received")
                .process(exchange -> {
                    String id = exchange.getProperty(CORRELATION_ID, String.class);
                    Map<String, Object> variables = new HashMap<>();
                    String callback = exchange.getIn().getBody(String.class);
                    if (callback.contains("200")) {
                        logger.info("Still Pending");
                        exchange.setProperty(ACKNOWLEDGEMENT_MESSAGE, callback);
                    } else {
                        logger.info("Passed");
                        exchange.setProperty(ACKNOWLEDGEMENT_MESSAGE, "");

                    }
                    Map<String, Object> newVariables = new HashMap<>();
                    newVariables.put(ACKNOWLEDGEMENT_MESSAGE, exchange.getProperty(ACKNOWLEDGEMENT_MESSAGE));
                    zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                            .variables(newVariables)
                            .send()
                            .join();
                })
                .otherwise()
                .process(exchange -> {
                    exchange.setProperty(ACKNOWLEDGEMENT_MESSAGE, no);
                    exchange.setProperty(ACKNOWLEDGEMENT_MESSAGE_VERIFIED, true);
                })
                .to("direct:set-response-no");

    }

}
