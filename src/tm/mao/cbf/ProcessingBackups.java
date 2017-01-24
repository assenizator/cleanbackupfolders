package tm.mao.cbf;
import java.io.*;
import jcifs.smb.*;
import java.util.*;
import java.text.SimpleDateFormat;
import org.apache.log4j.*;
import tm.mao.cbf.CBFIni.*;

public class ProcessingBackups {

	private static Logger log = Logger.getLogger(ProcessingBackups.class.getName());
	public ArrayList<String> essentialFiles = new ArrayList<String>();

	
	public ProcessingBackups (NtlmPasswordAuthentication auth, CBFIni iniBckObj ) { // Передача данных авторизации и списка параметров бэкапов

		try {
			Calendar currentDate = new GregorianCalendar(); // текущая дата
			Long currentDateInMs = currentDate.getTimeInMillis();
			Long diffTime;
			int currentDOW, masterDay; // текущий день недели
			SmbFile smbFile;

			for(SectionFields sectionFields: iniBckObj.sectionData) { //перебор списка с данными для бэкапов
				smbFile = new SmbFile("smb://" + sectionFields.server + "/", sectionFields.folder + "/", auth); // список файлов

				// Вычисление ежедневных копий
				if (sectionFields.days.replaceAll(" ", "") != "") { // если задано число дней
					diffTime = Long.parseLong(sectionFields.days) * 3600 * 24 * 1000;
					for ( SmbFile f : smbFile.listFiles() ) { // перебираем список файлов
						if (f.createTime() >= (currentDateInMs - diffTime) ) { // если файл попадает в интервал дат количества файлов
							essentialFiles.add(f.getName());
						}
					}
					currentDate.add(Calendar.DAY_OF_MONTH, - (Integer.parseInt(sectionFields.days) + 1)); 
					currentDateInMs = currentDate.getTimeInMillis();
				}

				// теперь currentDate указывает на дату самого раннего дневного бэкапа + 1 день (если дневные заданы)
				// начинаем искать вниз по дате ближайший опорный день, с которого начнем отсчитывать недельные бэкапы

				// Вычисление еженедельных копий
				if (sectionFields.weeks.replaceAll(" ", "") != "") { // если задано число недель
					diffTime = Long.parseLong(sectionFields.weeks) * 3600 * 24 * 1000;
					currentDOW = currentDate.get(Calendar.DAY_OF_WEEK); // какой на текущей дате день недели
					masterDay = Integer.parseInt(sectionFields.masterday);
					currentDate.add(Calendar.DAY_OF_MONTH, masterDay - currentDOW);
					if (currentDOW - masterDay >= 0) { // если день недели больше опорного или равен ему
						currentDate.add(Calendar.DAY_OF_MONTH, masterDay - currentDOW);
					} else {
						currentDate.add(Calendar.DAY_OF_MONTH, masterDay - currentDOW - 7);
					} // Теперь currentDate указывает на дату самого позднего недельного бэкапа
					currentDateInMs = currentDate.getTimeInMillis();

					for (int i = 0; i < Integer.parseInt(sectionFields.weeks); i++) { // Перебор всех недельных бэкапов
						currentDate.add(Calendar.DAY_OF_MONTH, - i * 7); // Идем каждый раз на неделю назад, начиная с текущей
						currentDateInMs = currentDate.getTimeInMillis();
						for ( SmbFile f : smbFile.listFiles() ) { // перебираем список файлов
							if ((f.createTime() / (1000 * 3600 * 24)) == (currentDateInMs / (1000 * 3600 * 24))) {
								essentialFiles.add(f.getName());
							}
						}
					}
				}

	                        for(String s: essentialFiles) {
					log.info(s);
				}
		

/*				log.info(String.format("%40s", "").replace(' ', '-'));
				log.info((char)27 + "[93m" + sectionFields.backup + " -- " + sectionFields.description + " (срок давности - " + sectionFields.days + " сут., путь - smb://" + sectionFields.server + "/" + sectionFields.folder + (char)27 + "[0m"); // section header

				for ( SmbFile f : smbFile.listFiles() ) {

					itemTime = f.createTime();
					if (currentTime - itemTime > diffTime) {
						log.info(f.getName() + " <-- expired, deleted");
//						f.delete(); // dangerous!
					}
				}*/
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
