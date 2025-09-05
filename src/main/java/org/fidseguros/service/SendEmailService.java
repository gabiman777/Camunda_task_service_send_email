package org.fidseguros.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SendEmailService {

    private JavaMailSender mailSender;
    private final static Logger LOG = LoggerFactory.getLogger(SendEmailService.class);

    // Constructor injection
    public SendEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

  /*  public void chargeCreditCard(){
        System.out.println("Charging Credit Card");
    }*/

    public String chargeCreditCard(final String reference,
                                   final Double amount,
                                   final String cardNumber,
                                   final String cardExpiryDate,
                                   final String cardCVC){

        System.out.println("Starting Transaction: " + reference);
        System.out.println("Card Number: " + cardNumber);
        System.out.println("Card Expiry Date: " + cardExpiryDate);
        System.out.println("Card CVC: " + cardCVC);
        System.out.println("Amount: " + amount);

        final String confirmation = String.valueOf(System.currentTimeMillis());
        System.out.println("Successful Transaction: " + confirmation);
        return confirmation;
    }

    public String sendEmail(final String emailMessageContent){
            LOG.info("SendEmail. Notificar a cliente");

            //TODO. capturar el email_cliente de las variables del proceso, pasar al iniciar el proceso
            String email_cliente = "gabperez.exito@gmail.com";

            if (email_cliente == null || email_cliente.isEmpty()) {
                LOG.error("No se proporcionó email_cliente en la tarea SendMail");
                throw new IllegalStateException("La variable email_cliente es requerida en la tarea SendMail");
            }

            // Send email
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email_cliente);
                message.setSubject("Notificación de DelmarTG - SendMail con tarea de servicio");
                message.setText(emailMessageContent);
                message.setFrom("gabperez.exito@gmail.com");
                mailSender.send(message);
                LOG.info("Correo enviado al cliente {} para la tarea SendMail", email_cliente);
            } catch (Exception e) {
                LOG.error("Error al enviar correo al cliente {}: {}", email_cliente, e.getMessage());
                throw new RuntimeException("Fallo al enviar correo", e);
            }

            String confirmation = String.valueOf(System.currentTimeMillis());
            LOG.info("Fin tarea Send-Mail. currentTimeMillis:" + confirmation);
            return confirmation;
        }
}