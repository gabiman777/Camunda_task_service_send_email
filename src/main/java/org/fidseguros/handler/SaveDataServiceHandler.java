package org.fidseguros.handler;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.fidseguros.service.SendEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SaveDataServiceHandler implements JobHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SaveDataServiceHandler.class);

    // Process's Variables
    private static final String VARIABLE1 = "email_message_content";
    private static final String VARIABLE_CONFIRMATION = "confirmation";

    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {

        final Map<String, Object> inputVariables = job.getVariablesAsMap();
        final String variable1 = (String) inputVariables.get(VARIABLE1);
        LOG.info("SaveData. VARIABLE1: " + variable1);

        final String confirmation = String.valueOf(System.currentTimeMillis());;
        final Map<String, Object> outputVariables = new HashMap<String, Object>();
        outputVariables.put(VARIABLE_CONFIRMATION, confirmation);

        client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();

    }
}