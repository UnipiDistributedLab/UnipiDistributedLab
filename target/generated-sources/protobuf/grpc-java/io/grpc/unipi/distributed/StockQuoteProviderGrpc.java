package io.grpc.unipi.distributed;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.45.0)",
    comments = "Source: distributed.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class StockQuoteProviderGrpc {

  private StockQuoteProviderGrpc() {}

  public static final String SERVICE_NAME = "org.example.distributed.StockQuoteProvider";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.grpc.unipi.distributed.Stock,
      io.grpc.unipi.distributed.StockQuote> getServerSideStreamingGetListStockQuotesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "serverSideStreamingGetListStockQuotes",
      requestType = io.grpc.unipi.distributed.Stock.class,
      responseType = io.grpc.unipi.distributed.StockQuote.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<io.grpc.unipi.distributed.Stock,
      io.grpc.unipi.distributed.StockQuote> getServerSideStreamingGetListStockQuotesMethod() {
    io.grpc.MethodDescriptor<io.grpc.unipi.distributed.Stock, io.grpc.unipi.distributed.StockQuote> getServerSideStreamingGetListStockQuotesMethod;
    if ((getServerSideStreamingGetListStockQuotesMethod = StockQuoteProviderGrpc.getServerSideStreamingGetListStockQuotesMethod) == null) {
      synchronized (StockQuoteProviderGrpc.class) {
        if ((getServerSideStreamingGetListStockQuotesMethod = StockQuoteProviderGrpc.getServerSideStreamingGetListStockQuotesMethod) == null) {
          StockQuoteProviderGrpc.getServerSideStreamingGetListStockQuotesMethod = getServerSideStreamingGetListStockQuotesMethod =
              io.grpc.MethodDescriptor.<io.grpc.unipi.distributed.Stock, io.grpc.unipi.distributed.StockQuote>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "serverSideStreamingGetListStockQuotes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.grpc.unipi.distributed.Stock.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.grpc.unipi.distributed.StockQuote.getDefaultInstance()))
              .setSchemaDescriptor(new StockQuoteProviderMethodDescriptorSupplier("serverSideStreamingGetListStockQuotes"))
              .build();
        }
      }
    }
    return getServerSideStreamingGetListStockQuotesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.grpc.unipi.distributed.Stock,
      io.grpc.unipi.distributed.StockQuote> getChatStreamingGetListStockQuotesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "chatStreamingGetListStockQuotes",
      requestType = io.grpc.unipi.distributed.Stock.class,
      responseType = io.grpc.unipi.distributed.StockQuote.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<io.grpc.unipi.distributed.Stock,
      io.grpc.unipi.distributed.StockQuote> getChatStreamingGetListStockQuotesMethod() {
    io.grpc.MethodDescriptor<io.grpc.unipi.distributed.Stock, io.grpc.unipi.distributed.StockQuote> getChatStreamingGetListStockQuotesMethod;
    if ((getChatStreamingGetListStockQuotesMethod = StockQuoteProviderGrpc.getChatStreamingGetListStockQuotesMethod) == null) {
      synchronized (StockQuoteProviderGrpc.class) {
        if ((getChatStreamingGetListStockQuotesMethod = StockQuoteProviderGrpc.getChatStreamingGetListStockQuotesMethod) == null) {
          StockQuoteProviderGrpc.getChatStreamingGetListStockQuotesMethod = getChatStreamingGetListStockQuotesMethod =
              io.grpc.MethodDescriptor.<io.grpc.unipi.distributed.Stock, io.grpc.unipi.distributed.StockQuote>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "chatStreamingGetListStockQuotes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.grpc.unipi.distributed.Stock.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.grpc.unipi.distributed.StockQuote.getDefaultInstance()))
              .setSchemaDescriptor(new StockQuoteProviderMethodDescriptorSupplier("chatStreamingGetListStockQuotes"))
              .build();
        }
      }
    }
    return getChatStreamingGetListStockQuotesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static StockQuoteProviderStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StockQuoteProviderStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StockQuoteProviderStub>() {
        @java.lang.Override
        public StockQuoteProviderStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StockQuoteProviderStub(channel, callOptions);
        }
      };
    return StockQuoteProviderStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static StockQuoteProviderBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StockQuoteProviderBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StockQuoteProviderBlockingStub>() {
        @java.lang.Override
        public StockQuoteProviderBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StockQuoteProviderBlockingStub(channel, callOptions);
        }
      };
    return StockQuoteProviderBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static StockQuoteProviderFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<StockQuoteProviderFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<StockQuoteProviderFutureStub>() {
        @java.lang.Override
        public StockQuoteProviderFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new StockQuoteProviderFutureStub(channel, callOptions);
        }
      };
    return StockQuoteProviderFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class StockQuoteProviderImplBase implements io.grpc.BindableService {

    /**
     */
    public void serverSideStreamingGetListStockQuotes(io.grpc.unipi.distributed.Stock request,
        io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.StockQuote> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getServerSideStreamingGetListStockQuotesMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.Stock> chatStreamingGetListStockQuotes(
        io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.StockQuote> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getChatStreamingGetListStockQuotesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getServerSideStreamingGetListStockQuotesMethod(),
            io.grpc.stub.ServerCalls.asyncServerStreamingCall(
              new MethodHandlers<
                io.grpc.unipi.distributed.Stock,
                io.grpc.unipi.distributed.StockQuote>(
                  this, METHODID_SERVER_SIDE_STREAMING_GET_LIST_STOCK_QUOTES)))
          .addMethod(
            getChatStreamingGetListStockQuotesMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                io.grpc.unipi.distributed.Stock,
                io.grpc.unipi.distributed.StockQuote>(
                  this, METHODID_CHAT_STREAMING_GET_LIST_STOCK_QUOTES)))
          .build();
    }
  }

  /**
   */
  public static final class StockQuoteProviderStub extends io.grpc.stub.AbstractAsyncStub<StockQuoteProviderStub> {
    private StockQuoteProviderStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StockQuoteProviderStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StockQuoteProviderStub(channel, callOptions);
    }

    /**
     */
    public void serverSideStreamingGetListStockQuotes(io.grpc.unipi.distributed.Stock request,
        io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.StockQuote> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getServerSideStreamingGetListStockQuotesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.Stock> chatStreamingGetListStockQuotes(
        io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.StockQuote> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getChatStreamingGetListStockQuotesMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class StockQuoteProviderBlockingStub extends io.grpc.stub.AbstractBlockingStub<StockQuoteProviderBlockingStub> {
    private StockQuoteProviderBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StockQuoteProviderBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StockQuoteProviderBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<io.grpc.unipi.distributed.StockQuote> serverSideStreamingGetListStockQuotes(
        io.grpc.unipi.distributed.Stock request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getServerSideStreamingGetListStockQuotesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class StockQuoteProviderFutureStub extends io.grpc.stub.AbstractFutureStub<StockQuoteProviderFutureStub> {
    private StockQuoteProviderFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected StockQuoteProviderFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new StockQuoteProviderFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_SERVER_SIDE_STREAMING_GET_LIST_STOCK_QUOTES = 0;
  private static final int METHODID_CHAT_STREAMING_GET_LIST_STOCK_QUOTES = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final StockQuoteProviderImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(StockQuoteProviderImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SERVER_SIDE_STREAMING_GET_LIST_STOCK_QUOTES:
          serviceImpl.serverSideStreamingGetListStockQuotes((io.grpc.unipi.distributed.Stock) request,
              (io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.StockQuote>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CHAT_STREAMING_GET_LIST_STOCK_QUOTES:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.chatStreamingGetListStockQuotes(
              (io.grpc.stub.StreamObserver<io.grpc.unipi.distributed.StockQuote>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class StockQuoteProviderBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    StockQuoteProviderBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.grpc.unipi.distributed.ApiProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("StockQuoteProvider");
    }
  }

  private static final class StockQuoteProviderFileDescriptorSupplier
      extends StockQuoteProviderBaseDescriptorSupplier {
    StockQuoteProviderFileDescriptorSupplier() {}
  }

  private static final class StockQuoteProviderMethodDescriptorSupplier
      extends StockQuoteProviderBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    StockQuoteProviderMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (StockQuoteProviderGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new StockQuoteProviderFileDescriptorSupplier())
              .addMethod(getServerSideStreamingGetListStockQuotesMethod())
              .addMethod(getChatStreamingGetListStockQuotesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
