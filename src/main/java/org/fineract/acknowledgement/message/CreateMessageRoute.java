package org.fineract.acknowledgement.message;


import io.zeebe.client.ZeebeClient;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.fineract.acknowledgement.template.TemplateConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.apache.camel.Exchange.CORRELATION_ID;
import static org.fineract.acknowledgement.camel.config.CamelProperties.*;
import static org.fineract.acknowledgement.zeebe.ZeebeVariables.MESSAGE_INTERNAL_ID;
import static org.fineract.acknowledgement.zeebe.ZeebeVariables.MESSAGE_TO_SEND;


@Component
public class CreateMessageRoute extends RouteBuilder {
    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private TemplateConfig templateConfig;

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure() throws Exception {
        String id = "create-ack-message";
        from("direct:" + id)
                .id(id)
                .log(LoggingLevel.INFO, "Creating message")
                .process(exchange -> {
                    StringWriter message = new StringWriter();
                    templateConfig.getVelocityContext().put(exchange.getProperty(AMOUNT, Object.class).toString(), exchange.getProperty(CORRELATION_ID));
                    templateConfig.getMessageTemplate().merge(templateConfig.getVelocityContext(), message);
                    exchange.setProperty(DELIVERY_MESSAGE, message);
                    Map<String, Object> newVariables = new HashMap<>();
                    newVariables.put(MESSAGE_TO_SEND, exchange.getProperty(DELIVERY_MESSAGE).toString());
                    newVariables.put(MESSAGE_INTERNAL_ID, exchange.getProperty(INTERNAL_ID).toString());
                    zeebeClient.newSetVariablesCommand(Long.parseLong(exchange.getProperty(INTERNAL_ID).toString()))
                            .variables(newVariables)
                            .send()
                            .join();
                })
                .log(LoggingLevel.INFO, "Creating message completed with message :${exchangeProperty." + DELIVERY_MESSAGE + "}");
    }

}


