package servers.lamportstorage;

public enum StorageType {
    READ(0), WRITE(1);

    private int value;
    StorageType(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}
