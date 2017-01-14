/*
* Чтение всех значений всех секций в список массивов
*
* Пример секции:
*
* Backup=HANA-EQA_FULL
* Description=Полные бэкапы системы качества
* Type=Folder
* Server=172.16.0.23/Backup_file
* Folder=DB/hana/hana-eqa/full
* Depth=7
*
*/

package tm.mao.cbf;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.regex.*;

public class CBFIni {

	public ArrayList<String[]> sectionData = new ArrayList<String[]>();
	String[] sectionFields = new String[6];
	Pattern patternParam = Pattern.compile("[A-Za-z_0-9]+[$=]");
	Matcher matchParam;

        public CBFIni() {

		try {
		
			List<String> lines = Files.readAllLines(Paths.get("backups.conf"));
			boolean checkSection = false;
			int i = -1;

			for(String line: lines){
				if (line.length() > 0 && line.charAt(0) != '#') {
					if (!checkSection) {
						checkSection = true;
						sectionData.add(new String[6]);
					}
					i++;
					matchParam = patternParam.matcher(line);
					if (matchParam.lookingAt()) {
						sectionData.get(sectionData.size()-1)[i] = line.substring(matchParam.end());
					}
				} else {
					checkSection = false;
					i = -1;
				}
			}
		} catch (IOException e) {
		}
        }
}
