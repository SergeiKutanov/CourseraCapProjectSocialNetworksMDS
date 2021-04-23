# Coursera Cap Project
## Minimum Dominating Set in a Graph
### Overview
In this part we’ll try to find out what’s the minimum set of people that need to publish a post so everyone int the network will see the message. This is a well-known NP-complete problem.
For a given social network find the minimum set of people to publish a post so all users of the social network will see the message.

### Data
The provided UCSD Facebook data.

### Algorithms, Data Structures
The data is laid out as a standard directed graph (although each person is mutually connected to the other one, so the graph can be considered undirected). As underlying data structure I selected adjacency list as it saves memory and still allows to answer the project question.

### Algorithm
Since this is a well-known NP-complete problem it will take exponential time to get the exact answer. But we can make use of a greedy approximation algorithm with a defined vertex selection condition.

1. Initiate a set of uncovered vertices U
2. Initiate empty set to hold the answer L
3. While U is not empty:
    1. Select a vertex S which will cover the most uncovered vertices
    2. Remove S and it’s neighbours from uncovered set U
    3. Add S to L
4. Return L

### Algorithm Analysis
The algorithm can be implemented in polynomial time in terms of |U| and |L|. Since the number of iteration of the loop at steps 3 is bounded by minimum value of |U| and |L| and we can implement the loop body to run in time O(|U|*|L|). The total running time would be O(|U|*|L|*min(|U|, |L|))

