from distanceMatrix import *
from math import exp
import sys
import random


class Solution:
    def __init__(self, list, cost):
        self.list = list
        self.cost = cost

    def get_list(self):
        return self.list

    def get_cost(self):
        return self.cost


class Problem:
    def __init__(self, big_matrix, city_list):
        self.small_matrix = createSmallMatrix(big_matrix, city_list)
        self.init_cost = self.init_solution().get_cost()

    def init_solution(self):
        total_distance = 0
        for city_n in range(len(self.small_matrix[0])):
            total_distance += distance(self.small_matrix, self.small_matrix[0][city_n % len(self.small_matrix[0])],
                                       self.small_matrix[0][(city_n + 1) % len(self.small_matrix[0])])
        return Solution(self.small_matrix[0], total_distance)

    def decay(self, temperature):
        return temperature * 0.8

    def n_iter(self, n_iter):
        return n_iter * 1.2

    def should_stop(self, temp):
        return temp < 1

    def init_temp(self):
        min_dist = float("inf")
        max_dist = 0
        for line in self.small_matrix[1]:
            for elem in line:
                if min_dist > elem:
                    min_dist = elem
                if max_dist < elem:
                    max_dist = elem
        return (max_dist - min_dist) * 2

    def get_neighbor(self, solution):
        list = solution.get_list()
        i = random.randint(0, len(list) - 1)
        j = random.randint(0, len(list) - 2)
        # Here we fix j to the appropriate place
        if i <= j:
            j += 1
        nice_list = list.copy()
        # Here we create a new list, that will start at i + 1, and will have the same "circular order"
        for a in range(0, (i + 1) % (len(nice_list))):
            nice_list.append(nice_list.pop(0))

        # Here we break up the list in the reversible part, and the rest
        j_list = nice_list[:(j - i) % (len(nice_list))]
        rest_list = nice_list[(j - i) % (len(nice_list)):]

        # Reverse the order of the list, from i+1 to j
        j_list.reverse()

        # Concat processed lists
        end_list = j_list + rest_list

        # Calculate variance
        delta = distance(self.small_matrix, list[i], list[j]) + \
                distance(self.small_matrix, list[(i + 1) % len(list)], list[(j + 1) % len(list)]) - \
                distance(self.small_matrix, list[i], list[(i + 1) % len(list)]) - \
                distance(self.small_matrix, list[j], list[(j + 1) % len(list)])

        return Solution(end_list, solution.get_cost() + delta)


class Solver:
    def __init__(self, problem):
        self.problem = problem
        self.initial_cost = problem.init_cost
        self.end_cost = problem.init_cost



    def solve(self, n_iter):
        current = self.problem.init_solution()
        best = current
        worst = current
        t = self.problem.init_temp()
        num_loops = 0
        while True:
            for n in range(int(n_iter)):
                num_loops += 1
                next = self.problem.get_neighbor(current)
                d = next.get_cost() - current.get_cost()
                if d < 0:
                    current = next
                else:
                    if next.get_cost() >= worst.get_cost():
                        worst = next
                if current.get_cost() < best.get_cost():
                    best = current
                    self.end_cost = best.get_cost()
                else:
                    if t < 0 and exp(-d / t) < random.random():
                        current = next
            if self.problem.should_stop(t):
                print("**************WORST SOLUTION***************")
                print(getInitials(worst.get_list()))
                print("Cost ", worst.get_cost())
                print("***********NUMBER OF ITERATIONS************")
                print(num_loops)
                print("***************BEST SOLUTION***************")
                return best
            n_iter = self.problem.n_iter(n_iter)
            t = self.problem.decay(t)


orig_matrix = readDistanceMatrix("distancias.txt")


orig_cities = []
cities = sys.argv[1]
for i in range(len(cities)):
    orig_cities.append(getCity(orig_matrix[0], cities[i]))

prob = Problem(orig_matrix, orig_cities)
print("***********INITIAL SOLUTION*************")
print(getInitials(orig_cities))

solv = Solver(prob)
print("Cost ", solv.initial_cost)
print(getInitials(solv.solve(1).get_list()))
print("Cost ", solv.end_cost)

# import itertools
# small_matrix = createSmallMatrix(orig_matrix,orig_cities)
# best = 1000000000
# for end_list in itertools.permutations(orig_cities[1:]):
#     total_distance = distance(small_matrix, orig_cities[0],end_list[0]) + distance(small_matrix,orig_cities[0],end_list[len(end_list)-1])
#     for city_n in range(len(end_list)-1):
#         total_distance += distance(small_matrix, end_list[city_n], end_list[(city_n + 1) ])
#     if total_distance<best:
#         best = total_distance
# print(best)
