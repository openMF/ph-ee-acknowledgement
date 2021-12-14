package org.fineract.acknowledgement;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("rest:GET:/")
                .to("direct:acknowledgement")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(constant("GET Good"));

        from("rest:POST:/")
                .to("direct:acknowledgement")
                .log(LoggingLevel.INFO, "POST Body: ${body}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(constant("All Post Good"));
    }
}
