#include <functional>
#include <queue>
#include <vector>
#include <thread>
#include <condition_variable>
#include <mutex>
#include <atomic>

class Workers {
    std::queue<std::function<void()>> tasks;
    std::vector<std::thread> threads;
    std::condition_variable tasks_cv;
    std::condition_variable finished_cv;
    std::mutex tasks_mutex;
    int threadnum;
    std::atomic_bool should_stop;

    public:
    Workers(int);

    void start();
    void post(std::function<void()>);
    void post_timeout(std::function<void()>, int);
    void join();
};