package org.movieHub._nextGenEdition._reincarnated._custom;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Mandela aka puumInc
 */
public abstract class Email extends Watchdog {

    /**
     * <strong>
     * When sending an email to someone using java, it will only work if you get an APP PASSWORD from your email provider
     * </strong>
     */
    private final String[] DEVELOPER_ADDRESSES = new String[]{"puumInc@outlook.com", "emandela60@gmail.com"};

    public boolean send_automatic_reply_to_user(String receiverAddress) {
        if (Arrays.stream(DEVELOPER_ADDRESSES).filter(receiverAddress::equalsIgnoreCase).findAny().orElse(null) != null) {
            return false;
        }
        @SuppressWarnings("SpellCheckingInspection") String DEVELOPER_PASSWORD = "mrwkcthfbtnujbhd";
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp-mail.outlook.com");
            properties.put("mail.smtp.port", 587);

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(DEVELOPER_ADDRESSES[0], DEVELOPER_PASSWORD);
                }
            });
            session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(DEVELOPER_ADDRESSES[0]));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverAddress));
            message.setSubject("Movie hub");
            message.setText("Hello, this is an automatic reply. Thank you for your response, i will get back to you soon.\n"
                    .concat("Regards\n")
                    .concat("<puum.inc()/>"));
            Transport.send(message);

            return true;
        } catch (MessagingException e) {
            if (e.getLocalizedMessage().contains("Couldn't connect to host, port")) {
                Platform.runLater(() -> error_message("Could not connect to the Internet!", "Please ensure you are connected to the internet t continue").show());
            } else {
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
            }
        }
        return false;
    }

    public void inform_developer(String receiverAddress, String text) {
        if (Arrays.stream(DEVELOPER_ADDRESSES).filter(receiverAddress::equalsIgnoreCase).findAny().orElse(null) != null) {
            return;
        }
        @SuppressWarnings("SpellCheckingInspection") String PASSWORD = "xirswwtvweuonbdg";

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("senderEmail", new Gson().toJsonTree(receiverAddress, String.class));
        jsonObject.add("message", new Gson().toJsonTree(text, String.class));

        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", 587);

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(DEVELOPER_ADDRESSES[1], PASSWORD);
                }
            });
            session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(DEVELOPER_ADDRESSES[1]));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(DEVELOPER_ADDRESSES[1]));
            message.setSubject("Moviehub@COMMENTS");
            message.setText(new Gson().toJson(jsonObject, JsonObject.class));
            Transport.send(message);

        } catch (MessagingException e) {
            if (e.getLocalizedMessage().contains("Couldn't connect to host, port")) {
                Platform.runLater(() -> error_message("Could not connect to the Internet!", "Please ensure you are connected to the internet t continue").show());
            } else {
                new Thread(write_stack_trace(e)).start();
                Platform.runLater(() -> programmer_error(e).show());
            }
        }
    }

}
