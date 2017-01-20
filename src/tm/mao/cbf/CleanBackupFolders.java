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
			Date currentDate = new Date();
			Long currentTime = currentDate.getTime();
			Long itemTime, diffTime;
			SmbFile smbFile;

			Settings iniFileObj = new Settings(); // settings from settings.conf
			CBFIni iniBckObj = new CBFIni(); // backup settings from backups.conf

			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(iniFileObj.domain, iniFileObj.user, iniFileObj.pass);

			for(SectionFields sectionFields: iniBckObj.sectionData) { //перебор списка с данными для бэкапов
//				log.info((char)27 + "[37;41m" + sectionFields[0] + (char)27 + "[0m"); // section header
				log.info(String.format("%40s", "").replace(' ', '-'));
				log.info(sectionFields.backup + " -- " + sectionFields.description + " (срок давности - " + sectionFields.days + " сут., путь - smb://" + sectionFields.server + "/" + sectionFields.folder); // section header

				smbFile = new SmbFile("smb://" + sectionFields.server + "/", sectionFields.folder + "/", auth);
				diffTime = Long.parseLong(sectionFields.days) * 3600 * 24 * 1000;
				log.debug("sectionFields.days = " + sectionFields.days);

				for ( SmbFile f : smbFile.listFiles() ) {

					itemTime = f.createTime();
					log.debug("currentTime = " + currentTime);
					log.debug("itemTime = " + itemTime);
					log.debug("curr - item = " + (currentTime - itemTime) );
					log.debug("diffTime = " + diffTime);
					if (currentTime - itemTime > diffTime) {
//						log.info((char)27 + "[37;41m" + f.getName() + (char)27 + "[0m  <-- expired");
						log.info(f.getName() + " <-- expired, deleted");
//						f.delete(); // dangerous!
//						log.debug("...........................................");
					}
				}
			}
		} catch (NumberFormatException e) {
                        log.error("Формат файла < backup.conf >, возможно, не соответствует ожидаемому!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
