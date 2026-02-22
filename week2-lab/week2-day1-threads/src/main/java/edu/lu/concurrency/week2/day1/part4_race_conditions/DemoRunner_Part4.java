package edu.lu.concurrency.week2.day1.part4_race_conditions;

public final class DemoRunner_Part4 {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Part 4 Runner ===");

        Demo19_RaceCounter_Baseline.main(new String[0]);
        System.out.println();

        Demo20_JoinDoesNotFixRace.main(new String[0]);
        System.out.println();

        Demo23_VolatileIsNotAtomic.main(new String[0]);
        System.out.println();

        Demo21_SynchronizedFix.main(new String[0]);
        System.out.println();

        Demo22_AtomicIntegerFix.main(new String[0]);
        System.out.println();

        System.out.println("[NOTE] Demo24 is interactive (jstack). Run it manually:");
        System.out.println("  java -cp .\\build\\classes\\java\\main edu.lu.concurrency.week2.day1.part4_race_conditions.Demo24_ThreadDump_Contention");
    }

    private DemoRunner_Part4() {}
}