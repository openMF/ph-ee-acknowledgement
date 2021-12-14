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

    @PostConstruct
    public void setupWorkers() {
        workerSendAckMessage();
        workerSetResponseNo();
        workerValidateResponse();
    }

    private void workerSendAckMessage(){
        final String sendAckWorkerName = "send-ack-message";
        zeebeClient.newWorker()
                .jobType(sendAckWorkerName)
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Exchange exchange = new DefaultExchange(camelContext);

                    // connect message gateway to send sms

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name(sendAckWorkerName)
                .maxJobsActive(workerMaxJobs)
                .open();
    }

    private void workerSetResponseNo(){
        final String sendAckWorkerName = "set-response-no";
        zeebeClient.newWorker()
                .jobType(sendAckWorkerName)
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Exchange exchange = new DefaultExchange(camelContext);

                    // map NO to payment id

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name(sendAckWorkerName)
                .maxJobsActive(workerMaxJobs)
                .open();
    }

    private void workerValidateResponse(){
        final String sendAckWorkerName = "validate-response";
        zeebeClient.newWorker()
                .jobType(sendAckWorkerName)
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Exchange exchange = new DefaultExchange(camelContext);

                    // validate response

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name(sendAckWorkerName)
                .maxJobsActive(workerMaxJobs)
                .open();
    }
}