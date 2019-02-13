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
	String smbServer, smbShare;
	String domain, user, pass;

	public Settings() {

		FileInputStream fis;
		Properties property = new Properties();

		try {
			fis = new FileInputStream("access.conf");
			property.load(fis);

			smbServer = property.getProperty("smbServer");
			smbShare = property.getProperty("smbShare");
			domain = property.getProperty("domain");
			user = property.getProperty("user");
			pass = property.getProperty("pass");

		} catch (FileNotFoundException e) {
			log.error((char) 27 + "[93m" + "ОШИБКА: Файл свойств отсутствует!" + (char) 27 + "[0m");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/*
 * Переделать поиск авторизационных данных с общего поиска пары
 * "свойство=значение" на поиск секции с необходимыми авторизационными данными и
 * их выборку по типу обработки в CBFIni.java
 */