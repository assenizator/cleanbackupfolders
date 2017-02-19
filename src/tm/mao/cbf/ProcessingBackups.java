package tm.mao.cbf;
import java.io.*;
import jcifs.smb.*;
import java.util.*;
import java.time.*;
import static java.time.temporal.TemporalAdjusters.*;
import java.text.SimpleDateFormat;
import org.apache.log4j.*;
import tm.mao.cbf.CBFIni.*;

public class ProcessingBackups {

	private static Logger log = Logger.getLogger(ProcessingBackups.class.getName());
	public ArrayList<String> essentialFiles = new ArrayList<String>();

	
	public ProcessingBackups (NtlmPasswordAuthentication auth, CBFIni iniBckObj ) { // Передача данных авторизации и списка параметров бэкапов

		try {
			LocalDate currentDate; // текущая дата
			Long currentEpochDay; // текущий Unix день
			Long edgeDay; // крайний день ежедневных бэкапов
			int currentDOW, currentMonth, masterDay;
			SmbFile smbFile;

			for(SectionFields sectionFields: iniBckObj.sectionData) { //перебор списка с данными для бэкапов
				smbFile = new SmbFile("smb://" + sectionFields.server + "/", sectionFields.folder + "/", auth); // список файлов

				currentDate = LocalDate.now(ZoneId.of("Europe/Moscow")); // текущая дата
				currentEpochDay = currentDate.toEpochDay(); // текущий Unix день

				// Вычисление ежедневных копий
				if (sectionFields.days.replaceAll(" ", "") != "") { // если задано число дней
					edgeDay = currentEpochDay - Long.parseLong(sectionFields.days);
					for ( SmbFile f : smbFile.listFiles() ) { // перебираем список файлов
						if ((f.createTime() / 86400000) >= edgeDay) { // если файл попадает в интервал дат количества файлов
							essentialFiles.add(f.getName());
							log.info("daily -> " + f.getName());
						}
					}
					currentDate = currentDate.minusDays(Integer.parseInt(sectionFields.days)); 
					currentEpochDay = currentDate.toEpochDay();
				}

				// теперь currentDate указывает на дату самого раннего дневного бэкапа (если дневные заданы)
				// начинаем искать вниз по дате ближайший опорный день, с которого начнем отсчитывать недельные бэкапы

				// Вычисление еженедельных копий
				if (sectionFields.weeks.replaceAll(" ", "") != "") { // если задано число недель
					currentDate = currentDate.with(previousOrSame(DayOfWeek.of(Integer.parseInt(sectionFields.masterday)))); // Теперь currentDate указывает на дату самого позднего недельного бэкапа (это может быть и текущая дата)
					for (int i = 0; i < Integer.parseInt(sectionFields.weeks); i++) { // Перебор всех недельных бэкапов
						currentEpochDay = currentDate.toEpochDay();
						for ( SmbFile f : smbFile.listFiles() ) { // перебираем список файлов
							if ((f.createTime() / 86400000) == currentEpochDay) {
								essentialFiles.add(f.getName());
								log.info("weekly -> " + f.getName());
							}
						}
						currentDate = currentDate.minusWeeks(1); // Идем каждый раз на неделю назад, начиная с текущей
					}
				}

				// Теперь, если пройдены еженедельные бэкапы, мы находимся в месяце, с которого надо начать месячные бэкапы
				// но сначала надо вернуться на неделю назад, т.к. если последний недельный бэкап приходился на начало месяца,
				// нас передвинуло на предыдущий месяц, и тогда мы никогда не получим месячных бэкапов, т.к. все время будем
				// удалять их

				// Вычисление ежемесячных копий
				if (sectionFields.monthes.replaceAll(" ", "") != "") { // если задано число месяцев
       					currentDate = currentDate.plusWeeks(1); // возвращаемся на неделю вперед (надо проверить, как алгоритм ведет себя, если недельные бэкапы (и дневные тоже) не предусмотрены)
					currentDate = currentDate.with(firstInMonth(DayOfWeek.of(Integer.parseInt(sectionFields.masterday)))); // определяем, на какую дату месяца приходится первый опорный день недели

					for (int i = 0; i < Integer.parseInt(sectionFields.monthes); i++) { // Перебор всех недельных бэкапов
						currentEpochDay = currentDate.toEpochDay();
						for ( SmbFile f : smbFile.listFiles() ) { // перебираем список файлов
							if ((f.createTime() / 86400000) == currentEpochDay) {
								essentialFiles.add(f.getName());
								log.info("monthly -> " + f.getName());
							}
						}
						currentDate = currentDate.minusMonths(1); // Идем каждый раз на месяц назад, начиная с текущего
						currentDate = currentDate.with(firstInMonth(DayOfWeek.of(Integer.parseInt(sectionFields.masterday)))); // в новом месяце снова вычисляем дату первого опорного дня
					}
				}

				// Вычисление ежегодных копий
				if (sectionFields.monthes.replaceAll(" ", "") != "") { // если задано число месяцев
       					currentDate = currentDate.plusMonths(1); // возвращаемся на месяц вперед (надо проверить, как алгоритм ведет себя, если недельные бэкапы (и дневные тоже) не предусмотрены)
					currentDate = currentDate.with(firstDayOfYear()); // переходим в 1-й месяц года (на первое число года)
					currentDate = currentDate.with(firstInMonth(DayOfWeek.of(Integer.parseInt(sectionFields.masterday)))); // определяем, на какую дату месяца приходится первый опорный день н

					for (int i = 0; i < Integer.parseInt(sectionFields.years); i++) { // Перебор всех недельных бэкапов
						currentEpochDay = currentDate.toEpochDay();
						for ( SmbFile f : smbFile.listFiles() ) { // перебираем список файлов
							if ((f.createTime() / 86400000) == currentEpochDay) {
								essentialFiles.add(f.getName());
								log.info("every year -> " + f.getName());
							}
						}
						currentDate = currentDate.minusYears(1); // Идем каждый раз на год назад, начиная с текущего
						currentDate = currentDate.with(firstInMonth(DayOfWeek.of(Integer.parseInt(sectionFields.masterday)))); // в новом месяце снова вычисляем дату первого опорного дня
					}
				}
			}

			log.info("------------------------------");

			for(String s: essentialFiles) {
				log.info(s);
			}

/*			log.info(String.format("%40s", "").replace(' ', '-'));
			log.info((char)27 + "[93m" + sectionFields.backup + " -- " + sectionFields.description + " (срок давности - " + sectionFields.days + " сут., путь - smb://" + sectionFields.server + "/" + sectionFields.folder + (char)27 + "[0m"); // section header

			for ( SmbFile f : smbFile.listFiles() ) {

				itemTime = f.createTime();
				if (currentTime - itemTime > diffTime) {
					log.info(f.getName() + " <-- expired, deleted");
//					f.delete(); // dangerous!
				}
			}*/
		} catch ( NumberFormatException e ) {
                        log.error((char)27 + "[93m" + "Формат файла < backups.conf >, возможно, не соответствует ожидаемому!" + (char)27 + "[0m");
		} catch ( SmbException e ) {
                        log.error((char)27 + "[93m" + "Проблема при подключении через SMB, проверьте настройки в файлах < settings.conf > и < backups.cong> и доступность сети!" + (char)27 + "[0m");
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
}
