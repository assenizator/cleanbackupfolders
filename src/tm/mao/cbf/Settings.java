/*
* Чтение установочного файла settings.conf
*
*/

package tm.mao.cbf;
import java.io.*;
import java.util.Properties;
import org.apache.log4j.*;
 
public class Settings {

        private static Logger log = Logger.getLogger(Settings.class.getName());
	String smbServer, smbShare, smbShareSubDir;
	String domain, user, pass;

	public Settings() {

		FileInputStream fis;
		Properties property = new Properties();

		try {
			fis = new FileInputStream("settings.conf");
			property.load(fis);


			smbServer = property.getProperty("smbServer");
			smbShare = property.getProperty("smbShare");
			smbShareSubDir = property.getProperty("smbShareSubDir");
			domain = property.getProperty("domain");
			user = property.getProperty("user");
			pass = property.getProperty("pass");

		} catch (FileNotFoundException e) {
			log.error("ОШИБКА: Файл свойств отсутствует!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

