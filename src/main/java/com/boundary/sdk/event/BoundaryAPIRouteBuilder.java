package com.boundary.sdk.event;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.AuthMethod;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.component.http.HttpConfiguration;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RouteBuilder} for sending events to Boundary. Accepts serialized requests
 * to invoked the Boundary API
 *  
 * @author davidg
 *
 */
public class BoundaryAPIRouteBuilder extends BoundaryRouteBuilder {
	
	private static Logger LOG = LoggerFactory.getLogger(BoundaryAPIRouteBuilder.class);

	private String apiHost;
	private String orgId;
	private String apiKey;

	/**
	 * Default constructor
	 */
	public BoundaryAPIRouteBuilder() {
		this.apiHost = "api.boundary.com";
		this.orgId = "";
		this.apiKey = "";
		this.fromUri = "direct:boundary-event";
	}
		
	/**
	 * Set the Boundary Organization ID to use by default
	 * 
	 * @param orgId Organization Id from the Boundary console.
	 */
	
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	
	/**
	 * Set the Boundary authentication key
	 * 
	 * @param apiKey API key from the Boundary Console
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	/**
	 * Current value of the API key
	 * 
	 * @return {@link String}
	 */
	public String getApiKey() {
		return this.apiKey;
	}
		
	/**
	 * Set the host to use for sending Boundary API requests
	 * 
	 * @param apiHost Host where the Boundary API is running
	 */
	public void setApiHost(String apiHost) {
		this.apiHost = apiHost;
	}
	
	/**
	 * Return the current Boundary API host
	 * 
	 * @return {@link String}
	 */
	public String getApiHost() {
		return this.apiHost;
	}
		
	
	/**
	 * Configures the Camel route that receives {@link RawEvent}
	 * and then sends to the Boundary API
	 * 
	 */
	@Override
	public void configure() {
		
		// Create the URL used to send events
		String url = "https://" + apiHost + "/" + orgId + "/" + "events";
		
		LOG.info("boundary event api url: " + url);
		
		// Configure our HTTP connection to use BASIC authentication
		HttpConfiguration config = new HttpConfiguration();
		config.setAuthMethod(AuthMethod.Basic);
		config.setAuthUsername(this.apiKey);
		config.setAuthPassword("");
		
		HttpComponent http = this.getContext().getComponent("https",HttpComponent.class);
		http.setHttpConfiguration(config);
		
		from(fromUri)
			.startupOrder(startUpOrder)
			.routeId(routeId)
			.unmarshal().serialization()
			.marshal().json(JsonLibrary.Jackson)
			.to("log:com.boundary.sdk.BoundaryEventRoute?level=DEBUG&showBody=true")
			.setHeader(Exchange.ACCEPT_CONTENT_TYPE, constant("application/json"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
			.setHeader(Exchange.HTTP_METHOD, constant("POST"))
			//.setHeader(name, expression)
			.to("log:com.boundary.sdk.event.BoundaryEventRouteBuilder?level=DEBUG&groupInterval=60000&groupDelay=60000&groupActiveOnly=false")
			.to(url.toString())
			.to("log:com.boundary.sdk.event.BoundaryEventRouteBuilder?level=INFO&showHeaders=true&multiline=true")
			;
	}
}
