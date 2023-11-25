package lab03.assignments;

import przyklady03.ArrayRearrangement;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
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
        public int[] seq(){
            int[] sums = new int[numRows];
            for(int i=0; i< numRows; i++){
                sums[i]=0;
                for(int j=0; j< numColumns; j++){
                    sums[i]+=definition.applyAsInt(i,j);
                }
            }
            return sums;
        }

        public int[] rowSums() throws InterruptedException {
            Thread[] threads = new Thread[NUM_COLUMNS];
            int[] row = new int[NUM_COLUMNS];
            int[] rowSums = new int[NUM_ROWS];
            Thread mainThread=new Thread(new RowSummer(rowSums, row));
            CyclicBarrier barrier = new CyclicBarrier(NUM_COLUMNS, mainThread);
            for (int i = 0; i < NUM_COLUMNS; ++i) {
                threads[i] = new Thread(new PerColumnDefinitionApplier(i, barrier, row, mainThread));
                threads[i].start();
            }
            try {
                for (int i = 0; i < NUM_COLUMNS; ++i) {
                    threads[i].join();
                }
            } catch (InterruptedException e) {
                System.err.println("Main interrupted");
                Thread.currentThread().interrupt();
            }
            return rowSums;
        }

        private class PerColumnDefinitionApplier implements Runnable {
            private final int myColumnNo;
            private final CyclicBarrier barrier;
            private final int[] row;
            private final Thread mainThread;

            private PerColumnDefinitionApplier(
                    int myColumnNo,
                    CyclicBarrier barrier,
                    int[] row,
                    Thread mainThread
            ) {
                this.myColumnNo = myColumnNo;
                this.barrier = barrier;
                this.row = row;
                this.mainThread = mainThread;
            }

            @Override
            public void run() {
                for (int i = 0; i < NUM_ROWS; i++) {
                    try {
                        row[myColumnNo] = definition.applyAsInt(i, myColumnNo);
                        barrier.await();
                    }catch(InterruptedException | BrokenBarrierException e) {
                        Thread t = Thread.currentThread();
                        t.interrupt();
                        System.err.println(i+"x"+myColumnNo + " interrupted");
                    }
                }
            }
        }


        private class RowSummer implements Runnable {

            private final int[] rowSums;
            private final int[] row;
            private int currentRowNo;

            private RowSummer(int[] rowSums, int[] row) {
                this.rowSums = rowSums;
                this.row = row;
                this.currentRowNo = 0;
            }

            @Override
            public void run() {
                rowSums[currentRowNo] = 0;
                for (int i : row) {
                    rowSums[currentRowNo] += i;
                }
                currentRowNo++;
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
            int[] seqSums= matrix.seq();

            for (int i = 0; i < rowSums.length; i++) {
                System.out.println(i + " -> " + rowSums[i]);
                assert(rowSums[i]==seqSums[i]);
            }
        } catch (InterruptedException e) {
            System.err.println("Obliczenie przerwane");
            return;
        }
    }

}
