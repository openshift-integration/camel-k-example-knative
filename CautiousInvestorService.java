// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class CautiousInvestorService extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        rest()
            .post("/{currency}").to("direct:buy")
            .delete("/{currency}").to("direct:sell");

        from("direct:buy")
            .log("Asking the internal department to BUY ${header.currency}...")
            .setBody(constant(""));

        from("direct:sell")
            .log("Asking the internal department to SELL ${header.currency}...")
            .setBody(constant(""));
    }
}
