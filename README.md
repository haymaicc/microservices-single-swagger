# microservices-single-swagger (aggregate)
Repository to showcase how to create a SpringBoot *Single/Aggregate* SpringFox-Swagger Documentation Server for all your micro services.

### 改成了maven 项目

### Challenge:
   SpringFox-Swagger generates different URL/document for each microservice. There is no way currently to view complete list of micro servies available or the details of all micro services at one place.

### Solution:
'microservices-single-swagger' is a springboot application and creates a documentation server where all the available microservices can be accessed. On the springfox swagger documentation page from this documentation server, use will be able to view all the microservices available (drop down on top) and select any service to view the documentation without leaving the page.

#### Configuration:
In the **application.yaml** file, you can configure all your microservice swagger URLs.

```
documentation: 
  baseurl: http://localhost
  swagger: 
    services:   
      - 
        name: Service1
        url: ${documentation.baseurl}:8040/v2/api-docs?group=service1
        version: 2.0
      - 
        name: Service2
        url: ${documentation.baseurl}:8050/v2/api-docs?group=service2
        version: 2.0
```

Docker file is also available to create an image. 


##### Future enhacements:
* Enhance the application to retrieve the list from API Gateway or Registry (e.g. Eureka or Zuul) so that the list will be dynamic.


##### Remember:
* If you dont use Zuul or Eureka, please remove the dependency on it from Gradile and Application files.
* Cross origin Resource Sharing (CORS) need to be enabled on the micriservices server. Please let me know if you need details on how to configure the same in SpringBoot application.


Thanks to @dilipkrish (SpringFox Member) & @jhipster.

@dilipkrish mentioned that he is planning to add this to springfox swagger demos. So, you might be able to get this directly as part of swagger in future.

### 跨域问题解决
一般来说，解决spring boot跨域网上有很多教程：比如可以：
```java
@Configuration
public class MyConfiguration extends WebMvcConfigurerAdapter {
    	@Override
    	public void addCorsMappings(CorsRegistry registry) {
    		registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE",
    				"OPTIONS");
    	}
}
```
但是这些貌似加了都对swagger /v2/api-docs 这个接口无效，这是为什么呢？
打开Swagger2Controller 这个类我们发现
```java
@Controller
@ApiIgnore
public class Swagger2Controller {
    @RequestMapping(
        value = {"/v2/api-docs"},
        method = {RequestMethod.GET},
        produces = {"application/json", "application/hal+json"}
    )
    @PropertySourcedMapping(
        value = "${springfox.documentation.swagger.v2.path}",
        propertyKey = "springfox.documentation.swagger.v2.path"
    )
    @ResponseBody
    public ResponseEntity<Json> getDocumentation(@RequestParam(value = "group",required = false) String swaggerGroup, HttpServletRequest servletRequest) {
        // ....
    }
}
```
这个接口多了一个PropertySourcedMapping 注解，而我们上述配置的addCorsMappings 只加到了RequestMapping的handler 中。

#### 解决办法 
在swagger 定义的swagger2ControllerMapping Bean 对象中加入我们的跨域配置：
```java
@Component
public class SwaggerPostProccesor implements BeanPostProcessor {

	private static final String SWAGGER_BEAN_NAME = "swagger2ControllerMapping";

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (SWAGGER_BEAN_NAME.equals(beanName)) {
			Map<String, CorsConfiguration> configs = new LinkedHashMap<>();
			CorsConfiguration corsConfiguration = (new CorsConfiguration()).applyPermitDefaultValues();
			corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
			corsConfiguration.setAllowedMethods(ImmutableList.of("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"));
			configs.put("/**", corsConfiguration);
			PropertySourcedRequestMappingHandlerMapping.class.cast(bean).setCorsConfigurations(configs);
		}
		return bean;
	}

}
```