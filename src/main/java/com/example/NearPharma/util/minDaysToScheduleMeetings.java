package com.example.NearPharma.util;
import java.util.*;
public class minDaysToScheduleMeetings {

        public static int minDaysToScheduleMeetings(int[][] intervals) {
            if (intervals == null || intervals.length == 0) return 0;

            int n = intervals.length;
            int[] st = new int[n];
            int[] ed = new int[n];//finding the starttiming
            // and endtiming of each interval
            for (int i = 0; i < n; i++) {
                st[i] = intervals[i][0];
                ed[i] = intervals[i][1];
            }
            //sort both the starting as well as ending days
            Arrays.sort(st);
            Arrays.sort(ed);
            int stPtr = 0, edPtr = 0;
            int AlreadyUsedDays = 0, maximumOfDaystaken = 0;
            //finding the number of days while traversing the array
            while (stPtr < n) {
                if (st[stPtr] < ed[edPtr]) {
                    AlreadyUsedDays++;  // if there  is collision then we
                    //  have to add a new day for that
                    stPtr++;
                } else {
                    AlreadyUsedDays--;  // otherwise meeting ended simply
                    edPtr++;
                }
                maximumOfDaystaken = Math.max(maximumOfDaystaken, AlreadyUsedDays);
            }
            return maximumOfDaystaken;
        }
        public static void main(String[] args) {
            int[][][] testCases = {
                    {{0,30}, {5,10}, {15,20}},
                    {{7,10}, {2,4}},
                    {{1,5}, {8,9}, {8,9}},
                    {{1,4}, {2,5}, {3,6}},
                    {{1,2}, {2,3}, {3,4}, {4,5}}
            };

            System.out.println("Running Given Test Cases:");
            for (int i = 0; i < testCases.length; i++) {
                int result = minDaysToScheduleMeetings(testCases[i]);
                System.out.println("Test case " + (i + 1) + ": " + result);
            }

            Scanner sc = new Scanner(System.in);
            System.out.print("\n Enter number of meetings: ");
            int n = sc.nextInt();

            int[][] intervals = new int[n][2];
            System.out.println("Enter start and end times for each meeting:");
            for (int i = 0; i < n; i++) {
                System.out.print("Meeting " + (i + 1) + ": ");
                intervals[i][0] = sc.nextInt();
                intervals[i][1] = sc.nextInt();
            }
            int result = minDaysToScheduleMeetings(intervals);
            System.out.println("Minimum number of days required: " + result);
        }
}
