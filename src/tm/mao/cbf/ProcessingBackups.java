package tm.mao.cbf;
import java.io.*;
import jcifs.smb.*;
import java.util.*;
import org.apache.log4j.*;
import tm.mao.cbf.CBFIni.*;

public class ProcessingBackups {

	private static Logger log = Logger.getLogger(ProcessingBackups.class.getName());

	public ProcessingBackups (NtlmPasswordAuthentication auth, CBFIni iniBckObj ) {

		try {
			Date currentDate = new Date();
			Long currentTime = currentDate.getTime();
			Long itemTime, diffTime;
			SmbFile smbFile;

			for(SectionFields sectionFields: iniBckObj.sectionData) { //перебор списка с данными для бэкапов
				log.info(String.format("%40s", "").replace(' ', '-'));
				log.info((char)27 + "[93m" + sectionFields.backup + " -- " + sectionFields.description + " (срок давности - " + sectionFields.days + " сут., путь - smb://" + sectionFields.server + "/" + sectionFields.folder + (char)27 + "[0m"); // section header

				smbFile = new SmbFile("smb://" + sectionFields.server + "/", sectionFields.folder + "/", auth);
				diffTime = Long.parseLong(sectionFields.days) * 3600 * 24 * 1000;

				for ( SmbFile f : smbFile.listFiles() ) {

					itemTime = f.createTime();
					if (currentTime - itemTime > diffTime) {
						log.info(f.getName() + " <-- expired, deleted");
//						f.delete(); // dangerous!
					}
				}
			}
		} catch ( NumberFormatException e ) {
                        log.error((char)27 + "[93m" + "Формат файла < backups.conf >, возможно, не соответствует ожидаемому!" + (char)27 + "[0m");
		} catch ( SmbException e ) {
                        log.error((char)27 + "[93m" + "Проблема при подключении через SMB, проверьте настройки в файлах < settings.conf > и < backups.cong> и доступность сети!" + (char)27 + "[0m");
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
}
