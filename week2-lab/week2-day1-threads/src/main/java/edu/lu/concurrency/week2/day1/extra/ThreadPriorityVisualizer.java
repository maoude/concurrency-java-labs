package edu.lu.concurrency.week2.day1.extra;

public class ThreadPriorityVisualizer {

    private static String bar(int n) {
        return "|".repeat(Math.max(0, n));
    }

    public static void main(String[] args) {
        System.out.println("Low:    " + bar(3));
        System.out.println("Medium: " + bar(6));
        System.out.println("High:   " + bar(9));
        System.out.println("(Visualization only - not real scheduling.)");
    }
}
