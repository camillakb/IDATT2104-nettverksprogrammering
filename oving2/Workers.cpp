#include "Workers.h"
#include <unistd.h>

//constructor for the Workers class
Workers::Workers(int threadnum) {
    this->threadnum = threadnum;
    tasks = std::list<std::function<void()>>();
    threads = std::vector<std::thread>(threadnum);
    should_stop = false;
}

//method to add a task to a queue of tasks
void Workers::post(std::function<void()> task) {
    {
        std::unique_lock<std::mutex> lock(tasks_mutex);
        tasks.emplace_back(task);
    }
    tasks_cv.notify_one();
}

//method to delay the task by a given amount of milliseconds
void task_time_delay(std::function<void()> task, int ms) {
    std::this_thread::sleep_for(std::chrono::milliseconds(ms)); 
    task();
}

//method to run tasks using a time delay
void Workers::post_timeout(std::function<void()> task, int waiting_time) {
    std::function<void()> func = bind(task_time_delay, task, waiting_time); 
    {
        std::lock_guard<std::mutex> lock(this->tasks_mutex);
        this->tasks.emplace_back(func); 
    }
}

//method to initialize threads that are waiting, until they are given a task
void Workers::start() {
    for (int i = 0; i < threadnum; i++) {
        threads.emplace_back([this] {
            while (true) {
                std::function<void()> task;
                {
                    std::unique_lock<std::mutex> lock(tasks_mutex);

                    while (tasks.empty() && !should_stop) {
                        tasks_cv.wait(lock);
                    }

                    if (should_stop) {
                        return;
                    }

                    task = tasks.front(); //copy task for later use
                    tasks.pop_front();          //remove task from queue
                }

                if (task)
                    task(); //run task outside of mutex lock
            }
        });
    }
}

//need to make sure that all of the tasks are executed before stopping the program
//method to stop the Workers threads when the task list is empty
void Workers::join() {
    should_stop = true;
    tasks_cv.notify_all();

    for (auto &thread : threads) {
        if (thread.joinable()) { 
            thread.join();
        }
    }
}