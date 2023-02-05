package rpc;

import io.grpc.stub.StreamObserver;
import io.grpc.unipi.distributed.Stock;
import io.grpc.unipi.distributed.StockQuote;
import io.grpc.unipi.distributed.StockQuoteProviderGrpc;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class StreamServiceImpl extends StockQuoteProviderGrpc.StockQuoteProviderImplBase {
    private static LinkedHashSet<StreamObserver<StockQuote>> observers = new LinkedHashSet<>();

    @Override
    public void serverSideStreamingGetListStockQuotes(Stock request, StreamObserver<StockQuote> responseObserver) {
        observers.add(responseObserver);
        Runnable runnable = () -> {
            for(int i=0; i<10; i++) {
                StockQuote stockQuote = StockQuote.newBuilder()
                        .setPrice(100 + i)
                        .setOfferNumber(i)
                        .setDescription("Price for stock:" + request.getTickerSymbol())
                        .setTickerSymbol(request.getTickerSymbol())
                        .setCompanyName(request.getCompanyName())
                        .build();
                sendBroadCaste(stockQuote);
            }
        };

        Thread vThread = Thread.ofVirtual().start(runnable);
    }

    @Override
    public StreamObserver<Stock> chatStreamingGetListStockQuotes(StreamObserver<StockQuote> responseObserver) {
        return new StreamObserver<Stock>() {

            @Override
            public void onNext(Stock value) {

                System.out.println(value);
                StockQuote stockQuote = StockQuote.newBuilder()
                        .setPrice(100)
                        .setOfferNumber(100)
                        .setDescription("Price for stock:" + value.getTickerSymbol())
                        .setTickerSymbol(value.getTickerSymbol())
                        .setCompanyName(value.getCompanyName())
                        .build();
                sendBroadCaste(stockQuote);
            }

            @Override
            public void onError(Throwable t) {
                t.getMessage();
                observers.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed!");
//                responseObserver.onCompleted();
//                observers.remove(responseObserver);
            }
        };
    }

    private void sendBroadCaste(StockQuote stockQuote)  {
        ArrayList<StreamObserver<StockQuote>> toBeRemoved = new ArrayList<>();
        for (StreamObserver<StockQuote> observer : observers) {
            try {
                observer.onNext(stockQuote);
                System.out.println("Sent message to: " + observer);
            } catch (Exception e) {
                toBeRemoved.add(observer);
                System.out.println(e.getMessage() + " " + observer.toString());
            }
        }
        for (StreamObserver<StockQuote> observer : toBeRemoved) {
            observers.remove(observer);
        }
    }
}