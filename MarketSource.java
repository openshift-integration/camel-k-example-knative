// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class MarketSource extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      from("timer:update?period=10000")
        .to("xchange:binance?service=marketdata&method=ticker&currencyPair=BTC/USDT")
        .marshal().json()
        .log("Sending BTC/USDT data to the broker: ${body}")
        .to("knative:event/market.btc.usdt");

  }
}
