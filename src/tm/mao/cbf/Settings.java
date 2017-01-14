/*
* Чтение установочного файла settings.conf
*
*/

package tm.mao.cbf;
import java.io.*;
import java.util.Properties;
 
public class Settings {

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

		} catch (IOException e) {
			System.err.println("ОШИБКА: Файл свойств отсутствует!");
		}
	}
}

