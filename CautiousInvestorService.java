// camel-k: language=java

import org.apache.camel.builder.RouteBuilder;

public class CautiousInvestorService extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        rest().post("/{currency}").to("direct:post");
        rest().delete("/{currency}").to("direct:delete");

        from("direct:post")
            .log("Asking the internal department to BUY ${header.currency}...")
            .setBody(constant(""));

        from("direct:delete")
            .log("Asking the internal department to SELL ${header.currency}...")
            .setBody(constant(""));

    }
}
