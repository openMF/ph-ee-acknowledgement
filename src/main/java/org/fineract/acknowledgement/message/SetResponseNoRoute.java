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


@Component
public class SetResponseNoRoute extends ErrorHandlerRouteBuilder {
    @Autowired
    private ZeebeClient zeebeClient;

    final Logger logger = LoggerFactory.getLogger(SetResponseNoRoute.class);

    @Override
    public void configure() {
        String routeId = "set-response-no";
        from("direct:" + routeId)
                .id(routeId)
                .process(exchange -> {
                    zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                            .variables(exchange.getAllProperties())
                            .send()
                            .join();
                });

    }

}
