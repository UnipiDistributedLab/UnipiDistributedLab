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
        for (int i = 1; i <= 5; i++) {
            StockQuote stockQuote = StockQuote.newBuilder()
                    .setPrice(100)
                    .setOfferNumber(i)
                    .setDescription("Price for stock:" + request.getTickerSymbol())
                    .build();
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
}