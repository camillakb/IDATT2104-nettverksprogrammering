#include "Workers.h"
#include <iostream>

int main(int argc, char const *argv[])
{
    Workers worker_threads(4);
    Workers event_loop(1);
    worker_threads.start(); //create 4 internal threads
    event_loop.start(); //create 1 internal thread

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

    //tasks with time delays
    event_loop.post_timeout([] { 
        std::cout << "Task 5" << std::endl;
    }, 0);

    event_loop.post_timeout([] {
        std::cout << "Task 6" << std::endl;
    }, 2000);

    //using sleep to be able to see all the prints before the program is finished running
    std::this_thread::sleep_for(std::chrono::milliseconds(4000));

    worker_threads.join(); //calls join() on the worker threads
    event_loop.join(); //calls join() on the event thread
    return 0;
}
