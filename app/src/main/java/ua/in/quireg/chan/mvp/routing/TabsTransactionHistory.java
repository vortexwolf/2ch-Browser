package ua.in.quireg.chan.mvp.routing;

import java.util.ArrayList;

import timber.log.Timber;

public class TabsTransactionHistory {

    private ArrayList<Integer> mStackArr;

    public TabsTransactionHistory() {
        mStackArr = new ArrayList<>();
    }

    void push(int entry) {
        Timber.v("push() entry: %s", entry);

        if (isAlreadyExists(entry)) {
            mStackArr.remove(Integer.valueOf(entry));
            Timber.v("entry %d exists, not pushed", entry);
        }
        mStackArr.add(entry);
        //printHistoryStack();
    }

    int pop() {
        Timber.v("pop()");

        int entry = -1;

        if (!isEmpty()) {
            mStackArr.remove(mStackArr.size() - 1);
        }

        if (!isEmpty()) {
            entry = mStackArr.get(mStackArr.size() - 1);
        }
        Timber.v("pop() returned %d", entry);
        //printHistoryStack();

        return entry;
    }

    private boolean isAlreadyExists(int entry) {
        return (mStackArr.contains(entry));
    }

//    public int popPrevious() {
//        Timber.v("popPrevious()");
//
//        int entry = -1;
//
//        if (!isEmpty() && mStackArr.size() > 2) {
//            entry = mStackArr.get(mStackArr.size() - 2);
//            mStackArr.remove(mStackArr.size() - 2);
//        }
//        Timber.v("popPrevious() returned %d", entry);
//        return entry;
//    }
//
//    /**
//     * This method returns top of the stack
//     * without removing it.
//     *
//     * @return
//     */
//    public int peek() {
//        if (!isEmpty()) {
//            return mStackArr.get(mStackArr.size() - 1);
//        }
//
//        return -1;
//    }

    boolean isEmpty() {
        return (mStackArr.size() == 0);
    }

    int getStackSize() {
        return mStackArr.size();
    }

    void emptyStack() {
        mStackArr.clear();
    }

    private void printHistoryStack() {
        Timber.v("history stack: ");
        for (int i = 0; i < mStackArr.size(); i++) {
            Timber.v("Position %d, tab: %d", i, mStackArr.get(i));
        }
    }
}
