package lol.hyper.cobalttester.tasks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TestQueue {
    private final Queue<Runnable> queue = new LinkedList<>();

    public synchronized void addTask(Runnable task) {
        queue.add(task);
        notify();
    }

    public synchronized Runnable getTask() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }

    public synchronized List<Runnable> listTasks() {
        return new ArrayList<>(queue);
    }
}
