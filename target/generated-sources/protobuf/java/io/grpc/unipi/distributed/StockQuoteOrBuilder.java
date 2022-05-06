// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: distributed.proto

package io.grpc.unipi.distributed;

public interface StockQuoteOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.example.distributed.StockQuote)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>double price = 1;</code>
   * @return The price.
   */
  double getPrice();

  /**
   * <code>int32 offer_number = 2;</code>
   * @return The offerNumber.
   */
  int getOfferNumber();

  /**
   * <code>string description = 3;</code>
   * @return The description.
   */
  java.lang.String getDescription();
  /**
   * <code>string description = 3;</code>
   * @return The bytes for description.
   */
  com.google.protobuf.ByteString
      getDescriptionBytes();

  /**
   * <code>string ticker_symbol = 4;</code>
   * @return The tickerSymbol.
   */
  java.lang.String getTickerSymbol();
  /**
   * <code>string ticker_symbol = 4;</code>
   * @return The bytes for tickerSymbol.
   */
  com.google.protobuf.ByteString
      getTickerSymbolBytes();

  /**
   * <code>string company_name = 5;</code>
   * @return The companyName.
   */
  java.lang.String getCompanyName();
  /**
   * <code>string company_name = 5;</code>
   * @return The bytes for companyName.
   */
  com.google.protobuf.ByteString
      getCompanyNameBytes();
}
