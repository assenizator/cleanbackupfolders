/*
* Чтение всех значений всех секций в список массивов
*
*
*/

package tm.mao.cbf;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.regex.*;

public class CBFIni {

	public ArrayList<SectionFields> sectionData = new ArrayList<SectionFields>();
	Pattern patternParam = Pattern.compile("^[A-Za-z_0-9]+");
//	Pattern patternValue = Pattern.compile("[A-Za-z_0-9.\\/]+$");
	Matcher matchParam, matchValue;

        public CBFIni() {

		try {
		
			List<String> lines = Files.readAllLines(Paths.get("backups.conf"));
			boolean checkSection = false;
			int i = -1;

			for(String line: lines){
				if (line.length() > 0 && line.charAt(0) != '#') {
					if (!checkSection) {
						checkSection = true; // Отметка, что начались параметры секции
						sectionData.add(new SectionFields()); // Создаем новый элемент-объект списка
					}
					i++;
					matchParam = patternParam.matcher(line); // Выуживаем имя параметра
//					matchValue = patternValue.matcher(line); // Выуживаем значение параметра
					if (matchParam.matches()) {
						sectionData.get(sectionData.size()-1).class.line.substring(matchParam.begin(),matchParam.end()) = line.substring(matchParam.end()+1);
					}
				} else {
					checkSection = false;
					i = -1;
				}
			}
		} catch (IOException e) {
		}
        }

	Map<String, MethodHandle> setters = getSetters(SectionFields.class);
	SectionFields obj = new SectionFields();
	setters.get("backup").invoke(obj, "hello world");

	private static Map<String, MethodHandle> getSetters(Class<?> type) throws Exception {
		Map<String, MethodHandle> setters = new HashMap<>();
		while (type != null) {
			for (Field field : type.getDeclaredFields()) {
				field.setAccessible(true);
				setters.put(field.getName(), MethodHandles
									.lookup()
									.unreflectSetter(field));
			}
			type = type.getSuperclass();
		}

		return setters;
	}

	public class SectionFields {
		/*
		# Продуктивная система SAP
		Backup=HANA-ERP_FULL
		Description=Полные бэкапы продуктивной системы
		Type=Folder
		Server=172.16.0.23/Backup_file
		Folder=DB/hana/hana-erp/full
		Days=30
		Weeks=4
		Monthes=4
		Years=3
		MasterDay=5
		*/
		
		String backup, description, type, server, folder;
		int days, weeks, monthes, years, masterDay;
	}

}

