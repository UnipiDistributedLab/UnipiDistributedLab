package servers.lamportstorage;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.examples.utlis.LamportClock.*;
import io.grpc.stub.StreamObserver;
import servers.TimeOutConfigParams;
import servers.leaderelection.ServerData;
import utlis.Atomic;
import utlis.LamportClock;
import utlis.Utils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class LeaderStorageHelper {
    private LamportClock clock;
    private ArrayList<ServerData> allServersData;
    private ArrayList<ServerData> writeServers = new ArrayList<>();
    private ArrayList<ServerData> readServers = new ArrayList<>();
    private ServerData thisServerData;
    private Atomic<Boolean> isUnderSync = new Atomic<>();
    private Long period = TimeOutConfigParams.shared().getValueStorageSyncPeriodMS();
    private Integer archivePeriod = TimeOutConfigParams.shared().getArchiveLogsCycle();
    private Integer clearFilesPeriod = TimeOutConfigParams.shared().getClearLogFileCycle();
    private ScheduledThreadPoolExecutor carrierThread = new ScheduledThreadPoolExecutor(1);
    private Runnable periodicWork;
    private ScheduledFuture periodicScheduler;

    public LeaderStorageHelper(ArrayList<ServerData> allServersData, ServerData thisServerData, ServerBuilder builder) {
        this.thisServerData = thisServerData;
        updateAllServer(allServersData);
        isUnderSync.set(false);
        periodicSync();
    }

    public void updateAllServer(ArrayList<ServerData> allServersData) {
        this.allServersData = allServersData;
        for (ServerData serverData : allServersData) {
            if (serverData.getId() == thisServerData.getId()) continue;
            if (serverData.getType() == StorageType.READ) {
                readServers.add(serverData);
                continue;
            }
            writeServers.add(serverData);
        }
    }

    public void stopOperations() {
        carrierThread.shutdownNow();
    }

    public int getClock() {
        return clock.getClock();
    }

    /**
     * We can optimise this in order to get all data and sync them we can get only the new time stamps and update the deltas
     */
    private void periodicSync() {
        periodicWork = () -> {
            isUnderSync.set(true);
            Map<Integer, String> syncDataMap = new HashMap<>();
            for (ServerData writeSever : writeServers) {
//                Function handleResponse = (responseData) -> {
//                    if (!(responseData instanceof Map)) return null;
//                    if (responseData != null) {
//                        syncDataMap.putAll((Map<? extends Integer, ? extends String>) responseData);
//                    }
//                    return null;
//                };
//                readValuesFromAsync(writeSever, handleResponse);
                Map<Integer, String> responseData = readValuesFrom(writeSever);
                if (responseData != null && !responseData.isEmpty()) {
                    syncDataMap.putAll(responseData);
                    int maxClock = Collections.max(syncDataMap.keySet());
                    if (clock == null) clock = new LamportClock(maxClock);
                }
                if (clock == null) clock = new LamportClock(0);
            }
            if (syncDataMap.isEmpty()) {
                isUnderSync.set(false);
                return;
            }
            archivePeriod--;
            if (archivePeriod <= 0) {
                archiveData(syncDataMap);
            }
            for (ServerData readServer : readServers) {
                updateValueTo(syncDataMap, readServer);
            }
            for (ServerData writeSever : writeServers) {
                updateValueTo(syncDataMap, writeSever);
            }
            isUnderSync.set(false);
//            });
//            thead.start();
        };
        carrierThread.scheduleAtFixedRate(periodicWork, period, period, TimeUnit.MILLISECONDS);
    }

    private void archiveData(Map<Integer, String> dataMap) {
        archivePeriod = TimeOutConfigParams.shared().getArchiveLogsCycle();
        System.out.println("Data are " + dataMap.keySet().size());
        List<Integer> keys = new ArrayList<>(dataMap.keySet());
        Collections.sort(keys);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        FileWriter fileWriter = null;
        String logFolderName = "logs";
        String logFolderFullPath = System.getProperty("user.dir") + "/" + logFolderName;
        Boolean isWindows = System.getProperties().getProperty("os.name").toLowerCase().contains("windows");
        if (isWindows) {
            logFolderFullPath =  System.getProperty("user.dir") + "\\" + logFolderName;
        }
        if (TimeOutConfigParams.shared().getSupportsClearLogFileCycle()) {
            clearFilesPeriod--;
            if (clearFilesPeriod <= 0) {
                clearFolder(logFolderFullPath);
            }
        }
        try {
            fileWriter = new FileWriter( logFolderFullPath + "/log_" + dtf.format(now) +".txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println("Data sync " + dtf.format(now));
            for (Integer key : keys) {
                printWriter.println(key + " " + dataMap.get(key).toString());
            }
            printWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void clearFolder(String logFolderPath) {
        clearFilesPeriod = TimeOutConfigParams.shared().getClearLogFileCycle();
        File[] listOfFiles = new File(logFolderPath).listFiles();
        if (listOfFiles.length == 0) return;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getName());
            }
            file.delete();
        }
    }

    @Nullable
    private Map<Integer, String> readValuesFrom(ServerData server) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(server.getTotalUrl())
                .usePlaintext()
                .build();
        ValueStoreGrpc.ValueStoreBlockingStub blockingStub = ValueStoreGrpc.newBlockingStub(channel);
        Empty request = null;
        try {
            request = Empty
                    .newBuilder()
                    .build();
        } finally {
            try {
                ReadAllReply response = blockingStub.readAll(request);
                channel.shutdownNow();
                return response.getMapMap();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                periodicScheduler.notify();
                carrierThread.scheduleAtFixedRate(periodicWork, 2, period, TimeUnit.MILLISECONDS);
            }
        }
        return null;
    }

    private void readValuesFromAsync(ServerData server, Function callBack) {
        Thread thread = new Thread((Runnable) () -> {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(server.getTotalUrl())
                    .usePlaintext()
                    .build();
            ValueStoreGrpc.ValueStoreBlockingStub blockingStub = ValueStoreGrpc.newBlockingStub(channel);
            Empty request = null;
            try {
                request = Empty
                        .newBuilder()
                        .build();
            } finally {
                try {
                    ReadAllReply response = blockingStub.readAll(request);
                    channel.shutdownNow();
                    callBack.apply(response.getMapMap());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    periodicScheduler.notify();
                    carrierThread.scheduleAtFixedRate(periodicWork, 2, period, TimeUnit.MILLISECONDS);
                }
            }
        });
        thread.start();
    }

    private void updateValueTo(Map<Integer, String> data, ServerData server) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(server.getTotalUrl())
                .usePlaintext()
                .build();
        ValueStoreGrpc.ValueStoreBlockingStub blockingStub = ValueStoreGrpc.newBlockingStub(channel);
        UpdateRequest request = null;
        try {
            request = UpdateRequest
                    .newBuilder()
                    .putAllMap(data)
                    .build();
        } finally {
            try {
                UpdateReply response = blockingStub.updateSecondary(request);
                channel.shutdownNow();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                periodicScheduler.notify();
                carrierThread.scheduleAtFixedRate(periodicWork, 2, period, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void retryWriteExclude(WriteRequest request, StreamObserver<WriteReply> responseObserver, ServerData serverExcData, int requestTime) {
        ArrayList<ServerData> otherWriteServers = new ArrayList<>();
        for (ServerData serverData : writeServers) {
            if (serverData == serverExcData) continue;
            otherWriteServers.add(serverData);
        }
        int serverIndex = Math.abs(Utils.getString().hashCode()) % otherWriteServers.size();
        ServerData writeSeverData = otherWriteServers.get(serverIndex);
        writeRequest(request, responseObserver, writeSeverData, requestTime);
    }

    private void writeRequest(WriteRequest request, StreamObserver<WriteReply> responseObserver, ServerData writeSeverData, int lamportClock) {
        while (clock == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                responseObserver.onError(e);
                responseObserver.onCompleted();
                throw new RuntimeException(e);
            }
        }
//        System.out.println("Clock Timer request is " + lamportClock);
        Thread thread = new Thread(() -> {
            ManagedChannel channel = null;
            try {
                while (isUnderSync.get()){
                    Thread.sleep(100);
                }
                channel = ManagedChannelBuilder.forTarget(writeSeverData.getTotalUrl())
                        .usePlaintext()
                        .build();
                ValueStoreGrpc.ValueStoreBlockingStub blockingStub = ValueStoreGrpc.newBlockingStub(channel);
                WriteRequest writeRequest = WriteRequest.newBuilder()
                        .setValue(request.getValue())
                        .setId(request.getId())
                        .setCounter(lamportClock)
                        .setTimestamp(request.getTimestamp())
                        .build();
                try {
                    WriteReply response = blockingStub.write(writeRequest);
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    retryWriteExclude(request, responseObserver, writeSeverData, lamportClock);
                    System.out.println(e.getMessage());
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
            } finally {
                if (channel != null) channel.shutdownNow();
            }
        });
        thread.start();
    }


    public void write(WriteRequest request, StreamObserver<WriteReply> responseObserver, int requestTime) {
        int serverIndex = Math.abs(request.getId().hashCode()) % writeServers.size();
        ServerData writeSeverData = writeServers.get(serverIndex);
        int lamportClock = clock.tick(requestTime);
        writeRequest(request, responseObserver, writeSeverData, lamportClock);
    }

    public void read(ReadRequest request, StreamObserver<ReadReply> responseObserver) {
        int serverIndex = Math.abs(request.getId().hashCode()) % readServers.size();
        ManagedChannel channel = null;
        try {
            ServerData readServersData = readServers.get(serverIndex);
            channel = ManagedChannelBuilder.forTarget(readServersData.getTotalUrl())
                    // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            ValueStoreGrpc.ValueStoreBlockingStub blockingStub = ValueStoreGrpc.newBlockingStub(channel);
            ReadRequest readRequest = ReadRequest.newBuilder()
                    .setId(request.getId())
                    .setCounter(request.getCounter())
                    .build();
            ReadReply response = blockingStub.read(readRequest);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            if (channel != null) channel.shutdownNow();
        }
    }

    public void readAll(Empty request, StreamObserver<ReadAllReply> responseObserver) {

    }

}
