package server;

import java.sql.Timestamp;
import java.io.Serializable;

import org.apache.activemq.Message;

public class Stock implements Serializable {

    private static final long serialVersionUID = -7623418515327418953L;
    
    private String name;
    private double price;
    private Timestamp timestamp;
    
    public Stock(String name, double price) {
	this.name = name;
	this.price = price;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public double getPrice() {
	return price;
    }

    public void setPrice(double price) {
	this.price = price;
    }

    public Timestamp getTimestamp() {
	return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
	this.timestamp = timestamp;
    }

    @Override
    public String toString() {
	return new StringBuffer("Stock: ").append(this.name)
		.append(" " + this.price).toString();
    }

}
