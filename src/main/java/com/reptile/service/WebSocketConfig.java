package com.reptile.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.reptile.Bank.CustomSpringConfigurator;




@Configuration 
public class WebSocketConfig {

	 @Bean  
	   public CustomSpringConfigurator newConfigure (){  
	        return new CustomSpringConfigurator();  
	    }  
}
