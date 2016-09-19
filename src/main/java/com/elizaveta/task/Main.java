package com.elizaveta.task;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@WebServlet(urlPatterns = {"/"})
public class Main extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final String SMTP_HOST = "smtp.mail.ru";
    private static final String SMTP_PORT = "465";
    private static final String USER_NAME = "lizap@bk.ru";
    private static final String PASSWORD = "secret";
    ScheduledThreadPoolExecutor manager;

    public Main() {
        super();
        manager = new ScheduledThreadPoolExecutor(5);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String externalId = req.getParameter("externalId");
        String message = req.getParameter("message");
        String stringDate = req.getParameter("time");
        if (stringDate == null) {
            System.out.println("null");
            return;
        }
        DateFormat dateformat = new SimpleDateFormat("yyyy.MM.dd'at'HH:mm:ss");
        Date time = null;
        try {
            time = dateformat.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String extraParams = req.getParameter("extraParams");
        String stringType = req.getParameter("NotificationType");
        NotificationType type;
        if (stringType.toLowerCase().equals("mail")) {
            type = NotificationType.MAIL;
        } else {
            type = NotificationType.HTTP;
        }

        manager.schedule(() -> {
            try {
                switch (type) {
                    case HTTP:
                        CloseableHttpClient httpClient = HttpClients.createDefault();
                        HttpGet httpGet = new HttpGet("http://" + extraParams + "/?externalId="
                                + externalId + "&message=" + message);
                        CloseableHttpResponse httpResponse = null;
                        try {
                            httpResponse = httpClient.execute(httpGet);
                            System.out.println("GET Response Status:: "
                                    + httpResponse.getStatusLine().getStatusCode());
                            httpResponse.close();
                            httpClient.close();
                            LOG.info("command externalId = " + externalId + " is sent");
                        } catch (IOException e) {
                            e.printStackTrace();
                            LOG.warn("command externalId = " + externalId + " is not sent");
                        }
                        return;
                    case MAIL:

                        Properties props = System.getProperties();
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.starttls.enable", "true");
                        props.put("mail.smtp.host", SMTP_HOST);
                        props.put("mail.smtp.port", SMTP_PORT);
                        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.socketFactory.fallback", "false");
                        Session session = Session.getDefaultInstance(props,
                                new javax.mail.Authenticator() {
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(USER_NAME, PASSWORD);
                                    }
                                });
                        try {
                            Message outmessage = new MimeMessage(session);
                            outmessage.setFrom(new InternetAddress(USER_NAME));
                            outmessage.setRecipients(Message.RecipientType.TO,
                                    InternetAddress.parse(extraParams));
                            outmessage.setSubject("Testing Subject");
                            outmessage.setText("externalId = " + externalId
                                    + "\n message = " + message);
                            Transport.send(outmessage);
                            LOG.info("command externalId = " + externalId + " is sent");
                        } catch (MessagingException mex) {
                            mex.printStackTrace();
                            LOG.warn("command externalId = " + externalId + " is not sent");
                        }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }, time.getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);

    }

}
