package com.elizaveta.task;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/"})
public class Main extends HttpServlet {

    ScheduledThreadPoolExecutor manager;
    PriorityQueue<Command> queue;
    {
        CommandComparator comparator = new CommandComparator();
        queue = new PriorityQueue<>(comparator);
    }

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
        }catch (ParseException e){
            System.out.println(e);
        }
        String extraParams = req.getParameter("extraParams");
        String stringType = req.getParameter("NotificationType");
        NotificationType type;
        if(stringType.toLowerCase().equals("mail")){
            type = NotificationType.Mail;
        }else {
            type = NotificationType.Http;
        }

        Command comand = new Command(externalId,message,time,extraParams,type);
        queue.offer(comand);
        final Date time1 = time;
        System.out.println(""+time);
        System.out.println("" + (time1.getTime() - new Date().getTime()));
        manager.schedule(() -> {
            if(type.getClass().equals("Http")) {
                System.out.println("" + (time1.getTime() - new Date().getTime()));
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(extraParams + "?message=" + message);
                CloseableHttpResponse httpResponse = null;
                try {
                    httpResponse = httpClient.execute(httpGet);
                    System.out.println("GET Response Status:: "
                            + httpResponse.getStatusLine().getStatusCode());
                    httpClient.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }else {
                String hostname = "smtp.example.com";
                int port = 2525;
                String username = "nobody";
                String password = "idonttellyou";
                Mailer mailer = new Mailer(hostname, port, username, password);

                // Send mail.
                String from = "john.doe@example.com";
                String to = "jane.doe@example.com";
                String subject = "Interesting news";
                String message = "I've got JavaMail to work!";
                mailer.send(from, to, subject, message);
            }
        },time.getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);

    }

    }
