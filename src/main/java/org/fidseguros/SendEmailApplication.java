package org.fidseguros;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.fidseguros.handler.SendEmailServiceHandler;
import org.fidseguros.handler.SaveDataServiceHandler;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SendEmailApplication {

    private static final Logger LOG = LoggerFactory.getLogger(SendEmailApplication.class);


    // Payment Application Details
    private static final int WORKERTIMEOUT = 10;
    private static final int WORKER_TIME_TO_LIVE = 10000;

    // Process Definition Details
    //private static final String BPMN_PROCESS_ID = "send-email";
    private static final String BPMN_PROCESS_ID = "Process_send_email_message_custom";
    //Job Type de tareas de servicio, que se ejecutan en forma automática
    private static final String SEND_EMAIL_JOB_TYPE = "SendEmailTask";
    private static final String SAVE_DATA_JOB_TYPE = "SaveDataTask";

    // Process Variables
    private static final String VARIABLE_CARD_CVC = "cardCVC";
    private static final String VARIABLE_CARD_EXPIRY = "cardExpiry";
    private static final String VARIABLE_CARD_NUMBER = "cardNumber";
    private static final String VARIABLE_AMOUNT = "amount";
    private static final String VARIABLE_REFERENCE = "reference";
    private static final String VARIABLE_EMAIL_CONTENT = "email_message_content";


    public static void main(String[] args) {

     // Cargar el archivo .env
        try{
            Dotenv dotenv = Dotenv.load(); // Busca .env en el directorio actual
            dotenv.entries().forEach(entry ->{
                System.setProperty(entry.getKey(), entry.getValue());
            });
        } catch (io.github.cdimascio.dotenv.DotenvException e){
            System.err.println("ADVERTENCIA: Archivo .env no encontrado o error al cargarlo. \" +\n" +
                    "                    \"Las variables de entorno deben estar configuradas manualmente o en otro lugar.");
        }

        // Start Spring Boot application to initialize beans
        ConfigurableApplicationContext context = SpringApplication.run(SendEmailApplication.class, args);

        // Get beans from Spring context
        SendEmailServiceHandler handlerSendMail = context.getBean(SendEmailServiceHandler.class);
        SaveDataServiceHandler handlerSaveData = context.getBean(SaveDataServiceHandler.class);

        final String zeebeClientId = System.getProperty("ZEEBE_CLIENT_ID");
        final String zeebeClientSecret = System.getProperty("ZEEBE_CLIENT_SECRET");
        final String zeebeAuthorizationServerUrl = System.getProperty("ZEEBE_AUTHORIZATION_SERVER_URL");
        final String zeebeTokenAudience = System.getProperty("ZEEBE_TOKEN_AUDIENCE");
        final String zeebeRestAddress = System.getProperty("ZEEBE_REST_ADDRESS");
        final String zeebeGrpcAddress = System.getProperty("ZEEBE_GRPC_ADDRESS");

      final OAuthCredentialsProvider credentialsProvider = new OAuthCredentialsProviderBuilder()
              .authorizationServerUrl(zeebeAuthorizationServerUrl)
              .audience(zeebeTokenAudience)
              .clientId(zeebeClientId)
              .clientSecret(zeebeClientSecret)
              .build();

     try(final ZeebeClient client = ZeebeClient.newClientBuilder()
              .grpcAddress(URI.create(zeebeGrpcAddress))
              .restAddress(URI.create(zeebeRestAddress))
              .credentialsProvider(credentialsProvider)
              .build()) {

         //System.out.println("Connected to: " + client.newTopologyRequest().send().join());

         // Build the Start Process Variables
         final Map<String, Object> variables = new HashMap<String, Object>();
         /*TODO. borrar
         variables.put(VARIABLE_REFERENCE, "C8_12345");
         variables.put(VARIABLE_AMOUNT, Double.valueOf(800.00));
         variables.put(VARIABLE_CARD_NUMBER, "1234567812345678");
         variables.put(VARIABLE_CARD_EXPIRY, "12/2027");
         variables.put(VARIABLE_CARD_CVC, "123");
         */
         variables.put(VARIABLE_EMAIL_CONTENT, "Mensaje de correo enviado automáticamente desde proceso Send-Mail");

         // Launch The Process Instance
         client.newCreateInstanceCommand()
                 .bpmnProcessId(BPMN_PROCESS_ID)
                 .latestVersion()
                 .variables(variables)
                 .send()
                 .join();

         System.out.println("Process instance started for BPMN Process ID: " + BPMN_PROCESS_ID);

         // Start a Job Worker for job type: SEND_EMAIL_JOB_TYPE
         final JobWorker sendMailWorker =
                 client.newWorker()
                         .jobType(SEND_EMAIL_JOB_TYPE)
                         .handler(handlerSendMail)
                         .timeout(Duration.ofSeconds(WORKERTIMEOUT).toMillis())
                         .open();
         System.out.println("Worker opened for job type: " + SEND_EMAIL_JOB_TYPE + ". Application is now waiting for jobs asynchronously.");

         // Start a Job Worker for job type: SAVE_DATA_JOB_TYPE
         final JobWorker saveDataWorker =
                 client.newWorker()
                         .jobType(SAVE_DATA_JOB_TYPE)
                         .handler(handlerSaveData)
                         .timeout(Duration.ofSeconds(WORKERTIMEOUT).toMillis())
                         .open();
         System.out.println("Worker opened for job type: " + SAVE_DATA_JOB_TYPE + ". Application is now waiting for jobs asynchronously.");

         //Wait for the Workers
        //Thread.sleep(WORKER_TIME_TO_LIVE);
         // Add shutdown hook to close resources gracefully
         final ZeebeClient finalClient = client;
         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
             System.out.println("Shutting down...");
             if (sendMailWorker != null && !sendMailWorker.isClosed()) {
                 sendMailWorker.close();
             }
             if (saveDataWorker != null && !saveDataWorker.isClosed()) {
                 saveDataWorker.close();
             }
             if (finalClient != null) {
                 finalClient.close();
             }
         }));
         // Block main thread to keep the application running
         new CountDownLatch(1).await();

         } catch (Exception e){
            e.printStackTrace();
     };


    }
}