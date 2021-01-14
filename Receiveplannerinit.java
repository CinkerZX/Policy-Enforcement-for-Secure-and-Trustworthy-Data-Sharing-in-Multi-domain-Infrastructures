import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Receiveplannerinit {

    private static final String EXCHANGE_NAME = "dex01";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("user");
        factory.setPassword("mouse.cat.dog.wolf.tiger");
        factory.setHost("dex-01.lab.uvalight.net");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        String queueName = channel.queueDeclare().getQueue();

        //String bindingKey = "planner.init";
        String bindingKey = "planner.run";
        channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          if(delivery != null){
            System.out.println("Received start");
            Receiveplannerinit planner = new Receiveplannerinit();
            String message = "{\"src\":\"Planner_VMCA\", \"dst\":\"Auditor_OMC\", \"type\":\"DATA_REQUEST\", \"body\":{ \"auditorId\":\"Auditor_OMC\", \"details\":\"need_aut(parking1,omc,vmca,traffic_diversion)\"}}";
            try{
              planner.sendrequest(message);
            }
            catch(Exception s){
                  System.out.println(s);
            }
          }
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public static void sendrequest(String argv) throws Exception {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setUsername("user");
      factory.setPassword("mouse.cat.dog.wolf.tiger");
      factory.setHost("dex-01.lab.uvalight.net");
      factory.setPort(5672);
      factory.setVirtualHost("/");
      try (Connection connection = factory.newConnection();
           Channel channel = connection.createChannel()) {

          channel.exchangeDeclare(EXCHANGE_NAME, "topic");

          String routingKey_1 = "frontend.logs"; //send to the frontend
          String routingKey_2 = "auditor1.logs"; //send to the Auditor1

          //String message = "need_aut(parking1,omc,vmca,traffic_diversion)";

          channel.basicPublish(EXCHANGE_NAME, routingKey_1, null, argv.getBytes("UTF-8"));
          channel.basicPublish(EXCHANGE_NAME, routingKey_2, null, argv.getBytes("UTF-8"));
      }
    }
}
