// camel-k: language=java trait=knative-service.min-scale=1 trait=knative-service.max-scale=1 source=SimpleAlgorithm.java

import org.apache.camel.builder.RouteBuilder;

public class Predictor extends RouteBuilder {

  @Override
  public void configure() throws Exception {

      from("knative:event/market.btc.usdt")
        .unmarshal().json()
        .transform().simple("${body[last]}")
        .log("Latest value for BTC/USDT is: ${body}")
        .to("seda:evaluate")
        .setBody().constant("");

      from("seda:evaluate")
        .bean("algorithm")
        .choice()
          .when(body().isNotNull())
            .log("Predicted action: ${body}")
            .to("direct:publish");

      from("direct:publish")
        .marshal().json()
        .removeHeaders("*")
        .to("knative:event/prediction.btc.usdt");

  }

}
