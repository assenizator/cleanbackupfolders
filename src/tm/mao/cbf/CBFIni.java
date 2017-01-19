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
			int i = -1;

			for(String line: lines){
				if (line.length() > 0 && line.charAt(0) != '#') {
					if (!checkSection) {
						checkSection = true; // Отметка, что начались параметры секции
						obj = new SectionFields();
						sectionData.add(obj); // Создаем новый элемент-объект списка
					}
					i++;
					matchParam = patternParam.matcher(line); // Выуживаем имя параметра
//					matchValue = patternValue.matcher(line); // Выуживаем значение параметра
					log.debug("line = " + line);
					if (matchParam.lookingAt()) {
						setters = getSetters(SectionFields.class); //
//						obj = new SectionFields();
						setters.get(line.substring(matchParam.start(),matchParam.end()).toLowerCase()).invoke(obj, line.substring(matchParam.end()+1)); // находит поле и заполняет его значением (выполняет метод заполнения данного поля значением) 
					}
				} else {
					checkSection = false;
					i = -1;
				}
			}

//			тестовый блок вывода всех значений списка
			for(SectionFields sectionField: sectionData){
				log.info(sectionField.backup);
				log.info(sectionField.description);
			}
		} catch (Exception e) {
		} catch (Throwable e) {
		}
        }

	private static Map<String, MethodHandle> getSetters(Class<?> type) throws Exception {
		Map<String, MethodHandle> setters = new HashMap<>(); // таблица с набором "имя поля" - "метод заполнения значения поля"?
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				field.setAccessible(true);
				setters.put(field.getName(), MethodHandles
									.lookup()
									.unreflectSetter(field)); // заполнение таблицы
			}
			type = type.getSuperclass();
		}

		return setters;
	}

	public class SectionFields {
		String backup, description, type, server, folder, days, weeks, monthes, years, masterDay;
	}

}

