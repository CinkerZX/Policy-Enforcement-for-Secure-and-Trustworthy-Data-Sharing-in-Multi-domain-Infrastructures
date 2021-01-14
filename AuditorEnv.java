import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.*;
import jason.asSyntax.Literal;
import java.util.logging.Logger;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.asSyntax.*;
import jason.architecture.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

// Need to read the policy, the pending request from the planner, and the
// environment conditions
public class AuditorEnv extends  jason.environment.Environment{
  Literal nnc = Literal.parseLiteral("~normal_condition");
  Literal nc  = Literal.parseLiteral("normal_condition");
  static boolean eventHappen = false;
  static String request = "";

  //Read messages from rabbitmq queue "sensor.event"
  private static final String EXCHANGE_NAME = "dex01";
  public static void receiveEnv() throws Exception {
      ConnectionFactory factory = new ConnectionFactory();
      //factory.setHost("localhost");
      factory.setUsername("user");
      factory.setPassword("mouse.cat.dog.wolf.tiger");
      factory.setHost("dex-01.lab.uvalight.net");
      factory.setPort(5672);
      factory.setVirtualHost("/");
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();

      channel.exchangeDeclare(EXCHANGE_NAME, "topic");
      String queueName = channel.queueDeclare().getQueue();

      String bindingKey = "sensor.event"; //receive from the Send.java
      channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        if(delivery != null){
          String message = new String(delivery.getBody(), "UTF-8");
          eventHappen = eventsubset(message);
          /*System.out.println("Received '" + eventHappen + "'");
          if (message){
            eventHappen = true;
          }
          else{
            eventHappen = false;
          }*/
        }
      };
      boolean autoAck = true;
      channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });
  }

  //get the event
  public static boolean eventsubset(String argv){
    String regex = "EVENT_EMERGENCY_ON"; //Escape character \\)
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(argv);
    if (matcher.find()){
      return true;
    }
    else{
      return false;
    }
  }

  //Read messages from rabbitmq queue "auditor1.logs"
  public static void receiveReq() throws Exception {
      ConnectionFactory factory = new ConnectionFactory();
      //factory.setHost("localhost");
      factory.setUsername("user");
      factory.setPassword("mouse.cat.dog.wolf.tiger");
      factory.setHost("dex-01.lab.uvalight.net");
      factory.setPort(5672);
      factory.setVirtualHost("/");
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();

      channel.exchangeDeclare(EXCHANGE_NAME, "topic");
      String queueName = channel.queueDeclare().getQueue();

      //String bindingKey = "auditor1.logs"; //receive from the Send.java
      String bindingKey = "auditor1.logs";
      channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          if(delivery != null){
            String message = new String(delivery.getBody(), "UTF-8");
            //System.out.println("Received '" + message + "'");
            request = requestsubset(message); // subset the request from message
          }
      };

      boolean autoAck = true;
      channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });
  }

  //get the needed request from received message from "Request.java"
  public static String requestsubset(String argv){
    String regex = "need_aut.*\\)"; //Escape character \\)
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(argv);
    if (matcher.find()){
      return(matcher.group(0));
    }
    else{
      return "";
    }
  }

  //send the result
  public static void send(String argv) throws Exception {
      ConnectionFactory factory = new ConnectionFactory();
      //factory.setHost("localhost");
      factory.setUsername("user");
      factory.setPassword("mouse.cat.dog.wolf.tiger");
      factory.setHost("dex-01.lab.uvalight.net");
      factory.setPort(5672);
      factory.setVirtualHost("/");
      try (Connection connection = factory.newConnection();
           Channel channel = connection.createChannel()) {
          channel.exchangeDeclare(EXCHANGE_NAME, "topic");

          String routingKey_1 = "frontend.logs"; //send to the frontend
          String routingKey_2 = "planner.logs"; //send to the Auditor1
          String message = argv;

          channel.basicPublish(EXCHANGE_NAME, routingKey_1, null, message.getBytes("UTF-8"));
          channel.basicPublish(EXCHANGE_NAME, routingKey_2, null, message.getBytes("UTF-8"));
        }
  }

  @Override
  public void init(String[] args) {
    addPercept(nc); //start
  }

  @Override
  public boolean executeAction(String ag, Structure act) {
      //System.out.println(ag+" is doing "+act);
      AuditorEnv auditorio = new AuditorEnv();
      if(act.getFunctor().equals("signature")){
        try{
          System.out.println("Signature has already sent.");
          auditorio.send("{\"src\":\"Auditor_OMC\", \"dst\":\"Planner_VMCA\", \"type\":\"DATA_AUTH\", \"body\":{"+ request +"}}");
          request = "";
          updateAgsPercepts();
        }
        catch(Exception s){
              System.out.println(s);
        }
      }
      else if(act.getFunctor().equals("rejection")){
        try{
          System.out.println("Rejection has already sent.");
          auditorio.send("{\"src\":\"Auditor_OMC\", \"dst\": \"Planner_VMCA\", \"type\":\"DATA_UNAUTH\", \"body\":{"+ request +"}}");
          request = "";
          updateAgsPercepts();
        }
        catch(Exception s){
              System.out.println(s);
        }
      }
      else{
        request = "";
        updateAgsPercepts();
      }
      informAgsEnvironmentChanged();
      return true;
    }

  public void updateAgsPercepts() {
    //clearPercepts();
    removePercept(nnc);
    removePercept(nc);

    AuditorEnv auditorio = new AuditorEnv();
    // Listen to the requests from rabbitmq
    try{
      auditorio.receiveReq();
      //System.out.println("******Listen to the requests*******");
    }
    catch(Exception e){
          System.out.println(e);
    }
    // Listen to the environment context from rabbitmq
    try{
      auditorio.receiveEnv();
    }
    catch(Exception s){
          System.out.println(s);
    }

    //The following one won't work
    try{
      auditorio.receiveReq();
    }
    catch(Exception s){
          System.out.println(s);
    }
    // Update belief about the pending request
    if (request.equals("")){
      //System.out.println("No request added.");
      try{ // wait for 2 second
        Thread.sleep(2000);
      }
      catch(InterruptedException ex){
        Thread.currentThread().interrupt();
      }
      updateAgsPercepts();
    }
    else{
      System.out.println("Received request "+request+".");
      try{
        auditorio.send("{\"src\":\"Auditor_OMC\",\"dst\": \"Planner_VMCA\",\"type\":\"REQUEST_RECEIVED\", \"body\":{"+ request +"}}");
      }
      catch(Exception s){
            System.out.println(s);
      }
      Literal pendingReq = Literal.parseLiteral(request);
      addPercept(pendingReq);
      /*try{ // wait for 2 second
        Thread.sleep(3000);
      }
      catch(InterruptedException ex){
        Thread.currentThread().interrupt();
      }*/
      // Update the belief about the environment
      if (eventHappen){
        addPercept(nnc);
        System.out.println("Received alarm.");
        //eventHappen = false;
      }
      else
      {
        addPercept(nc); //make the auditor run
        //System.out.println("Add normal conditions");
      }
      informAgsEnvironmentChanged();
      //System.out.println("Received request added");
    }
  }
}
