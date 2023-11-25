package lab02.assignments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Vector {

    private static final int SUM_CHUNK_LENGTH = 10;
    private static final int DOT_CHUNK_LENGTH = 10;

    private final int[] elements;

    public Vector(int length) {
        this.elements = new int[length];
    }

    public Vector(int[] elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
    }

    public Vector sum(Vector other) throws InterruptedException {
        if (this.elements.length != other.elements.length) {
            throw new IllegalArgumentException("different lengths of summed vectors");
        }
        Vector result = new Vector(this.elements.length);
        int i=0;
        ArrayList<Thread> sum_threads = new ArrayList<>();
        try{
        while(i<this.elements.length){
            if(this.elements.length-i>=SUM_CHUNK_LENGTH){
                Thread sum = new Thread(new Summer(this, other, i, SUM_CHUNK_LENGTH, result));
                sum_threads.add(sum);
                sum.start();
            }else{
                Thread sum = new Thread(new Summer(this, other, i, this.elements.length-i, result));
                sum_threads.add(sum);
                sum.start();
            }
            i+=SUM_CHUNK_LENGTH;
        }

        for(Thread r: sum_threads){
            r.join();
        }

        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Sum interrupted");
        }
        return result;
    }

    private static class Summer implements Runnable {

        private final Vector leftVec;
        private final Vector rightVec;
        private final int startPosIncl;
        private final int len;
        private final Vector resVec;

        public Summer(Vector leftVec, Vector rightVec, int startPosIncl, int len, Vector resVec) {
            this.resVec = resVec;
            this.leftVec = leftVec;
            this.rightVec = rightVec;
            this.startPosIncl = startPosIncl;
            this.len = len;
        }

        @Override
        public void run() {
                for (int i = this.startPosIncl; i < this.startPosIncl + len; i++) {
                    this.resVec.elements[i] = this.leftVec.elements[i] + this.rightVec.elements[i];
                }
        }
    }

    public int dot(Vector other) throws InterruptedException {
        if (this.elements.length != other.elements.length) {
            throw new IllegalArgumentException("different lengths of dotted vectors");
        }
        int result = 0;
        int j=elements.length/DOT_CHUNK_LENGTH;
        if(elements.length%DOT_CHUNK_LENGTH!=0) j++;
        int[] resV=new int[j];
        int i=0;
        int which_one=0;
        ArrayList<Thread> dot_threads = new ArrayList<>();
        try{
        while(i<this.elements.length){
            if(this.elements.length-i>=DOT_CHUNK_LENGTH){
                Thread dot = new Thread(new Dotter(this, other, i, DOT_CHUNK_LENGTH, resV, which_one));
                dot_threads.add(dot);
                dot.start();
            }else{
                Thread dot = new Thread(new Dotter(this, other, i, this.elements.length-i, resV, which_one));
                dot_threads.add(dot);
                dot.start();
            }
            which_one++;
            i+=DOT_CHUNK_LENGTH;
        }

        for(Thread r: dot_threads){
            r.join();
        }
            for(int r: resV){
                result+=r;
            }


        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Dot interrupted");
        }
        return result;
    }

    private static class Dotter implements Runnable {

        private final Vector leftVec;
        private final Vector rightVec;
        private final int startPosInc;
        private final int len;
        private final int[] resVec;
        private final int resPos;

        public Dotter(Vector leftVec, Vector rightVect, int startPosInc, int len, int[] resVec, int resPos) {
            this.leftVec = leftVec;
            this.rightVec = rightVect;
            this.startPosInc = startPosInc;
            this.len = len;
            this.resVec = resVec;
            this.resPos = resPos;
        }

        @Override
        public void run() {
            resVec[resPos]=0;
                for (int i = this.startPosInc; i < this.startPosInc + len; i++) {
                    this.resVec[resPos] += this.leftVec.elements[i] * this.rightVec.elements[i];
                }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector)) {
            return false;
        }
        Vector other = (Vector) obj;
        return Arrays.equals(this.elements, other.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.elements);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < this.elements.length; ++i) {
            if (i > 0) {
                s.append(", ");
            }
            s.append(this.elements[i]);
        }
        s.append("]");
        return s.toString();
    }

    // ----------------------- TESTS -----------------------

    private static final Random RANDOM = new Random();

    private static Vector generateRandomVector(int length) {
        int[] a = new int[length];
        for (int i = 0; i < length; ++i) {
            a[i] = RANDOM.nextInt(10);
        }
        return new Vector(a);
    }

    private final Vector sumSequential(Vector other) {
        if (this.elements.length != other.elements.length) {
            throw new IllegalArgumentException("different lengths of summed vectors");
        }
        Vector result = new Vector(this.elements.length);
        for (int i = 0; i < result.elements.length; ++i) {
            result.elements[i] = this.elements[i] + other.elements[i];
        }
        return result;
    }

    private final int dotSequential(Vector other) {
        if (this.elements.length != other.elements.length) {
            throw new IllegalArgumentException("different lengths of dotted vectors");
        }
        int result = 0;
        for (int i = 0; i < this.elements.length; ++i) {
            result += this.elements[i] * other.elements[i];
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            Vector a = generateRandomVector(33);
            System.out.println(a);
            Vector b = generateRandomVector(33);
            System.out.println(b);
            Vector c = a.sum(b);
            assert (c.equals(a.sumSequential(b)));
            int d = a.dot(b);
            System.out.println(c);
            System.out.println(d);
            assert (d == a.dotSequential(b));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("computations interrupted");
        }
    }

}
