package com.company;
//this is the sorting loop for all the draw objects in frame
// TODO: Replace with Gosling version
public class QuickSort implements Runnable {
    Drawable[] a;
    int lo;
    int hi;
    double mid;

    QuickSort(Drawable[] a, int lo0, int hi0) {
        this.a = a;
        this.lo = lo0;
        this.hi = hi0;
        run();
    }

    static private void swap(Drawable[] a, int i, int j) {
        Drawable T;
        T = a[i];
        a[i] = a[j];
        a[j] = T;
    }

    @Override
    public void run() {
        int hi0 = hi;
        int lo0 = lo;
        if (hi0 > lo0) {

            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            mid = a[(lo0 + hi0) / 2] != null ? a[(lo0 + hi0) / 2].getZ() : 0;

            // loop through the array until indices cross
            while (lo <= hi) {
                /* find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while (a[lo] != null && (lo < hi0) && (a[lo].getZ() < mid))
                    ++lo;

                /* find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while (a[hi] != null && (hi > lo0) && (a[hi].getZ() > mid))
                    --hi;

                // if the indexes have not crossed, swap
                if (lo <= hi) {
                    swap(a, lo, hi);
                    ++lo;
                    --hi;
                }
            }

            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if (lo0 < hi)
                new QuickSort(a, lo0, hi);

            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if (lo < hi0)
                new QuickSort(a, lo, hi0);
        }
    }
}
