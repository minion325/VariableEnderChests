package me.saif.betterenderchests.utils;

import java.util.ArrayList;
import java.util.List;

public class Callback<T> {

    private final List<Runnable> runnables = new ArrayList<>();
    private T result;
    private boolean results = false;

    public void addResultListener(Runnable runnable) {
        if (!this.hasResults())
            this.runnables.add(runnable);
        else {
            runnable.run();
        }
    }

    /**
     * Ideally this method should be called from the main thread.
     *
     * @param result Result that is being proviaded to the callback.
     */
    public void setResult(T result) {
        if (this.hasResults())
            throw new IllegalStateException("Callback already has a result");
        this.result = result;
        this.results = true;

        if (this.runnables.size() == 0)
            return;

        for (Runnable runnable : this.runnables) {
            runnable.run();
        }
        this.runnables.clear();
    }

    public boolean hasResults() {
        return this.results;
    }

    public synchronized T getResult() {
        return result;
    }

    public static<T>Callback<T> withResult(T result){
        Callback<T> callback = new Callback<>();
        callback.setResult(result);
        return callback;
    }

    @Override

    public String toString() {
        return "Callback{" +
                ", result=" + result +
                ", results=" + results +
                '}';
    }

}
