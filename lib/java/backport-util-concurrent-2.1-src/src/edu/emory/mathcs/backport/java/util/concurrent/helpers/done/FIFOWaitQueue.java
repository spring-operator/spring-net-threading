package edu.emory.mathcs.backport.java.util.concurrent.helpers;

import java.util.*;

/**
 * Simple linked list queue used in FIFOSemaphore.
 * Methods are not synchronized; they depend on synch of callers.
 * Must be public, since it is used by Semaphore (outside this package).
 * NOTE: this class is NOT present in java.util.concurrent.
 **/

public class FIFOWaitQueue extends WaitQueue implements java.io.Serializable {
    protected transient WaitNode head_ = null;
    protected transient WaitNode tail_ = null;

    public FIFOWaitQueue() {}

    public void insert(WaitNode w) {
        if (tail_ == null)
            head_ = tail_ = w;
        else {
            tail_.next = w;
            tail_ = w;
        }
    }

    public WaitNode extract() {
        if (head_ == null)
            return null;
        else {
            WaitNode w = head_;
            head_ = w.next;
            if (head_ == null)
                tail_ = null;
            w.next = null;
            return w;
        }
    }

    public boolean hasNodes() {
        return head_ != null;
    }

    public int getLength() {
        int count = 0;
        WaitNode node = head_;
        while (node != null) {
            if (node.waiting) count++;
            node = node.next;
        }
        return count;
    }

    public Collection getWaitingThreads() {
        List list = new ArrayList();
        int count = 0;
        WaitNode node = head_;
        while (node != null) {
            if (node.waiting) list.add(node.owner);
            node = node.next;
        }
        return list;
    }

    public boolean isWaiting(Thread thread) {
        if (thread == null) throw new NullPointerException();
        for (WaitNode node = head_; node != null; node = node.next) {
            if (node.waiting && node.owner == thread) return true;
        }
        return false;
    }

}
