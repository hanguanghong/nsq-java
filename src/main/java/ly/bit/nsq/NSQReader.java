package ly.bit.nsq;

import java.util.concurrent.ThreadPoolExecutor;

import ly.bit.nsq.exceptions.NSQException;


public abstract class NSQReader {
	
	protected int requeueDelay;
	protected int maxRetries;
	
	protected ThreadPoolExecutor executor;
	
	protected abstract Runnable makeRunnableFromMessage(Message msg);
		
	public void addMessageForProcessing(Message msg){
		this.executor.execute(this.makeRunnableFromMessage(msg));
	}
	
	public void requeueMessage(Message msg, boolean doDelay){
		if(msg.getAttempts() > this.maxRetries){
			// TODO log giving up
			this.finishMessage(msg);
			return;
		}else{
			int newDelay = doDelay ? 0 : this.requeueDelay * msg.getAttempts();
			try {
				msg.getConn().send(ConnectionUtils.requeue(msg.getId(), newDelay));
			} catch (NSQException e) {
				// TODO kill the connection
			}
		}
	}
	
	public void finishMessage(Message msg){
		try {
			msg.getConn().send(ConnectionUtils.finish(msg.getId()));
		} catch (NSQException e) {
			// TODO kill the connection
		}
	}

}