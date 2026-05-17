package org.example;

import org.example.trip.TripController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class Main {
    public static void main (String[] args){
        //SpringApplication.run(Main.class, args);
        TripController trip = new TripController();
        trip.TripController();
    }
}

