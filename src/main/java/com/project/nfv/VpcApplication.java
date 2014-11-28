package com.project.nfv;

import com.project.nfv.auth.VpcAuthenticator;
import com.project.nfv.cli.RenderCommand;
import com.project.nfv.core.Person;
import com.project.nfv.core.Template;
import com.project.nfv.core.User;
import com.project.nfv.db.PersonDAO;
import com.project.nfv.filter.DateRequiredFeature;
import com.project.nfv.health.TemplateHealthCheck;
import com.project.nfv.resources.FilteredResource;
import com.project.nfv.resources.VpcResource;
import com.project.nfv.resources.PeopleResource;
import com.project.nfv.resources.PersonResource;
import com.project.nfv.resources.ProtectedResource;
import com.project.nfv.resources.ViewResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class VpcApplication extends Application<VpcConfiguration> {
    public static void main(String[] args) throws Exception {
        new VpcApplication().run(args);
    }

    private final HibernateBundle<VpcConfiguration> hibernateBundle =
            new HibernateBundle<VpcConfiguration>(Person.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(VpcConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<VpcConfiguration> bootstrap) {
        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<VpcConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(VpcConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(VpcConfiguration configuration, Environment environment) {
        final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.healthChecks().register("template", new TemplateHealthCheck(template));
        environment.jersey().register(DateRequiredFeature.class);

        environment.jersey().register(AuthFactory.binder(new BasicAuthFactory<>(new VpcAuthenticator(),
                                                                 "SUPER SECRET STUFF",
                                                                 User.class)));
        environment.jersey().register(new VpcResource(template));
        environment.jersey().register(new ViewResource());
        environment.jersey().register(new ProtectedResource());
        environment.jersey().register(new PeopleResource(dao));
        environment.jersey().register(new PersonResource(dao));
        environment.jersey().register(new FilteredResource());
    }
}
