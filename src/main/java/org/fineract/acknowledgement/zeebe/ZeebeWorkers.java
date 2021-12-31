package org.fineract.acknowledgement.zeebe;

import io.zeebe.client.ZeebeClient;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Map;

import static org.fineract.acknowledgement.camel.config.CamelProperties.*;
import static org.fineract.acknowledgement.camel.config.CamelProperties.DELIVERY_MESSAGE;
import static org.fineract.acknowledgement.zeebe.ZeebeVariables.*;

@Component
public class ZeebeWorkers {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @Value("${config.yes}")
    private String yes;

    @Value("${config.no}")
    private String no;

    @PostConstruct
    public void setupWorkers() {
        workerSendAckMessage();
        workerSetResponseNo();
        workerValidateResponse();
        workerGetMessageState();
        workerFetchResponse();
    }

    private void workerSendAckMessage() {
        final String sendAckWorkerName = "send-ack-message";
        zeebeClient.newWorker()
                .jobType(sendAckWorkerName)
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Exchange exchange = new DefaultExchange(camelContext);
                    Map<String, Object> variables = job.getVariablesAsMap();

                    exchange.setProperty(MOBILE_NUMBER, variables.get(PHONE_NUMBER));
                    exchange.setProperty(INTERNAL_ID, variables.get(MESSAGE_INTERNAL_ID));
                    exchange.setProperty(DELIVERY_MESSAGE, variables.get(MESSAGE_TO_SEND));
                    exchange.setProperty(PROVIDER_ID, variables.get(MESSAGE_PROVIDER_ID));

                    producerTemplate.send("direct:create-ack-message", exchange);
                    producerTemplate.send("direct:send-ack-message", exchange);

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name(sendAckWorkerName)
                .maxJobsActive(workerMaxJobs)
                .open();
    }

    private void workerGetMessageState() {
        final String getMessageState = "get-message-state";
        zeebeClient.newWorker()
                .jobType(getMessageState)
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

                    Exchange exchange = new DefaultExchange(camelContext);

                    producerTemplate.send("direct" + getMessageState, exchange);

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name(getMessageState)
                .maxJobsActive(workerMaxJobs)
                .open();
    }

    private void workerSetResponseNo() {
        final String sendAckWorkerName = "set-response-no";
        zeebeClient.newWorker()
                .jobType(sendAckWorkerName)
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

                    Map<String, Object> variables = job.getVariablesAsMap();
                    Exchange exchange = new DefaultExchange(camelContext);

                    exchange.setProperty(ACKNOWLEDGEMENT_MESSAGE, no);
                    exchange.setProperty(ACKNOWLEDGEMENT_MESSAGE_VERIFIED, true);
                    producerTemplate.send("direct:set-response-no", exchange);

                    zeebeClient.newSetVariablesCommand(job.getKey())
                            .variables(variables)
                            .send()
                            .join();

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name(sendAckWorkerName)
                .maxJobsActive(workerMaxJobs)
                .open();
    }

    private void workerValidateResponse() {
        final String sendAckWorkerName = "validate-response";
        zeebeClient.newWorker()
                .jobType(sendAckWorkerName)
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

                    Map<String, Object> variables = job.getVariablesAsMap();
                    if (variables.containsKey(ACKNOWLEDGEMENT_MESSAGE)) {
                        if (variables.get(ACKNOWLEDGEMENT_MESSAGE).toString().equals(yes) || variables.get(ACKNOWLEDGEMENT_MESSAGE).toString().equals(no)) {
                            variables.put(ACKNOWLEDGEMENT_MESSAGE_VERIFIED, true);
                        } else {
                            variables.put(ACKNOWLEDGEMENT_MESSAGE_VERIFIED, false);
                        }
                    }

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name(sendAckWorkerName)
                .maxJobsActive(workerMaxJobs)
                .open();
    }

    // TODO
    private void workerFetchResponse(){}
}