package org.example;

import org.example.config.JsonStorageConfig;
import org.example.menu.Menu;
import org.example.repository.TripRepository;
import org.example.trip.TripController;

//@SpringBootApplication
public class Main {
    public static void main (String[] args){
        //SpringApplication.run(Main.class, args);
//        TripController trip = new TripController();
//        trip.TripController();
        Menu menu = new Menu();
        menu.mainScreen();
    }
}

