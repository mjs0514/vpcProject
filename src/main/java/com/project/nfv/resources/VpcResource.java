package com.project.nfv.resources;

import com.codahale.metrics.annotation.Timed;
import com.project.nfv.core.Saying;
import com.project.nfv.core.Template;
import com.google.common.base.Optional;
import io.dropwizard.jersey.caching.CacheControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Path("/vpc")
@Produces(MediaType.APPLICATION_JSON)
public class VpcResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(VpcResource.class);

    private final Template template;
    private final AtomicLong counter;

    public VpcResource(Template template) {
        this.template = template;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed(name = "get-requests")
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        return new Saying(counter.incrementAndGet(), template.render(name));
    }

    @POST
    public void receiveHello(@Valid Saying saying) {
        LOGGER.info("Received a saying: {}", saying);
    }
}
