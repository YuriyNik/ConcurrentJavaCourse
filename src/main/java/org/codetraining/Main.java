package org.codetraining;

public class Main {
    public static void main(String[] args) {
//        System.out.printf("Hello and welcome!");
        // Get the number of available CPU cores
        int cpuCount = Runtime.getRuntime().availableProcessors();
        System.out.printf("\nNumber of available CPUs: %d%n", cpuCount);
    }
}