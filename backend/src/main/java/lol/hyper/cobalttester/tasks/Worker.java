package lol.hyper.cobalttester.tasks;

public class Worker extends Thread {

    private final TestQueue taskQueue;

    public Worker(TestQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Runnable task = taskQueue.getTask();
                if (task == null) {
                    break;
                }
                task.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
