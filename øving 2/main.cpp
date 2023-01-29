#include "Workers.h"
#include <iostream>

int main(int argc, char const *argv[])
{
    Workers worker_threads(4);
    Workers event_loop(1);
    worker_threads.start(); // Create 4 internal threads
    event_loop.start(); // Create 1 internal thread

    worker_threads.post([] {
        std::cout << "Task 1" << std::endl;
    });

    worker_threads.post([] {
        std::cout << "Task 2" << std::endl;
    });

    event_loop.post([] { //task 3 should always finish before task 4
        std::cout << "Task 3" << std::endl;
    });

    event_loop.post([] { //task 4 should always finish after task 3
        std::cout << "Task 4" << std::endl;
    });

    //the task with the shortest time delay should always finish before the other
    event_loop.post_timeout([] { 
        std::cout << "Task 5" << std::endl;
    }, 3000);

    event_loop.post_timeout([] {
        std::cout << "Task 6" << std::endl;
    }, 2000);

    worker_threads.join(); // Calls join() on the worker threads
    event_loop.join(); // Calls join() on the event thread
    return 0;
}
