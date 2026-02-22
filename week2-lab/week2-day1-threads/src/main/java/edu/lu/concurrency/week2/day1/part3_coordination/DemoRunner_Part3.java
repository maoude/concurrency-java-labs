package edu.lu.concurrency.week2.day1.part3_coordination;

import edu.lu.concurrency.week2.day1.common.Console;

public final class DemoRunner_Part3 {

    public static void main(String[] args) throws Exception {
        Console.hr("Week 2  Part 3: sleep() vs join()  RUN ALL DEMOS");

        Demo14_SleepVsJoinFailure.main(new String[0]);
        Demo15_JoinCorrectness.main(new String[0]);
        Demo16_SleepDoesNotGuaranteeOrder.main(new String[0]);
        Demo17_HappensBeforeJoin.main(new String[0]);
        Demo18_JoinTimeoutPitfall.main(new String[0]);

        Console.hr("DONE");
    }

    private DemoRunner_Part3() {}
}
