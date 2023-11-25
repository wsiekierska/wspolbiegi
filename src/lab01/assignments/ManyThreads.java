package lab01.assignments;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ManyThreads {

    private static final int THREAD_COUNT = 100;

    private static class MyThread implements Runnable {

        private final int n;

        public MyThread(int n) {
            this.n = n;
        }

        @Override
        public void run() {
            // FIXME: implement here

            if(n!=100) {
                Runnable r = new MyThread(n + 1);
                Thread t= new Thread(r);
                t.start();
            }
            for(int i=0; i<n; i++){
                double unboundedRandomValue =  Math.random();
            }
            System.out.println(n);
        }
    }

    public static void main(String[] args) {
        // FIXME: implement
        Runnable r = new MyThread(1);
        Thread t= new Thread(r);
        t.start();
    }

}