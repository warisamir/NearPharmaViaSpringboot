# Happy Meeting Days
## Problem Statement

Given an array of meeting time intervals where `intervals[i] = [start_i, end_i]`, determine the **minimum number of days** required to schedule all meetings such that no two meetings overlap on the same day.

Two meetings `[start_i, end_i]` and `[start_j, end_j]` **overlap** if they share any common time, i.e., `max(start_i, start_j) < min(end_i, end_j)`.

Your task is to find the minimum number of days needed so that all meetings can be scheduled without any conflicts.

## Examples

### Example 1:
```
Input: intervals = [[0,30],[5,10],[15,20]]
Output: 2
Explanation: 
Day 1: [0,30] (runs from 0 to 30)
Day 2: [5,10], [15,20] (both can fit on the same day since 10 ≤ 15)
We need at least 2 days because [0,30] overlaps with both [5,10] and [15,20].
```

### Example 2:
```
Input: intervals = [[7,10],[2,4]]
Output: 1
Explanation: 
Day 1: [2,4], [7,10] (both can fit on the same day since 4 ≤ 7)
Only 1 day is needed as the meetings don't overlap.
```

### Example 3:
```
Input: intervals = [[1,5],[8,9],[8,9]]
Output: 2
Explanation:
Day 1: [1,5], [8,9] (one of the [8,9] meetings)
Day 2: [8,9] (the other [8,9] meeting)
We need 2 days because both [8,9] meetings have the exact same time and cannot be on the same day.
```

### Example 4:
```
Input: intervals = [[1,4],[2,5],[3,6]]
Output: 3
Explanation:
All three meetings overlap with each other:
- [1,4] and [2,5] overlap at time [2,4]
- [2,5] and [3,6] overlap at time [3,5]  
- [1,4] and [3,6] overlap at time [3,4]
So each meeting needs its own day, requiring 3 days total.
```

### Example 5:
```
Input: intervals = [[1,2],[2,3],[3,4],[4,5]]
Output: 1
Explanation:
Day 1: [1,2], [2,3], [3,4], [4,5]
All meetings can be scheduled on the same day because they don't overlap.
Note: [1,2] and [2,3] don't overlap because they only touch at the boundary (time 2).
```

## Constraints

- `1 <= intervals.length <= 10^4`
- `intervals[i].length == 2`
- `0 <= start_i < end_i <= 10^6`
- All intervals are given in the format `[start_i, end_i]` where `start_i < end_i`

## Follow-up Questions

1. **Optimization**: Can you solve this problem in O(n log n) time complexity?
2. **Memory**: What's the minimum space complexity you can achieve?
3. **Real-world**: How would you modify this algorithm if meetings could be moved within a small time window (±k minutes)?
4. **Scale**: How would you handle this problem if you had millions of meetings across multiple time zones?


## Expected Time Complexity
O(n log n) where n is the number of intervals

## Expected Space Complexity
O(n) for additional data structures needed for the algorithm