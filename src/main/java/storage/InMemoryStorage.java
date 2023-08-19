package storage;

import lamport.LamportClock;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class InMemoryStorage {

    private LamportClock clock = new LamportClock();

    private Map<Integer, String> storage = new HashMap<>();

    public int save(String note) {
        clock.increase();
        storage.put(clock.getCounter(), note);
        return  clock.getCounter();
    }

    public @Nullable String get(Integer counterPoint) {
        return storage.get(counterPoint);
    }

    public void replaceAll(Map<Integer, String> storage) {
        this.storage = storage;
    }

    public Map<Integer, String> getStorage() {
        return storage;
    }
}
