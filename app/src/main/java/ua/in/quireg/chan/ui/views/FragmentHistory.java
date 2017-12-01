package ua.in.quireg.chan.ui.views;

import java.util.ArrayList;

import timber.log.Timber;

public class FragmentHistory {

    private ArrayList<Integer> stackArr;

    public FragmentHistory() {
        stackArr = new ArrayList<>();

    }

    public void push(int entry) {
        Timber.v("push() entry: %s", entry);

        if (isAlreadyExists(entry)) {
            stackArr.remove(Integer.valueOf(entry));
            Timber.v("entry %d exists, not pushed", entry);
        }
        stackArr.add(entry);
        printHistoryStack();
    }

    private boolean isAlreadyExists(int entry) {
        return (stackArr.contains(entry));
    }

    public int pop() {

        Timber.v("pop()");
        int entry = -1;

        if (!isEmpty()) {
            stackArr.remove(stackArr.size() - 1);
        }

        if(!isEmpty()){
            entry = stackArr.get(stackArr.size() - 1);
        }
        Timber.v("pop() returned %d", entry);
        printHistoryStack();
        return entry;
    }

    public int popPrevious() {
        Timber.v("popPrevious()");

        int entry = -1;

        if (!isEmpty() && stackArr.size() > 2) {
            entry = stackArr.get(stackArr.size() - 2);
            stackArr.remove(stackArr.size() - 2);
        }
        Timber.v("popPrevious() returned %d", entry);
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
        Timber.v("history stack: ");
        for (int i = 0; i < stackArr.size(); i++) {
            Timber.v("Position %d, tab: %d", i, stackArr.get(i));
        }
    }
}
