package test;

// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class MarketSourceMock extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      from("timer:update?period=10000")
        .bean(this, "generate")
        .marshal().json()
        .log("Sending Mock BTC/USDT data to the broker: ${body}")
        .to("knative:event/market.btc.usdt");

  }

  private int counter;

  public MockData generate() {
    boolean high = (this.counter++) % 2 == 0;
    if (high) {
      return new MockData(20.0);
    }
    return new MockData(10.0);
  }

  static class MockData {

    private double last;

    public MockData(double last) {
      this.last = last;
    }

    public double getLast() {
      return last;
    }

    @Override
    public String toString() {
      return "" + last;
    }
  }

}
