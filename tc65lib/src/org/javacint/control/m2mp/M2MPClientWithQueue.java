package org.javacint.control.m2mp;

import java.io.IOException;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;
import org.javacint.common.Base64;
import org.javacint.common.safequeue.SafeQueue;
import org.javacint.common.safequeue.SafeQueueLineReader;
import org.javacint.logging.Logger;
import org.javacint.settings.Settings;
import org.javacint.task.Timers;

/**
 *
 * @author Florent Clairambault / www.webingenia.com
 */
public class M2MPClientWithQueue extends M2MPClientImpl {

	private final Vector queue = new Vector();
	private final SafeQueue safeQueue = new SafeQueue("m2mp");
	private int dataSent = 0;
	private static final int LIMIT_BEFORE_ACK = 10;
	private long stateStartedTime = System.currentTimeMillis();
	private byte currentState;
	private static final byte STATE_SENDING_RT = 0;
//	private static final long TIME_SENDING_RT = 7200000;
	private static final byte STATE_SENDING_QUEUE = 1;
	private static final long TIME_SENDING_QUEUE = 60000;
	private static final byte STATE_WAITING_FOR_ACK_RT = 2;
	private static final byte STATE_WAITING_FOR_ACK_QUEUE = 3;
	private static final long TIME_WAITING_FOR_ACK = 30000;
	private static final String SETTING_M2MP_LOGQUEUE = "m2mp.log.queue";
	private boolean logQueue = false;

	protected void parseSetting(String settingName) {
		super.parseSetting(settingName);
		if (SETTING_M2MP_LOGQUEUE.equals(settingName)) {
			logQueue = Settings.getBool(settingName);
		}
	}

	public void getDefaultSettings(Hashtable settings) {
		super.getDefaultSettings(settings);
		settings.put(SETTING_M2MP_LOGQUEUE, "0");
	}

	private static String stateToString(byte state) {
		switch (state) {
			case STATE_SENDING_RT:
				return "SENDING_RT";
			case STATE_SENDING_QUEUE:
				return "SENDING_QUEUE";
			case STATE_WAITING_FOR_ACK_RT:
				return "WAITING_ACK_RT";
			case STATE_WAITING_FOR_ACK_QUEUE:
				return "WAITING_ACK_QUEUE";
		}
		return null;
	}

	private void workSendingRt() {
		while (!queue.isEmpty()) {
			// We get the first element to send
			Object obj = queue.firstElement();
			queue.removeElementAt(0);
			if (obj instanceof NamedData) {
				NamedData nd = (NamedData) obj;
				sendData(nd.getChannelName(), nd.getData());
				safeQueue.addLine(nd.getChannelName() + "," + Base64.encode(nd.getData()));
			} else if (obj instanceof NamedArrayData) {
				NamedArrayData nad = (NamedArrayData) obj;
				byte[][] data = nad.getData();
				sendData(nad.getChannelName(), data);
				StringBuffer sb = new StringBuffer();
				sb.append('.');
				sb.append(nad.getChannelName());
				for (int i = 0; i < data.length; i++) {
					sb.append(',');
					sb.append(Base64.encode(data[i]));
				}
				safeQueue.addLine(sb.toString());
			}
			if (dataSent++ == LIMIT_BEFORE_ACK) {
				changeState(STATE_WAITING_FOR_ACK_RT);
				sendAckRequest((byte) 4);
				workWaitingForAckRt();
				break;
			}
		}
	}

	private void workWaitingForAckRt() {
		// If we didn't got an ack
		if (currentStateRunningTime() > TIME_WAITING_FOR_ACK) {
			safeQueue.saveMemoryInFile();
			dataSent = 0;

			// We're changing back to sending in real-time
			changeState(STATE_SENDING_RT);
			schedule();
		}
	}
	SafeQueueLineReader sendingQueueReader;

	private void workSendingQueue() {
		if (currentStateRunningTime() > TIME_SENDING_QUEUE) {
			// We're changing back to sending in real-time
			sendingQueueReader = null;
			changeState(STATE_SENDING_RT);
		}

		if (safeQueue.hasData()) {
			if (Logger.BUILD_DEBUG && logQueue) {
				Logger.log(this + ".workSendingQueue: Sending queue !", true);
			}

			sendingQueueReader = safeQueue.getFirstItemsSetWaiting();
			String line;
			while ((line = sendingQueueReader.readLine()) != null) {
				// We currently handle only byte[], to handle array of byte[]
				// we would just need to add commas and an array indicator (like the line starting with ",")
				int p = line.indexOf(',');
				String channelName = line.substring(0, p);
				if (channelName.startsWith(".")) {
					continue;
//					channelName = channelName.substring(1);
				} else {
					byte[] data = Base64.decode(line.substring(p + 1));
					sendData(channelName, data);
				}
			}
			sendAckRequest((byte) 5);
			changeState(STATE_WAITING_FOR_ACK_QUEUE);
		} else {
			changeState(STATE_SENDING_RT);
		}
	}

