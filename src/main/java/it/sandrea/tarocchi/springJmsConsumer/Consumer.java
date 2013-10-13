package it.sandrea.tarocchi.springJmsConsumer;

import it.sandrea.tarocchi.springJmsConsumer.exceptions.MaxConsumingMessagesPerTimeExceded;

import java.sql.Types;
import java.util.Calendar;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

public class Consumer implements MessageListener {

	private static final Logger log = Logger.getLogger(Consumer.class.getName());
	private final static String INSERT_QUERY = "INSERT INTO messages_sent (id, date_time) VALUES (?,?)";
	private final static int[] TYPES = new int[] { Types.BIGINT, Types.TIMESTAMP };
	private final static int MAX_MESSAGES = 5;
	private final static long TIME_IN_MILLLISEC = 1000;
	
	private static JmsTemplate jsmT;
	private static DataSource ds;
	
	private long lastMessageRecivedTime = Calendar.getInstance().getTimeInMillis();
	private int messageRecivedCount = 0;
	
	public synchronized void onMessage(Message message) {
		try {
			TextMessage msg = (TextMessage) message;
			long now = Calendar.getInstance().getTimeInMillis();
			
			if(  (now - lastMessageRecivedTime) > TIME_IN_MILLLISEC ){
				//first message in the time unit
				messageRecivedCount = 1;
				lastMessageRecivedTime = now;
			}else{
				if((MAX_MESSAGES - messageRecivedCount)>1){
					//from the second to the before last message in the time unit
					messageRecivedCount++;
					lastMessageRecivedTime = now;
				}else if((MAX_MESSAGES - messageRecivedCount)==1){
					//last message in the time unit
					messageRecivedCount = 0;
					lastMessageRecivedTime = now;
					long sleepingtime = TIME_IN_MILLLISEC-(now-lastMessageRecivedTime);
					Thread.sleep( sleepingtime>0 ? sleepingtime : 0);
				}else{
					log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX UnsupportedOperationException XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");
					throw new MaxConsumingMessagesPerTimeExceded( "MAX_MESSAGES: "+MAX_MESSAGES+" TIME_IN_MILLLISEC: "+TIME_IN_MILLLISEC+" lastMessageRecivedTime: "+lastMessageRecivedTime+" messageRecivedCount: "+messageRecivedCount+" now: "+now );
				}
			}
			String id = msg.getText();
			log.info("recived message whit id: "+id);
			saveMessage(id);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MaxConsumingMessagesPerTimeExceded e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	@Transactional("jdbcTxManager")
	private synchronized int saveMessage(String id){
		Long id_long;
		try{
			id_long = Long.parseLong(id);
			JdbcTemplate jt =  new JdbcTemplate(ds);
			Object[] params = new Object[] { id_long, Calendar.getInstance().getTime() };
			int row = jt.update(INSERT_QUERY, params, TYPES);
			log.info(row + " row inserted whit id: "+id_long);
			return row;
		}catch(NumberFormatException e){
			log.info("not a valid id, (Long parsable String required): "+id);
			return 0;
		}
	}

	public static JmsTemplate getJsmT() {
		return jsmT;
	}

	public static void setJsmT(JmsTemplate jsmT) {
		Consumer.jsmT = jsmT;
	}

	public static DataSource getDs() {
		return ds;
	}

	public static void setDs(DataSource ds) {
		Consumer.ds = ds;
	}
}
