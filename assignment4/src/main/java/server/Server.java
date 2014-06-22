package server;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.jms.*;

public class Server implements MessageListener {
    private static int ackMode;
    private static String messageQueueName;
    private static String messageBrokerUrl;

    private Session session;
    private boolean transacted = false;
    private MessageProducer replyProducer;

    static ArrayList<Stock> stocks = new ArrayList<Stock>();

    static {
	messageBrokerUrl = "tcp://localhost:61616";
	messageQueueName = "client.messages";
	ackMode = Session.AUTO_ACKNOWLEDGE;

	stocks.add(new Stock("BMW", 1000));
	stocks.add(new Stock("Audi", 2000));
	stocks.add(new Stock("Mercedes", 3000));
	stocks.add(new Stock("Porsche", 4000));

    }

    public Server() {
	this.setupMessageQueueConsumer();
    }

    private void setupMessageQueueConsumer() {
	ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
		messageBrokerUrl);
	Connection connection;
	try {
	    connection = connectionFactory.createConnection();
	    connection.start();
	    this.session = connection.createSession(this.transacted, ackMode);
	    Destination adminQueue = this.session.createQueue(messageQueueName);

	    // Setup a message producer to respond to messages from clients, we
	    // will get the destination
	    // to send to from the JMSReplyTo header field from a Message
	    this.replyProducer = this.session.createProducer(null);
	    this.replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

	    // Set up a consumer to consume messages off of the admin queue
	    MessageConsumer consumer = this.session.createConsumer(adminQueue);
	    consumer.setMessageListener(this);
	} catch (JMSException e) {
	    // Handle the exception appropriately
	}
    }

    public void onMessage(Message message) {
	Stock publishedStock = null;
	try {
	    ObjectMessage response = this.session.createObjectMessage();
	    if (message instanceof TextMessage) {
		TextMessage txtMsg = (TextMessage) message;
		String messageText = txtMsg.getText();
		for (Stock stock : stocks) {
		    if (stock.getName().equalsIgnoreCase(messageText)) {
			publishedStock = stock;
			System.out
				.println("Received update request for exsting stock "
					+ messageText);
		    }

		}
		
	    }
	    

	    // Set the correlation ID from the received message to be the
	    // correlation id of the response message
	    // this lets the client identify which message this is a response to
	    // if it has more than
	    // one outstanding message to the server
	    response.setJMSCorrelationID(message.getJMSCorrelationID());

	    // Send the response to the Destination specified by the JMSReplyTo
	    // field of the received message,
	    // this is presumably a temporary queue created by the client

	    if (publishedStock != null) {
		response.setObject(publishedStock);
		this.replyProducer.send(message.getJMSReplyTo(), response);
		System.out.println("Sent stock object in response");
	    }
	    else {
		System.out.println("Received request for unpublished stock");
	    }
	} catch (JMSException e) {
	    // Handle the exception appropriately
	}
    }

    public void serializeStocks(ArrayList<Stock> stocks) {
	try {
	    FileOutputStream fout = new FileOutputStream("stocks.ser");
	    ObjectOutputStream oos = new ObjectOutputStream(fout);
	    oos.writeObject(stocks);
	} catch (FileNotFoundException e) {
	    System.out.println("Stocks file not present!");
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public ArrayList<Stock> getStocksFromFile() {
	ArrayList<Stock> stocks = null;
	try {
	    FileInputStream fin = new FileInputStream("stocks.ser");
	    ObjectInputStream ois = new ObjectInputStream(fin);
	    stocks = (ArrayList<Stock>) ois.readObject();
	    ois.close();
	} catch (FileNotFoundException e) {
	    System.out.println("Stocks file not present!");
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return stocks;
    }

    public Timestamp getCurrentTimeStamp() {
	java.util.Date date = new java.util.Date();
	return new Timestamp(date.getTime());

    }

    public static void main(String[] args) {
	new Server();

    }

}
