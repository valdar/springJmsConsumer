package it.sandrea.tarocchi.springJmsConsumer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConsumerApp {
	
	public static void main(String[] args) {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("application-context.xml","datasource-context.xml");
		
		boolean terminated=false;
		
		while(!terminated){
			try{
				Thread.sleep(2000);
			}catch(InterruptedException e){
				terminated=true;
			}
		}
	}

}
