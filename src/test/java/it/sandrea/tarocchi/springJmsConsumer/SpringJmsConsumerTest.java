package it.sandrea.tarocchi.springJmsConsumer;


import java.io.File;
import java.io.FileFilter;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.sql.DataSource;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.AbstractJmsListeningContainer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
	@ContextConfiguration("classpath:datasource-test-context.xml"),
	@ContextConfiguration("classpath:application-context.xml")
})
public class SpringJmsConsumerTest {
	@Autowired
    private ConfigurableApplicationContext context;
	@Autowired
	private DataSource ds;
	@Autowired
	private JmsTemplate jmsT;
	@Resource
	private AbstractJmsListeningContainer simpleMessageListenerContainer;
	
	@Test
	public void testIdController() throws Exception {
		simpleMessageListenerContainer.stop();
		
		for(int i=0; i<200; i++){
			final String id = String.valueOf(i);
			jmsT.send("queue:toBeProcessed.messages", new MessageCreator() {

				@Override
				public Message createMessage(Session session) throws JMSException {

					return session.createTextMessage(id);
				}
			});
		}
		
		simpleMessageListenerContainer.start();
		
		Thread.sleep(20000);
		
		int processedMessages = new JdbcTemplate(ds).queryForList("SELECT * FROM messages_sent").size();
		System.out.println("Processed messages :"+processedMessages);
		Assert.assertTrue(processedMessages<=100);
	}

	@Before
	public void setUp(){
		new JdbcTemplate(ds).execute("CREATE TABLE messages_sent (id BIGINT NOT NULL, date_time TIMESTAMP, PRIMARY KEY(id))");
	}

	@After
	public void tearDown() throws InterruptedException{
		new JdbcTemplate(ds).execute("DROP TABLE messages_sent");
		if(context != null && context.isActive()){  
			context.close();
		}
		deleteDirectory("activemq-data");
		deleteDirectory("id_file");
		deleteAllMatchingfilesIndir(".", "socks*.trc");
		deleteAllMatchingfilesIndir(".", "*.log");
		deleteAllMatchingfilesIndir(".", "*.epoch");
	}

	private void deleteDirectory(String file) {
		deleteDirectory(new File(file));
	}

	private void deleteDirectory(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteDirectory(files[i]);
			}
		}
		file.delete();
	}

	private void deleteAllMatchingfilesIndir(String directory, String wildcardFileMatcher){
		File dir = new File(directory);
		FileFilter fileFilter = new WildcardFileFilter(wildcardFileMatcher);
		File[] files = dir.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].delete());
		}
	}

}
