/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

import edu.emory.mathcs.backport.java.util.*;
import edu.emory.mathcs.backport.java.util.concurrent.helpers.Utils;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * Estimates time per iteration of collection iterators.  Preloads
 * most elements, but adds about 1/8 of them dynamically to preclude
 * overly clever optimizations. The array of collections has
 * approximately exponentially different lengths, so check both short
 * and long iterators.  Reports include times for adds and other
 * checks, so overestimate times per iteration.
 */

public final class IteratorLoops {
    static final int DEFAULT_SIZE = 16384;
    static final int DEFAULT_TRIALS = 4;
    static final int NC = 16; // number of collections must be power of 2
    static volatile long mismatches = 0;
    static int randomSeed = 3122688;

    public static void main(String[] args) throws Exception {
        Class klass = Class.forName(args[0]);
        int n = (args.length <= 1)? DEFAULT_SIZE : Integer.parseInt(args[1]);
        int t = (args.length <= 2)? DEFAULT_TRIALS : Integer.parseInt(args[2]);

        System.out.print("Class: " + klass.getName());
        System.out.print(" ~iters: " + (long)n * (long)n);
        System.out.print(" trials: " + t);
        System.out.println();

        Collection[] colls =
            (Collection[])new Collection[NC];

        for (int k = 0; k < colls.length; ++k)
            colls[k] = (Collection)klass.newInstance();

        for (int i = 0; i < t; ++i)
            new IteratorLoops(colls).oneRun(n);

        if (mismatches != 0)
            throw new Error("Bad checksum :" + mismatches);
    }

    private int elementCount;
    private final Collection[] cs;

    IteratorLoops(Collection[] colls) {
        cs = colls;
        elementCount = 0;
    }

    void oneRun(int n) {
        preload(n);
        long startTime = Utils.nanoTime();
        long count = traversals(n);
        double elapsed = (double)(Utils.nanoTime() - startTime);
        double npi = elapsed / count;
        double secs = elapsed / 1000000000;
        System.out.print("" + npi + " ns/iter  " + secs + "s run time\n");
    }

    long traversals(int n) {
        long count = 0;
        long check = 0;
	for (int i = 0; i < n; i++) {
            check += elementCount;
            count += counts();
            maybeAdd();
        }
        if (count != check)
            mismatches = count;
        return count;
    }

    int counts() {
        int count = 0;
        for (int k = 0; k < cs.length; ++k) {
            for (Iterator it = cs[k].iterator(); it.hasNext();) {
                if (it.next() != null)
                    ++count;
            }
        }
        return count;
    }

    void maybeAdd() {
        int r = randomSeed;
        r ^= r << 6;
        r ^= r >>> 21;
        r ^= r << 7;
        randomSeed = r;
        if ((r >>> 29) == 0)
            cs[r & (cs.length-1)].add(new Integer(elementCount++));
    }

    void preload(int n) {
        for (int i = 0; i < cs.length; ++i)
            cs[i].clear();
        int k = (n - n / 8) / 2;
        ArrayList al = new ArrayList(k+1);
        for (int i = 0; i < cs.length; ++i) {
            if (k > 0) {
                for (int j = 0; j < k; ++j)
                    al.add(new Integer(elementCount++));
                cs[i].addAll(al);
                al.clear();
            }
            k >>>= 1;
        }
        // let GC settle down
        try { Thread.sleep(500); } catch(Exception ex) { return; }
    }


}
