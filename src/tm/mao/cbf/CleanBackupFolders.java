package tm.mao.cbf;
import java.io.*;
import jcifs.smb.*;
import java.util.*;
import org.apache.log4j.*;
import tm.mao.cbf.CBFIni.*;

public class CleanBackupFolders {

	private static Logger log = Logger.getLogger(CleanBackupFolders.class.getName());

	public static void main(String[] args) {

		try {
			Settings iniFileObj = new Settings(); // settings from settings.conf
			CBFIni iniBckObj = new CBFIni(); // backup settings from backups.conf
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(iniFileObj.domain, iniFileObj.user, iniFileObj.pass);
			new ProcessingBackups(auth, iniBckObj); // Вызов процедуры обработки бэкапов
		} catch ( NumberFormatException e ) {
                        log.error((char)27 + "[93m" + "Формат файла < backups.conf >, возможно, не соответствует ожидаемому!" + (char)27 + "[0m");
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
}
