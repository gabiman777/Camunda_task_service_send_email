package org.fidseguros.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.fidseguros.service.SendEmailService;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SendEmailServiceHandler implements JobHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SendEmailServiceHandler.class);

    // Process's Variables
    private static final String VARIABLE_CARD_CVC = "cardCVC";
    private static final String VARIABLE_CARD_EXPIRY = "cardExpiry";
    private static final String VARIABLE_CARD_NUMBER = "cardNumber";
    private static final String VARIABLE_AMOUNT = "amount";
    private static final String VARIABLE_REFERENCE = "reference";
    private static final String VARIABLE_CONFIRMATION = "confirmation";
    private static final String VARIABLE_EMAIL_CONTENT = "email_message_content";

    private final SendEmailService sendEmailService;

    // Constructor injection
    public SendEmailServiceHandler(SendEmailService sendEmailService) {
        this.sendEmailService = sendEmailService;
    }

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {

        final Map<String, Object> inputVariables = job.getVariablesAsMap();
        //final String emailMessageContent = "Mensaje de correo en DURO a mano."; //(String) inputVariables.get(VARIABLE_EMAIL_CONTENT);
        final String emailMessageContent = (String) inputVariables.get(VARIABLE_EMAIL_CONTENT);
        LOG.info("SendEmail. emailMessageContent: " + emailMessageContent);
        /*
        final String reference = (String) inputVariables.get(VARIABLE_REFERENCE);
        final Double amount = (Double) inputVariables.get(VARIABLE_AMOUNT);
        final String cardNumber = (String) inputVariables.get(VARIABLE_CARD_NUMBER);
        final String cardExpiry = (String) inputVariables.get(VARIABLE_CARD_EXPIRY);
        final String cardCVC = (String) inputVariables.get(VARIABLE_CARD_CVC);
        */

        //final String confirmation = sendEmailService.chargeCreditCard(reference, amount, cardNumber, cardExpiry, cardCVC);

        final String confirmation = sendEmailService.sendEmail(emailMessageContent);

        final Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put(VARIABLE_CONFIRMATION, confirmation);

        client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();

    }
}