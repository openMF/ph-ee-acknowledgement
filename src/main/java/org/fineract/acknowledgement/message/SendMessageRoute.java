package org.fineract.acknowledgement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.fineract.acknowledgement.zeebe.ZeebeVariables;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.fineract.acknowledgement.camel.config.CamelProperties.*;


@Component
public class SendMessageRoute extends RouteBuilder {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZeebeVariables zeebeVariables;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

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


    @Override
    public void configure() throws Exception {
        String id = "send-ack-message";
        from("direct:" + id)
                .id(id)
                .log(LoggingLevel.INFO, "Sending success for ${exchangeProperty." + PROVIDER_ID + "}")
                .setHeader(tenantId, constant(tenantIdValue))
                .setHeader(tenantAppKey, constant(tenantAppKeyValue))
                .process(exchange -> {
                    String mobile = exchange.getProperty(MOBILE_NUMBER).toString();
                    Long internalId = Long.parseLong(exchange.getProperty(INTERNAL_ID).toString());
                    String providerId = exchange.getProperty(PROVIDER_ID, String.class);

                    JSONObject request = new JSONObject();
                    JSONArray jArray = new JSONArray();
                    request.put("internalId", internalId);
                    request.put("mobileNumber", mobile);
                    request.put("message", exchange.getProperty(DELIVERY_MESSAGE));
                    request.put("providerId", providerId);
                    exchange.getIn().setBody(jArray.put(request).toString());
                })
                .log("${body}")
                .setHeader(Exchange.HTTP_METHOD, simple("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to(String.format("%s://%s:%d/sms/?bridgeEndpoint=true", protocol, address, port))
                .log(LoggingLevel.INFO, "Sending sms to message gateway completed");

    }
}


