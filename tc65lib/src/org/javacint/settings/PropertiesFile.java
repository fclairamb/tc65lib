package org.javacint.settings;

//#if sdkns == "siemens"
import com.siemens.icm.io.file.FileConnection;
//#elif sdkns == "cinterion"
//# import com.cinterion.io.file.FileConnection;
//#endif
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import org.javacint.common.BufferedReader;
import org.javacint.logging.Logger;

/**
 * Parameters file.
 *
 * This can be used for monitoring some parameters (not necessarily settings).
 *
 * This should be ported into the Settings class.
 *
 * @author Florent Clairambault / www.webingenia.com
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

	public String getString(String name, String defaultValue) {
		String value = (String) data.get(name);
		return value != null ? value : defaultValue;
	}

	public void set(String name, int value) {
		data.put(name, "" + value);
	}

	public int getInt(String name, int defaultValue) {
		try {
			return Integer.parseInt((String) data.get(name));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public void set(String name, long value) {
		data.put(name, "" + value);
	}

	public long getLong(String name, int defaultValue) {
		try {
			return Long.parseLong((String) data.get(name));
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public void set(String name, boolean value) {
		data.put(name, value ? "1" : "0");
	}

	public boolean getBool(String name, boolean defaultValue) {
		String value = (String) data.get(name);
		if ("1".equals(value)) {
			return true;
		} else if ("0".equals(value)) {
			return false;
		} else {
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
//			return true;
		}
	}

	public void save() throws IOException {
//		Logger.log(this + ".save()");
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
