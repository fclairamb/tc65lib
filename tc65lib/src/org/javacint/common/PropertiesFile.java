/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.common;

import com.siemens.icm.io.file.FileConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import org.javacint.logging.Logger;

/**
 * Parameters file.
 *
 * This can be used for monitoring some parameters (not necessarily settings).
 *
 * This should be ported into the Settings class.
 *
 * @author Florent Clairambault 
 */
public class PropertiesFile {

	private static final String PATH_PREFIX = "file:///a:/";
	private final String path;
	private Hashtable data;

	public PropertiesFile(String path) throws IOException {
		this.path = path;
		load();
	}

	public Hashtable getData() {
		return data;
	}

	public void setData(Hashtable data) {
		this.data = data;
	}

	public void set(String name, String value) {
		data.put(name, value);
	}

	public void set(String name, boolean value) {
		data.put(name, value ? "1" : "0");
	}

	public void set(String name, int value) {
		data.put(name, "" + value);
	}

	public void set(String name, long value) {
		data.put(name, "" + value);
	}

	public String get(String name, String defaultValue) {
		String value = (String) data.get(name);
		return value != null ? value : defaultValue;
	}

	public boolean get(String name, boolean defaultValue) {
		String value = (String) data.get(name);
		if ("1".equals(value)) {
			return true;
		} else if ("0".equals(value)) {
			return false;
		} else {
			return defaultValue;
		}
	}

	public int get(String name, int defaultValue) {
		try {
			return Integer.parseInt((String) data.get(name));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public long get(String name, long defaultValue) {
		try {
			return Long.parseLong((String) data.get(name));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	private void load() throws IOException {
		FileConnection fc = getFileConnection();
		data = new Hashtable();
		if (fc.exists()) {
			BufferedReader br = new BufferedReader(fc.openInputStream());
			String line;
			while ((line = br.readLine()) != null) {
				int c = line.indexOf('=');
				String key = line.substring(0, c);
				String value = line.substring(c + 1);
				data.put(key, value);
			}
			br.close();
		}
	}

	public void save() throws IOException {
		try {
			FileConnection fc = (FileConnection) Connector.open(PATH_PREFIX + path, Connector.READ_WRITE);
			if (fc.exists()) {
				fc.delete();
			}
			fc.create();
			OutputStream os = fc.openOutputStream();
			{
				String key;
				for (Enumeration en = data.keys(); en.hasMoreElements();) {
					key = (String) en.nextElement();
					os.write((key + "=" + data.get(key) + "\n").getBytes());
				}
			}
			os.flush();
			os.close();
		} catch (Exception ex) {
			if (Logger.BUILD_CRITICAL) {
				Logger.log(this + ".save", ex, true);
			}
		}
	}

	public String toString() {
		return "ParametersFile{" + path + "}";
	}

	private FileConnection getFileConnection() throws IOException {
		return (FileConnection) Connector.open(PATH_PREFIX + path, Connector.READ);
	}
}
