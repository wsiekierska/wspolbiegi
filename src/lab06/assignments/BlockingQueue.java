package lab06.assignments;
import java.util.LinkedList;

public class BlockingQueue<T> {

    private LinkedList<T> queue;
    private int capacity;
    public BlockingQueue(int capacity) {
        this.queue = new LinkedList<T>();
        this.capacity=capacity;
    }

    public synchronized T take() throws InterruptedException {
        while(queue.isEmpty()){
            wait();
        }
        T a= queue.getFirst();
        queue.removeFirst();
        notifyAll();
        return a;
    }

    public synchronized void put(T item) throws InterruptedException {
        while(queue.size()==capacity){
            wait();
        }
        queue.add(item);
        notifyAll();
    }

    public synchronized int getSize() {
        return this.queue.size();
    }

    public int getCapacity() {
        return this.capacity;
    }
}