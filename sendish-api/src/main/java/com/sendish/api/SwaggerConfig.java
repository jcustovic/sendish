package com.sendish.api;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import static org.springframework.web.bind.annotation.RequestMethod.TRACE;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.models.dto.ResponseMessage;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.sendish.api.security.userdetails.AuthUser;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class SwaggerConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private ResourceProperties resourceProperties = new ResourceProperties();
    
    private static final String INTERNAL_SERVER_ERROR_MSG = "Ouch! Contact administrator with error msg";
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Integer cachePeriod = this.resourceProperties.getCachePeriod();

        if (!registry.hasMappingForPattern("/swagger-ui/**")) {
            registry.addResourceHandler("/swagger-ui/**")
                    .addResourceLocations("classpath:/resources/swagger-ui/")
                    .setCachePeriod(cachePeriod);
        }
    }
    
	@Configuration
	@EnableSwagger
	protected static class SpringSwaggerConfigurer {
		
		@Autowired
	    private SpringSwaggerConfig springSwaggerConfig;
		
		 @Bean
		    public SwaggerSpringMvcPlugin apiV1Implementation() {
		        springSwaggerConfig.defaultIgnorableParameterTypes().add(AuthUser.class);
		        springSwaggerConfig.defaultIgnorableParameterTypes().add(WebRequest.class);

		        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
		                .includePatterns("/api/v1.0/.*")
		                .swaggerGroup("v1.0")
		                .apiInfo(v1ApiInfo())
		                .useDefaultResponseMessages(true);
		    }

		    private ApiInfo v1ApiInfo() {
		        return new ApiInfo(
		                "Sendish API",
		                "REST API to connect to backend system",
		                null, // "My Apps API terms of service"
		                "jan@sendish.com",
		                "© Sendish.com",
		                null // "My Apps API License URL"
		        );
		    }

		    @Bean
		    public SwaggerSpringMvcPlugin adminImplementation() {
		        springSwaggerConfig.defaultIgnorableParameterTypes().add(AuthUser.class);
		        springSwaggerConfig.defaultIgnorableParameterTypes().add(WebRequest.class);

		        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
		                .includePatterns("/api/admin/.*")
		                .swaggerGroup("admin")
		                .apiInfo(adminApiInfo())
		                .useDefaultResponseMessages(true);
		    }

		    private ApiInfo adminApiInfo() {
		        return new ApiInfo(
		                "Sendish Admin API",
		                "Admin stuff",
		                null, // "My Apps API terms of service"
		                "jan@sendish.com",
		                "© Sendish.com",
		                null // "My Apps API License URL"
		        );
		    }
		    
		    @Bean
		    public Map<RequestMethod, List<ResponseMessage>> defaultResponseMessages() {
		    	LinkedHashMap<RequestMethod, List<ResponseMessage>> responses = newLinkedHashMap();
		        
				responses.put(GET, asList(
		            new ResponseMessage(OK.value(), OK.getReasonPhrase(), null),
		            new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
		            new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
			    ));

		        responses.put(PUT, asList(
		            new ResponseMessage(NOT_FOUND.value(), NOT_FOUND.getReasonPhrase(), null),
		            new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
		            new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
		        ));

		        responses.put(POST, asList(
		            new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
		            new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
		        ));

		        responses.put(DELETE, asList(
			        new ResponseMessage(NO_CONTENT.value(), NO_CONTENT.getReasonPhrase(), null),
			        new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
			        new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
		        ));

		        responses.put(PATCH, asList(
		            new ResponseMessage(NO_CONTENT.value(), NO_CONTENT.getReasonPhrase(), null),
		            new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
		            new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
		        ));

		        responses.put(TRACE, asList(
		            new ResponseMessage(NO_CONTENT.value(), NO_CONTENT.getReasonPhrase(), null),
		            new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
		            new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
		        ));

		        responses.put(OPTIONS, asList(
		            new ResponseMessage(NO_CONTENT.value(), NO_CONTENT.getReasonPhrase(), null),
		            new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
		            new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
		        ));
		        
		        responses.put(HEAD, asList(
		            new ResponseMessage(NO_CONTENT.value(), NO_CONTENT.getReasonPhrase(), null),
		            new ResponseMessage(UNAUTHORIZED.value(), UNAUTHORIZED.getReasonPhrase(), null),
		            new ResponseMessage(INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR_MSG, null)
		        ));

		    	return responses;
		    }

		    private List<ResponseMessage> asList(ResponseMessage... responseMessages) {
				List<ResponseMessage> list = new ArrayList<>();
				for (ResponseMessage responseMessage : responseMessages) {
					list.add(responseMessage);
				}

				return list;
			}
		    
	}

}
