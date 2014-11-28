package com.project.nfv;

import com.project.nfv.core.Saying;
import com.google.common.base.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.testing.junit.DropwizardAppRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.ClassRule;
import org.junit.Test;


public class IntegrationTest {
	@ClassRule
	public static final DropwizardAppRule<VpcConfiguration> RULE =
		new DropwizardAppRule<VpcConfiguration>(VpcApplication.class, "nfv.yml");
	
	@Test
	public void testHelloWorld() throws Exception {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:" + RULE.getLocalPort() + "/vpc");
		
		final Optional<String> NAME = Optional.fromNullable("Dr. IntegrationTest");
		
		Saying saying = target.queryParam("name", NAME.get()).request().get(Saying.class);
		
		assertThat(saying.getContent()).isEqualTo(RULE.getConfiguration().buildTemplate().render(NAME));
	}
}
