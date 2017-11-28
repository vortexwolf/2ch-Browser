package ua.in.quireg.chan.ui.views;

import android.util.Log;

import java.util.ArrayList;

public class FragmentHistory {
    private static final String LOG_TAG = FragmentHistory.class.getSimpleName();

    private ArrayList<Integer> stackArr;

    public FragmentHistory() {
        stackArr = new ArrayList<>();

    }

    public void push(int entry) {
        Log.d(LOG_TAG, "push() entry:" + entry);

        if (isAlreadyExists(entry)) {
            stackArr.remove(Integer.valueOf(entry));
            Log.d(LOG_TAG, String.format("entry %d exitsts, not pushed", entry));
        }
        stackArr.add(entry);
        printHistoryStack();
    }

    private boolean isAlreadyExists(int entry) {
        return (stackArr.contains(entry));
    }

    public int pop() {

        Log.d(LOG_TAG, "pop()");
        int entry = -1;

        if (!isEmpty()) {
            stackArr.remove(stackArr.size() - 1);
        }

        if(!isEmpty()){
            entry = stackArr.get(stackArr.size() - 1);
        }
        Log.d(LOG_TAG, String.format("pop() returned %d", entry));
        printHistoryStack();
        return entry;
    }

    public int popPrevious() {
        Log.d(LOG_TAG, "popPrevious()");

        int entry = -1;

        if (!isEmpty() && stackArr.size() > 2) {
            entry = stackArr.get(stackArr.size() - 2);
            stackArr.remove(stackArr.size() - 2);
        }
        Log.d(LOG_TAG, String.format("popPrevious() returned %d", entry));
        return entry;
    }


    /**
     * This method returns top of the stack
     * without removing it.
     *
     * @return
     */
    public int peek() {
        if (!isEmpty()) {
            return stackArr.get(stackArr.size() - 1);
        }

        return -1;
    }


    public boolean isEmpty() {
        return (stackArr.size() == 0);
    }


    public int getStackSize() {
        return stackArr.size();
    }

    public void emptyStack() {

        stackArr.clear();
    }

    private void printHistoryStack(){
        Log.d(LOG_TAG, String.format("history stack: "));
        for (int i = 0; i < stackArr.size(); i++) {
            Log.d(LOG_TAG, String.format("Position %d, tab: %d", i, stackArr.get(i)));
        }
    }
}
