package documentation.single.swagger;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.cloud.client.discovery.DiscoveryClient;
//import org.springframework.cloud.netflix.zuul.filters.ProxyRouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Component
@Primary
@EnableAutoConfiguration
public class GatewaySwaggerResourceProvider implements SwaggerResourcesProvider {

    @Autowired
    private SwaggerServicesConfig swaggerServiceList;

	@Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();

        swaggerServiceList.getServices().forEach(service -> {
        	resources.add(swaggerResource(service.getName(),service.getUrl(), service.getVersion()));
        });

        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;	
    }
    

}