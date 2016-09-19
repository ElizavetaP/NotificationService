package com.elizaveta.task;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

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
        DateFormat dateformat = new SimpleDateFormat("yyyy.MM.dd'at'HH:mm:ss");
        Date time = null;
        try {
            time = dateformat.parse(stringDate);
        } catch (ParseException e) {
            System.out.println(e);
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
                        HttpGet httpGet = new HttpGet(extraParams + "?message=" + message);
                        CloseableHttpResponse httpResponse = null;
                        try {
                            httpResponse = httpClient.execute(httpGet);
                            System.out.println("GET Response Status:: "
                                    + httpResponse.getStatusLine().getStatusCode());
                            httpResponse.close();
                            httpClient.close();
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        break;
                    case MAIL:
                        final String username = "lizap@bk.ru";
                        final String password = "";

                        Properties props = System.getProperties();
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.starttls.enable", "true");
                        props.put("mail.smtp.host", "smtp.mail.ru");
                        props.put("mail.smtp.port", "465");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.socketFactory.fallback", "false");
                        System.out.println("ok before");
                        Session session = Session.getDefaultInstance(props,
                                new javax.mail.Authenticator() {
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(username, password);
                                    }
                                });

                        System.out.println("ok");
                        try {

                            Message outmessage = new MimeMessage(session);
                            outmessage.setFrom(new InternetAddress("lizap@bk.ru"));
                            outmessage.setRecipients(Message.RecipientType.TO,
                                    InternetAddress.parse("lizap@bk.ru"));
                            outmessage.setSubject("Testing Subject");
                            outmessage.setText("Dear Mail Crawler,"
                                    + "\n\n No spam to my email, please!");

                            Transport.send(outmessage);

                            System.out.println("Done");
                            System.out.println("Sent message successfully....");
                        } catch (MessagingException mex) {
                            mex.printStackTrace();
                        }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }, time.getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);

    }

}