	private void workWaitingForAckQueue() {
		// If we didn't got an ack
		if (currentStateRunningTime() > TIME_WAITING_FOR_ACK) {
			dataSent = 0;

			// We're changing back to sending in real-time
			changeState(STATE_SENDING_RT);
			schedule();
		}
	}

	private void changeState(byte state) {
		if (Logger.BUILD_DEBUG && logQueue) {
			Logger.log(this + ".changeState( " + stateToString(state) + " );", true);
		}
		stateStartedTime = System.currentTimeMillis();
		currentState = state;
	}

	private long currentStateRunningTime() {
		return System.currentTimeMillis() - stateStartedTime;
	}

	private class NamedData {

		private final String channelName;
		private final byte[] data;

		public NamedData(String channelName, byte[] data) {
			this.channelName = channelName;
			this.data = data;
		}

		public String getChannelName() {
			return channelName;
		}

		public byte[] getData() {
			return data;
		}
	}

	public class NamedArrayData {

		private final String channelName;
		private final byte[][] data;

		public NamedArrayData(String channelName, byte[][] data) {
			this.channelName = channelName;
			this.data = data;
		}

		public String getChannelName() {
			return channelName;
		}

		public byte[][] getData() {
			return data;
		}
	}

	// <editor-fold desc="Send queue">
	private class WorkState extends TimerTask {

		public void run() {
			work();
		}
	}

	private void work() {
		if (Logger.BUILD_DEBUG) {
			Logger.log("currentState=" + stateToString(currentState));
		}
		switch (currentState) {
			case STATE_SENDING_RT:
				workSendingRt();
				break;

			case STATE_WAITING_FOR_ACK_RT:
				workWaitingForAckRt();
				break;

			case STATE_SENDING_QUEUE:
				workSendingQueue();
				break;

			case STATE_WAITING_FOR_ACK_QUEUE:
				workWaitingForAckQueue();
				break;

		}
	}
	// </editor-fold>

	public void start() throws Exception {
		parseSetting(SETTING_M2MP_LOGQUEUE);
		super.start();
	}

	public void addQueuedData(String channelName, String data) {
		this.addQueuedData(channelName, data.getBytes());
	}

	public void addQueuedData(String channelName, byte[] data) {
		queue.addElement(new NamedData(channelName, data));
		schedule();
	}

	public void addQueuedData(String channelName, byte[][] data) {
		queue.addElement(new NamedArrayData(channelName, data));
		schedule();
	}

	private void schedule() {
		Timers.getSlow().schedule(new WorkState(), 0);
	}

	// If we got an ack from the server
	void onReceivedAckResponse(byte b) {
		super.onReceivedAckResponse(b);
		try {
			// We reset the dataSentCounter
			dataSent = 0;

			// And we mark it as switching back to it's original state
			if (currentState == STATE_WAITING_FOR_ACK_RT) {
				if (Logger.BUILD_DEBUG && logQueue) {
					Logger.log(this + ".onReceivedAckResponse: received RT ACK !", true);
				}
				safeQueue.deleteFirstItemsListWaiting();
				changeState(STATE_SENDING_QUEUE);
				schedule();
			} else if (currentState == STATE_WAITING_FOR_ACK_QUEUE) {
				if (Logger.BUILD_DEBUG && logQueue) {
					Logger.log(this + ".onReceivedAckResponse: received QUEUE ACK !", true);
				}
				// We need to to delete the data waiting to be sent
				if (sendingQueueReader != null) {
					sendingQueueReader.delete();
					sendingQueueReader = null;
				}
				changeState(STATE_SENDING_QUEUE);
				schedule();
			}
		} catch (IOException ex) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log(this + ".onReceivedAckResponse", ex, true);
			}
		}
	}

	// If we got disconnected
	void onDisconnected() {
		super.onDisconnected();

		// We need to save the content of the safe queue in files
		safeQueue.saveMemoryInFile();

		dataSent = 0;
		changeState(STATE_SENDING_RT);
	}

	public void stop() {
		safeQueue.saveMemoryInFile();
		super.stop();

	}

	public String toString() {
		return "M2MPClientWithQueue";
	}
}
