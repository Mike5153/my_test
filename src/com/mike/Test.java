package com.mike;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
//        new Test().testMemberLocal();

        testOOM();
    }

    private static void testOOM() {
        List<OOMObject> list = new ArrayList<>();
        while (true) {
            list.add(new OOMObject());
        }
    }

    private static void testMemberLocal() {
        long start = System.currentTimeMillis();
        List<Entry> list = new ArrayList<>();
        for (long i = 0L; i < 500000000; i++) {
            list.add(new Entry(i));
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    static class Entry{
        long i;
        public Entry(long i) {
            this.i = i;
        }
    }

    static class OOMObject {

    }

}
