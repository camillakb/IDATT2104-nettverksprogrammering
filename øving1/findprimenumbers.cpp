// Using multiple threads to find prime numbers in a given interval.
#include <iostream>
#include <thread>
#include <vector>
#include <mutex>
#include <algorithm>

using namespace std;
const int threadnum = 3;
const int from = 0;
const int to = 100;

//Method to check if a number is a prime number.
bool is_prime(int num) {
  if (num == 0 || num == 1) {
    return false;
  }

  for (int i = 2; i <= num / 2; ++i)  {
    if (num % i == 0) {
      return false;
    }
  }

  return true;
}

//Method to make a vector of numbers from the given interval.
vector<int> make_list() {
  vector<int> numbers;

  for (int i = from; i <= to; ++i) {
    numbers.push_back(i);
  }

  return numbers;
} 

//Method to split the vector of numbers between a given amount of vectors.
vector<vector<int>> split_numbers(vector<int> numbers) {
  vector<vector<int>> vectors;
  int thread = 0;

  for (int i = 0; i < threadnum; ++i) {
    vectors.push_back(vector<int>());
  }

  for (int number : numbers) {
    vectors[thread].push_back(number);
    ++thread;

    if (thread == threadnum) {
      thread = 0;
    }
  }

  return vectors;
}

int main() {
  auto numbers = make_list();
  auto number_pools = split_numbers(numbers);
  vector<int> primes;
  mutex primes_mutex;
  vector<thread> threads;

  //Find the prime numbers in each thread.
  for (int i = 0; i < threadnum; i++) {
    threads.emplace_back([i, &number_pools, &primes_mutex, &primes] {
      for (int number : number_pools[i]) {
        if (is_prime(number)) {
          primes_mutex.lock();
          primes.push_back(number);
          primes_mutex.unlock();
        }
      }
    });
  }

  //Join the threads.
  for (auto &thread : threads) {
    thread.join();
  }
    
  //Sort the prime numbers.
  sort(primes.begin(), primes.end());
  
  for (int prime : primes) {
    cout << prime << ", ";
  }

  cout << endl;
}

