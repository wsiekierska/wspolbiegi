package lab03.assignments;

import java.util.concurrent.Callable;
import java.util.function.IntBinaryOperator;

public class MatrixRowSums {

    private static final int ROWS = 3;
    private static final int COLUMNS = 10;

    private static class Matrix {

        private final int rows;
        private final int columns;
        private final IntBinaryOperator definition;

        public Matrix(int rows, int columns, IntBinaryOperator definition) {
            this.rows = rows;
            this.columns = columns;
            this.definition = definition;
        }

        public int[] rowSums() {
            int[] rowSums = new int[rows];
            for (int row = 0; row < rows; ++row) {
                int sum = 0;
                for (int column = 0; column < columns; ++column) {
                    sum += definition.applyAsInt(row, column);
                }
                rowSums[row] = sum;
            }
            return rowSums;
        }

        public int[] rowSumsConcurrent() {
            int res[] = {};
            // YOUR CODE GOES HERE

            // YOUR CODE GOES HERE
            return res;
        }
    }

    public static void main(String[] args) throws Exception {
        Matrix matrix = new Matrix(ROWS, COLUMNS, (row, column) -> {
            // long computations
            int a = 2 * column + 1;
            int cellId = column + row * COLUMNS;
            try {
                // different cells computations in rows takes different time to complete
                // and hence some thread will wait for others
                // but nevertheless there should be substantial gain from concurrent solutions
                Thread.sleep((1000 - (cellId % 13) * 1000 / 12));
            } catch (InterruptedException e) {
                Thread t = Thread.currentThread();
                t.interrupt();
                System.err.println(t.getName() + " interrupted");
            }
            return (row + 1) * (a % 4 - 2) * a;
        });

        timeIt("sequential execution", "should take about 17s to complete", () -> matrix.rowSums());

        // concurrent computations
        timeIt("concurrent execution", "should take about 3.5s to complete, "
                + "if thread are synchronized after every row calculations", () -> matrix.rowSumsConcurrent());
    }

    public static void timeIt(String name, String note, Callable<int[]> func) throws Exception {
        System.out.println("Running " + name + " (" + note + ")...");
        long startTime = System.currentTimeMillis();
        int[] rowSums = func.call();
        long usedTime = System.currentTimeMillis() - startTime; // in milliseconds
        System.out.println(name + " took: " + usedTime / 100 / 10. + "s");
        System.out.println("Result:");
        for (int i = 0; i < rowSums.length; i++) {
            System.out.println(i + " -> " + rowSums[i]);
        }
    }

}
