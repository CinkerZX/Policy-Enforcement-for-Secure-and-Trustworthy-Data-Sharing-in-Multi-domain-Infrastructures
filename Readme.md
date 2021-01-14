**This document describes the extension of the auditing layer, more concretely, the solution for realizing the communication between auditors, planner, and the police**

---

### Definition

> there are 3 classes of domains that need to communicate with each other.

- **Planner**: send the request to pointed auditors
- **Police**: send alarm (information about the environment) to the auditors
- **Auditor**: receive pending requests as well as the alarm, evaluate the requests, and then send signatures if they are compliant to the planner

### Related software:

- **Rabbitmq**: realize the communication between those three domains
- **Jason**: realize the function of auditors (receiving requests, reasoning, evaluating, and deciding)

### Aims and solutions:
> Here shows the aims followed by the related file. In the code block, the important methods/functions are listed.

- **Aim**: the auditors should be able to sense the environment,  receive requests from the outside, and save these inside their beliefs so as to evaluate.
- **Related file**: *AuditorEnv.java*

  ```java
  //Read messages from rabbitmq queue "auditorI"
  public static void receiveReq() throws Exception
  //get the needed request from received message from "Request.java"
  public static String requestsubset(String argv)
  //send the signature
  public static void send(String argv) throws Exception
  // update auditor's belief about the environment and the pending requests
  public void updateAgsPercepts()
  // Sending corresponding messages based on the auditor's decision
  public boolean executeAction(String ag, Structure act)
    ```

- **Aim**: send request
- **Related file**: *Receiveplannerinit.java*
  + By clicking the "run" button on Planner (http://dex-01.lab.uvalight.net:4200/), the request will be sent

- **Aim**: send alarm (information about the environment)
  + By clicking the "Activate Emergency"/"Deactivate Emergency" button on Sensor (http://dex-01.lab.uvalight.net:4200/), the environment will be changed

- **Aim**: send/receive signitures, alarm, requests
  + Realized by Routingkey and Bindingkey
```java
  // between police with auditor
  String bindingKey = "sensor.event";
  // initiate planner, let it send request
  String bindingKey = "planner.run";
  // send request to auditor
  String bindingKey = "auditor1.logs";
  // output request receive confirmation and siganture to the frontend
  String routingKey_1 = "frontend.logs";
  // output request receive confirmation and siganture to the Planner
  String routingKey_2 = "planner.logs";
```

  ```java
  // Receive signitures
  public class Recv
  ```

### Run
  ```
  javac -cp amqp-client-5.7.1.jar ReceiveLogsTopic.java
  java -cp .;amqp-client-5.7.1.jar;slf4j-api-1.7.26.jar;slf4j-simple-1.7.26.jar ReceiveLogsTopic
  ```

  > Those .jar need to be put in the \lib folder, so that when running jason, those packages can be loaded.

  > Only need to run "ReceiveLogsTopic" and Jason, requests and alarms are sent by UI.
