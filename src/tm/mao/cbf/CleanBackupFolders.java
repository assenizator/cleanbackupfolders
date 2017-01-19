package tm.mao.cbf;
import java.io.*;
import jcifs.smb.*;
import java.util.*;
import org.apache.log4j.*;

public class CleanBackupFolders {

	private static Logger log = Logger.getLogger(CleanBackupFolders.class.getName());

	public static void main(String[] args) throws IOException {

		String smbShare, smbShareSubDir;
		Date currentDate = new Date();
		Long currentTime = currentDate.getTime();
		Long itemTime, diffTime;
		SmbFile smbFile;
		ArrayList<String> nessesBackup = new ArrayList<String>();

		Settings iniFileObj = new Settings(); // settings from settings.conf
		CBFIni iniBckObj = new CBFIni(); // backup settings from backups.conf

		NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(iniFileObj.domain, iniFileObj.user, iniFileObj.pass);

		for(String[] sectionFields: iniBckObj.sectionData) { //перебор списка с данными для бэкапов
/*
* sectionField[0] Backup=HANA-EQA_FULL
*             [1] Description=Полные бэкапы системы качества
*             [2] Type=Folder
*             [3] Server=172.16.0.23/Backup_file
*             [4] Folder=DB/hana/hana-eqa/full
*             [5] Depth=7
*/
//			log.info((char)27 + "[37;41m" + sectionFields[0] + (char)27 + "[0m"); // section header
			log.info(String.format("%40s", "").replace(' ', '-'));
			log.info(sectionFields[0] + " -- " + sectionFields[1] + " (срок давности - " + sectionFields[5] + " сут., путь - smb://" + sectionFields[3] + "/" + sectionFields[4]); // section header

	                smbShare = "smb://" + sectionFields[3] + "/";
        	        smbShareSubDir = sectionFields[4] + "/";
			smbFile = new SmbFile(smbShare, smbShareSubDir, auth);
			diffTime = Long.parseLong(sectionFields[5]) * 3600 * 24 * 1000;

			for ( SmbFile f : smbFile.listFiles() ) {

				itemTime = f.createTime();
				if (currentTime - itemTime > diffTime) {
//					log.info((char)27 + "[37;41m" + f.getName() + (char)27 + "[0m  <-- expired");
					log.info(f.getName() + " <-- expired, deleted");
					f.delete(); // dangerous!
//					log.debug("...........................................");
				}
			}
		}
        }
}

