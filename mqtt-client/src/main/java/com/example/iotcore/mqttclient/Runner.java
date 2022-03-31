package com.example.iotcore;


import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;


@Slf4j
public class Runner {
    public static void main(String[] args) {
        int choice;
        Scanner sc = new Scanner(System.in);
        log.info("Enter your choice: \nsubscribe: 1\npublish: 2");
        choice = sc.nextInt();

        if (choice == 1)
            new Subscriber().run();
        else
            new Publisher().run();
    }
}
