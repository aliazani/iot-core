package com.example.iotcore.mqttclient;

import java.util.Scanner;


public class Runner {
    public static void main(String[] args) {
        int choice;
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your choice: \nsubscribe: 1\npublish: 2");
        choice = sc.nextInt();

        if (choice == 1)
            new Subscriber().run();
        else
            new Publisher().run();
    }
}
