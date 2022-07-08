package com.wiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class SpringbootDevApplication {

	@PostConstruct
	public void init(){
		// 解决netty启动冲突问题
		//see Netty4Utils.setAvailableProcessor()
		System.setProperty("es.set.netty.runtime.available.processors","false");
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringbootDevApplication.class, args);
	}

}
