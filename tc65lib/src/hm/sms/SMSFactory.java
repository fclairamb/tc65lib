/*
 * SMSFactory.java
 *
 * Created on 14 Декабрь 2009 г., 15:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package hm.sms;

//#if sdkns == "siemens"
import com.siemens.icm.io.*;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.*;
//#endif


import java.util.Random;
import org.javacint.at.ATCommands;
import org.javacint.common.ExceptionHandler;
import org.javacint.logging.Logger;
import org.javacint.utilities.Hex;
import org.javacint.utilities.Utilities;

/**
 * Class that creates and sends an SMS
 */
public class SMSFactory {

//#if DebugLevel=="debug"
	private static final boolean DEBUG = true;
//#elif DebugLevel=="warn" || DebugLevel=="fatal"
//# private static final boolean DEBUG = false;
//#endif
	private static final int WAIT_TIME = 200;
	private static final int MAXIMUM_DATA_SMS_LENGTH = 140;
	private static final int MAXIMUM_UCS2_SMS_LENGTH = 70;
	private static final int MAXIMUM_GSM_SMS_LENGTH = 160;
	public final static String BLAH_OPEN = "<BLAH>";
	public final static String BLAH_CLOSE = "</BLAH>";
	public final static String FAIL_OPEN = "<FAIL>";
	public final static String FAIL_CLOSE = "</FAIL>";
	public final static String OK_OPEN = "<OK>";
	public final static String OK_CLOSE = "</OK>";
	public final static String WARNING_OPEN = "<WARNING>";
	public final static String WARNING_CLOSE = "</WARNING>";
	// <Singleton pattern>
	private static SMSFactory instance;

	public static synchronized SMSFactory getInstance() {
		if (instance == null) {
			instance = new SMSFactory();
		}
		return instance;
	}
	// </Singleton pattern>
	private int smsSequenceNumber;

	private SMSFactory() {
		if (DEBUG) {
			Logger.log("\n\n\n<Constructing SMS>");
		}
		smsSequenceNumber = new Random().nextInt(256);

		boolean ok = false;

		do {
			try {
				//setting SMS mode
				ok = ATCommands.send("AT+CSMS=1").indexOf("OK") >= 0;
				if (DEBUG) {
					Logger.log("setting SMS mode to new standart... " + ok);
				}

				//prefer CSD bearer when sending SMSs
				ok &= ATCommands.send("AT+CGSMS=3").indexOf("OK") >= 0;
				if (DEBUG) {
					Logger.log("prefer CSD bearer when sending SMSs... " + ok);
				}

				//return error when sending sms fail
				ok &= ATCommands.send("AT^SM20=1,0").indexOf("OK") >= 0;
				if (DEBUG) {
					Logger.log("enable error when sending sms fail... " + ok);
				}

				if (DEBUG) {
					// if initialization was successful
					if (ok) {
						Logger.log("SMS Init done");
						// if initialization was not successful
					} else {
						Logger.log("Error");
					}
				}

				String smsFULL = ATCommands.send("AT^SMGO?");
				int a = smsFULL.indexOf("^SMGO:");
				if (a >= 0) {
					a = smsFULL.indexOf(',', a);
					a++;
					smsFULL = smsFULL.substring(a, a + 1);
					if (smsFULL.equals("1") || smsFULL.equals("2")) {
						if (DEBUG) {
							Logger.log("SMS storage is full - time to clean up!");
						}
						int i = 1;
						while (ATCommands.send("AT+CMGD=" + i).indexOf("OK") >= 0) {
							if (DEBUG) {
								Logger.log("Deleting SMS at index " + i);
							}
							i++;
						}
					}
				} else {
					if (DEBUG) {
						Logger.log(FAIL_OPEN + "CANNOT CHECK IF SMS STORAGE IS FULL" + FAIL_CLOSE);
					}
				}
			} catch (Exception ex) {
				ok = false;
				processException(ex, 113);
			}
		} while (!ok);
		if (DEBUG) {
			Logger.log("</Constructing SMS>\n\n\n");
		}
	}

	private void setCodePage(boolean isUCS2) throws ATCommandFailedException {
		if (isUCS2) {
			ATCommands.send("AT+CSCS=\"UCS2\"").indexOf("OK");
		} else {
			ATCommands.send("AT+CSCS=\"GSM\"").indexOf("OK");
		}
	}

