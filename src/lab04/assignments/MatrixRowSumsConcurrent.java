package lab04.assignments;

import przyklady04.ProducersConsumersQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.IntBinaryOperator;


public class MatrixRowSumsConcurrent {

    private static final int NUM_ROWS = 10;
    private static final int NUM_COLUMNS = 100;

    private static class Matrix {

        private final int numRows;
        private final int numColumns;
        private final IntBinaryOperator definition;

        public Matrix(int numRows, int numColumns, IntBinaryOperator definition) {
            this.numRows = numRows;
            this.numColumns = numColumns;
            this.definition = definition;
        }

        public int[] rowSums() throws InterruptedException {
            int[] rowSums = new int[numRows];
            List<Thread> threads = new ArrayList<>();
            List<LinkedBlockingQueue> queues = new ArrayList<>();
            for (int columnNo = 0; columnNo < numColumns; ++columnNo) {
                LinkedBlockingQueue<Integer> buffer = new LinkedBlockingQueue<Integer>(numColumns);
                queues.add(buffer);
                Thread t = new Thread(new PerColumnDefinitionApplier(columnNo, Thread.currentThread(), buffer));
                threads.add(t);
            }
            for (Thread t : threads) {
                t.start();
            }
            try {
                for (int i = 0; i < numRows; i++) {
                    for (LinkedBlockingQueue<Integer> queue : queues) {
                        rowSums[i] += queue.take();
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("Main interrupted");
                Thread.currentThread().interrupt();
                for(Thread t: threads) t.interrupt();
            }
            return rowSums;
        }

        private class PerColumnDefinitionApplier implements Runnable {

            private final int myColumnNo;
            private final BlockingQueue<Integer> buffer;
            private final Thread mainThread;

            private PerColumnDefinitionApplier(
                    int myColumnNo,
                    Thread mainThread,
                    BlockingQueue<Integer> buffer
            ) {
                this.buffer = buffer;
                this.myColumnNo = myColumnNo;
                this.mainThread = mainThread;
            }

            @Override
            public void run() {
                try {
                    for (int i = 0; i < NUM_ROWS; i++) {
                        buffer.put(definition.applyAsInt(i, myColumnNo));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    mainThread.interrupt();
                    System.err.println("Wątek " + myColumnNo + " przerwany");
                }
            }
        }
    }


    public static void main(String[] args) {
        Matrix matrix = new Matrix(NUM_ROWS, NUM_COLUMNS, (row, column) -> {
            int a = 2 * column + 1;
            return (row + 1) * (a % 4 - 2) * a;
        });
        try {

            int[] rowSums = matrix.rowSums();

            for (int i = 0; i < rowSums.length; i++) {
                System.out.println(i + " -> " + rowSums[i]);
            }

        } catch (InterruptedException e) {
            System.err.println("Obliczenie przerwane");
            return;
        }
    }

}
