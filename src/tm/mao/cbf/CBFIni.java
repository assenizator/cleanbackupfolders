/*
* Чтение всех значений всех секций в список массивов
*
* # Продуктивная система SAP
* Backup=HANA-ERP_FULL
* Description=Полные бэкапы продуктивной системы
* Type=Folder
* Server=172.16.0.23/Backup_file
* Folder=DB/hana/hana-erp/full
* Days=30
* Weeks=4
* Monthes=4
* Years=3
* MasterDay=5
*/

package tm.mao.cbf;
import java.io.*;
import java.util.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.regex.*;
import org.apache.log4j.*;

public class CBFIni {

	public ArrayList<SectionFields> sectionData = new ArrayList<SectionFields>();
	Pattern patternParam = Pattern.compile("^[A-Za-z_0-9]+");
//	Pattern patternValue = Pattern.compile("[A-Za-z_0-9.\\/]+$");
	Matcher matchParam, matchValue;
	Map<String, MethodHandle> setters;
	public SectionFields obj;
        private static Logger log = Logger.getLogger(CBFIni.class.getName());

        public CBFIni() {
		try {
			List<String> lines = Files.readAllLines(Paths.get("backups.conf"));
			boolean checkSection = false;

			for(String line: lines){
				if (line.length() > 0 && line.charAt(0) != '#') {
					if (!checkSection) { // Если секция еще не начиналась
						checkSection = true; // Отмечаем, что встретили секцию
						obj = new SectionFields(); // создаем объект класса с используемыми нами параметрами
						sectionData.add(obj); // Создаем в списке новый элемент с этим объектом
					}
					matchParam = patternParam.matcher(line); // Выуживаем из строки имя параметра
					if (matchParam.lookingAt()) { // если совпадение найдено (т.е. это строка с параметром)
						setters = getSetters(SectionFields.class); // вызываем метод создания метода заполнения значением поля класса с тем же именем, что и параметр
						setters.get(line.substring(matchParam.start(),matchParam.end()).toLowerCase()).invoke(obj, line.substring(matchParam.end()+1)); // находит поле и заполняет его значением (выполняет метод заполнения данного поля значением) 
					}
				} else {
					checkSection = false;
				}
			}
//                      тестовый блок вывода всех значений списка
                        for(SectionFields sectionField: sectionData){
				log.debug(sectionField.backup);
				log.debug(sectionField.description);
				log.debug(sectionField.type);
				log.debug(sectionField.server);
				log.debug(sectionField.folder);
				log.debug(sectionField.days);
				log.debug(sectionField.weeks);
				log.debug(sectionField.monthes);
				log.debug(sectionField.years);
				log.debug(sectionField.masterday);
				log.debug("------------------------");
			}
		} catch (NoSuchFileException e) {
			log.error("Файл конфигурации бэкапов < backup.conf > не найден!");
			e.printStackTrace();
		} catch (NullPointerException e) {
			log.error("Формат файла < backup.conf >, возможно, не соответствует ожидаемому!");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
        }

	private static Map<String, MethodHandle> getSetters(Class<?> type) throws Exception {
		Map<String, MethodHandle> setters = new HashMap<>(); // таблица с набором "имя поля" - "метод заполнения значения поля"?
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				field.setAccessible(true);
				setters.put(field.getName(), MethodHandles.lookup().unreflectSetter(field)); // заполнение таблицы
			}
			type = type.getSuperclass();
		}
		return setters;
	}

	public class SectionFields {
		String backup, description, type, server, folder, days, weeks, monthes, years, masterday;
	}

}