	private synchronized String sendPDU(String PDU, boolean isUCS2) throws ATCommandFailedException {
		String response;
		try {
			Thread.sleep(WAIT_TIME);
		} catch (InterruptedException ex) {
			if (DEBUG) {
				ex.printStackTrace();
			}
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////THIS IS CRITICAL AREA//////////////////////////////////////////////
		//!!!!!!!!!WARNING!!!!!!!!!!WARNING!!!!!!!!!WARNING!!!!!!!!!WARNING!!!!!!!!!WARNING!!!!!!!!!WARNING!!!!!!!!!!
		//All other calls anywhere else in the project to this atCommand should also be synchronized for this to work
		///////////////Otherwise, there is probability of SMS not being sent or stuck in the process/////////////////
		synchronized (ATCommands.getSyncObject()) {
			setCodePage(isUCS2);
			response = ATCommands.send("AT+CMGS=" + (PDU.length() / 2 - 1));
			if (DEBUG) {
				Logger.log(response);
			}
			if (response.indexOf('>') >= 0) {
				try {
					Thread.sleep(WAIT_TIME);
				} catch (InterruptedException ex) {
					if (DEBUG) {
						ex.printStackTrace();
					}
				}
				response = ATCommands.send(PDU + "\032");
			}
		}
		/////////////////////////////////////////CRITICAL AREA ENDS//////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		return response;
	}

	/**
	 * Sends text via one or more SMS
	 *
	 * @param address recepient's phone number
	 * @param text of the message, can be in any language
	 * @param allowLong true if allow concatenated SMS's, if false will cut
	 * excess text
	 * @param isUCS2 true if allow international characters, and one SMS will
	 * contain only 70 characters maximum, if false will convert to latin
	 * characters and encode to GSM 7-bit encoding resulting in 160 characters
	 * maximum
	 * @return String containing all warning and errors that occured while
	 * trying to send SMS
	 */
	public String SendText(String address, String text, boolean allowLong, boolean isUCS2) {

		if (address.indexOf('#') >= 0) {
			return WARNING_OPEN + "Try to send SMS to incorrect address " + address + WARNING_CLOSE;
		}
		String result = "";
		String text_remains = "";
		boolean onesms = true;

		if (DEBUG) {
			Logger.log("Trying to send SMS: ");
		}

		if (!isUCS2) {
			text = javaToGSM(text);
		}

		if ((text.length() > MAXIMUM_GSM_SMS_LENGTH && !isUCS2) || (text.length() > MAXIMUM_UCS2_SMS_LENGTH && isUCS2)) {
			if (allowLong) {
				onesms = false;
			} else {
				result += WARNING_OPEN + "Too long text!" + WARNING_CLOSE;
				if (DEBUG) {
					Logger.log(result);
				}
				if (isUCS2) {
					text_remains = text.substring(MAXIMUM_UCS2_SMS_LENGTH);
					text = text.substring(0, MAXIMUM_UCS2_SMS_LENGTH);
				} else {
					text_remains = text.substring(MAXIMUM_GSM_SMS_LENGTH);
					text = text.substring(0, MAXIMUM_GSM_SMS_LENGTH);
				}
			}
		}

		try {
			if (onesms) {
				String PDUText;
				String response;
				if (isUCS2) {
					PDUText = MakePDUTextUCS2(address, text);
				} else {
					PDUText = MakePDUTextGSM(address, text);
				}
				response = sendPDU(PDUText, isUCS2);
				if (response.indexOf("OK") < 0) {
					result += FAIL_OPEN + "Error during sending SMS: " + response + FAIL_CLOSE;
					if (DEBUG) {
						Logger.log(result);
					}
					return result;
				}
			} else {
				String[] PDUText;
				String response;
				if (isUCS2) {
					PDUText = MakeMultiPDUTextUCS2(address, text);
				} else {
					PDUText = MakeMultiPDUTextGSM(address, text);
				}
				for (int i = 0; i < PDUText.length; i++) {
					response = sendPDU(PDUText[i], isUCS2);
					if (response.indexOf("OK") <= 0) {
						result += "<FAIL>Error during sending SMS: " + response + "</FAIL>";
						if (DEBUG) {
							Logger.log(result);
						}
						return result;
					}
				}
			}
			result += BLAH_OPEN + "SMS with text:\n\"" + text + (text_remains.length() > 0 ? '(' + text_remains + ')' : "") + "\"\nwas sent to the address " + address + BLAH_CLOSE;
		} catch (Exception e) {
			processException(e, 241);
			result += FAIL_OPEN + "Exception while sending SMS! " + e.getMessage() + FAIL_CLOSE;
			if (DEBUG) {
				Logger.log(result);
			}
			return result;
		}
		return result;
	}

	public void SendData(String num, byte[] data) {

		if (DEBUG) {
			Logger.log("Trying to send SMS: ");
		}

		if (data.length > MAXIMUM_DATA_SMS_LENGTH) {
			if (DEBUG) {
				Logger.log("ERROR: too long data array");
			}
			return;
		}

		boolean ok = true;
		do {
			try {
				String PDUData = MakePDUData(num, data);
				ATCommands.send("AT+CMGS=" + (PDUData.length() / 2 - 1));
				Thread.sleep(WAIT_TIME);
				ATCommands.send(PDUData + "\032");
				Thread.sleep(WAIT_TIME);
			} catch (Exception e) {
				ok = false;
				processException(e, 274);
			}
		} while (!ok);
	}

	private String MakePDUTextUCS2(String num, String msg) {
		return "000100" + Hex.intToHexFixedWidth(num.length() - 1, 2) + "91" + AddrToPDU(num) + "00" + "08" + Hex.intToHexFixedWidth(msg.length() * 2, 2) + ATStringConverter.Java2UCS2Hex(msg);
	}

	private String[] MakeMultiPDUTextUCS2(String num, String msg) {
		int parts = msg.length() / 67 + (msg.length() % 67 == 0 ? 0 : 1);
		String[] result = new String[parts];
		int seqNum = getSequenceNumber();
		for (int i = 0; i < parts; i++) {
			String msgpart;
			if (i == parts - 1) {
				msgpart = msg.substring(i * 67);
			} else {
				msgpart = msg.substring(i * 67, (i + 1) * 67);
			}
			result[i] = "004100" + Hex.intToHexFixedWidth(num.length() - 1, 2) + "91" + AddrToPDU(num) + "00" + "08" + Hex.intToHexFixedWidth(msgpart.length() * 2 + 6, 2) + "050003" + Hex.intToHexFixedWidth(seqNum, 2) + Hex.intToHexFixedWidth(parts, 2) + Hex.intToHexFixedWidth(i + 1, 2) + ATStringConverter.Java2UCS2Hex(msgpart);
		}
		return result;
	}

	private String MakePDUTextGSM(String num, String msg) {
		String hexSeptets = gsmToSeptetsToHex(msg, null);
		return "000100" + Hex.intToHexFixedWidth(num.length() - 1, 2) + "91" + AddrToPDU(num) + "00" + "00" + Hex.intToHexFixedWidth(msg.length(), 2) + hexSeptets;
	}

	private String[] MakeMultiPDUTextGSM(String num, String msg) {
		final int UDH_LENGTH_IN_SEPTETS = 7;

		int parts = (msg.length() / (160 - UDH_LENGTH_IN_SEPTETS)) + (msg.length() % (160 - UDH_LENGTH_IN_SEPTETS) == 0 ? 0 : 1);
		String[] result = new String[parts];
		int seqNum = getSequenceNumber();
		for (int i = 0; i < parts; i++) {
			String msgpart;
			if (i == parts - 1) {
				msgpart = msg.substring(i * (160 - UDH_LENGTH_IN_SEPTETS));
			} else {
				msgpart = msg.substring(i * (160 - UDH_LENGTH_IN_SEPTETS), (i + 1) * (160 - UDH_LENGTH_IN_SEPTETS));
			}
			String UDH = "050003" + Hex.intToHexFixedWidth(seqNum, 2) + Hex.intToHexFixedWidth(parts, 2) + Hex.intToHexFixedWidth(i + 1, 2);
			String hexSeptets = gsmToSeptetsToHex(msgpart, UDH);
			result[i] = "004100" + Hex.intToHexFixedWidth(num.length() - 1, 2) + "91" + AddrToPDU(num) + "00" + "00" + Hex.intToHexFixedWidth(UDH_LENGTH_IN_SEPTETS + msgpart.length(), 2) + hexSeptets;
		}
		return result;
	}

	private String javaToGSM(String text) {
		if (DEBUG) {
			Logger.log("Trying to convert: " + text);
		}
		text = Utilities.fromRussianToTranslit(text);   //Could be deleted from here, 
		if (DEBUG) {                                    //but I've decided to keep it 
			Logger.log("To translit: " + text);        //so that in future there could be 
		}                                               //implemented translits for other languages
		StringBuffer gsmString = new StringBuffer();
		for (int i = 0; i < text.length(); i++) {
			try {
				gsmString.append(ATStringConverter.Java2GSM(text.substring(i, i + 1)));
			} catch (IllegalArgumentException illegalArgumentException) {
				String character = text.substring(i, i + 1);
				if (DEBUG) {
					Logger.log("Cannot convert character: " + character);
				}
				gsmString.append(character);
			}
		}
		if (DEBUG) {
			Logger.log("text.length=" + text.length() + ", gsmString.length=" + gsmString.length());
		}
		return gsmString.toString();
	}

	private String gsmToSeptetsToHex(String gsmString, String UDH) {
		if (DEBUG) {
			Logger.log("Trying to convert gsmToSeptetsToHex: gsmString=" + gsmString + ", UDH=" + UDH);
		}
		int skipBits = 0;
		if (UDH != null) {
			if (UDH.length() != 0) {
				int udhLength = UDH.length() / 2;
				skipBits = ((udhLength + 1) * 8) + ((7 - ((udhLength + 1) * 8) % 7) % 7); //Number of bits to skip from beginning
			}
		}
		if (DEBUG) {
			Logger.log("skipBits=" + skipBits);
		}
		boolean[] udBool = new boolean[skipBits + gsmString.length() * 7 + ((8 - ((skipBits + gsmString.length() * 7) % 8)) % 8)]; //Bits array of final data
		if (DEBUG) {
			Logger.log("gsmString.length()=" + gsmString.length() + "; udBool[].length=" + udBool.length);
		}
		if (UDH != null) {
			if (UDH.length() != 0) {
				for (int i = 0; i < UDH.length() / 2; i++) { //Fill the first bits of udBool with UDH bits
					boolean[] t = Utilities.intToBinaryArrayFixedWidth(Integer.parseInt(UDH.substring(i * 2, (i + 1) * 2), 16), 8);
					System.arraycopy(t, 0, udBool, i * 8, 8);
				}
			}
		}
		for (int i = 0; i < gsmString.length(); i++) { //Fill remaining bits of udBool with text septets bits
			boolean[] t = Utilities.intToBinaryArrayFixedWidth(gsmString.charAt(i), 7);
			//<DEBUG>
			String boolArrToString = "";
			for (int j = 0; j < t.length; j++) {
				boolArrToString += (t[j] == true ? '1' : '0');
			}
			if (DEBUG) {
				Logger.log("t=" + boolArrToString);
			}
			//</DEBUG>
			System.arraycopy(t, 0, udBool, skipBits + (i * 7), 7);
		}
		int length = udBool.length / 8; // length of UD in octets
		if (DEBUG) {
			Logger.log("length=" + length);
		}
		String hexResult = ""; //Hex string containing UD
		for (int i = 0; i < length; i++) {
			boolean[] t = new boolean[8];
			System.arraycopy(udBool, i * 8, t, 0, 8);
			hexResult += Hex.intToHexFixedWidth(Utilities.binaryArrayToInt(t), 2);
		}
		if (DEBUG) {
			Logger.log("hexResult=" + hexResult);
		}
		return hexResult;
	}

	private String MakePDUData(String num, byte[] data) {
		String data_string = new String();
		for (int i = 0; i < data.length; i++) {
			data_string += Hex.intToHexFixedWidth((int) data[i], 2);
		}
		return "000100" + Hex.intToHexFixedWidth(num.length() - 1, 2) + "91" + AddrToPDU(num) + "00" + "04" + Hex.intToHexFixedWidth(data.length, 2) + data_string;
	}

	private String AddrToPDU(String s) {
		String r = new String();
		int i = 0;
		s = s.substring(1, s.length());
		if (s.length() % 2 > 0) {
			s = s + 'F';
		}
		while (i < s.length()) {
			r = r + s.charAt(i + 1) + s.charAt(i);
			i = i + 2;
		}
		return r;
	}                       //Converts phone number from +71234567890 format to PDU format 1732547698F0

	private int getSequenceNumber() {
		smsSequenceNumber++;
		smsSequenceNumber = smsSequenceNumber % 256;
		return smsSequenceNumber;
	}

	private void processException(Exception ex, int line) {
		ExceptionHandler.processException(this, ex, line, null);
	}
}
