/*

Чтение файла backups.conf, описывающего глубину бэкапов, в список массивов

Пример файла конфигурации:

# БД Firebird Search <-- просто запись о секции, не обрабатывается
Backup=FIREBIRD_SEARCH <-- кодовое имя бэкапа
Description=Полные бэкапы БД Firebird для Search <-- описание бэкапа
Type=Folder <-- тип обрабатываемого ресурса (на будущее)
AuthServer=NETAPP1 <-- код авторизационных данных на шару (обрабатывается в Settings.java)
Folder=s4 <-- папка на соответствующей шаре (может через / указывать и на подпапки)
Days=7 <-- количество ежедневных копий
Weeks=4 <-- количество еженедельных копий (отсчитывается после ежедневных)
Monthes=10 <-- количество ежемесячных копий (отсчитывается после еженедельных или ежедневных)
Years=3 <-- количество ежегодных копий (отсчитывается после ежемесячных или еженедельных или ежедневных)
MasterDay=5 <-- какой день проверяется (опорный день)

Новый формат файла в XML:

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>

<AuthData>
	<AuthServer id="NETAPP1">
		<Description>Полные бэкапы VM и БД Махачкалы</Description>
		<Type>Share</Type>
		<SmbServer>172.16.0.23</SmbServer>
		<SmbShare>Backup_file</SmbShare>
		<Domain>sapfir</Domain>
		<User>mao_adm</User>
		<Password>1qazxsw23</Password>
	</AuthServer>
	<AuthServer id="QNAPMSK1">
		...
	</AuthServer>
</AuthData>

*/

package tm.mao.cbf;

import java.io.*;
import java.util.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.regex.*;
import org.apache.log4j.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
 
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.IOException;
 
public class CBFIni {

    public ArrayList<SectionFields> sectionData = new ArrayList<SectionFields>();
    Pattern patternParam = Pattern.compile("^[A-Za-z_0-9]+");
    // Pattern patternValue = Pattern.compile("[A-Za-z_0-9.\\/]+$");
    Matcher matchParam, matchValue;
    Map<String, MethodHandle> setters;
    public SectionFields obj;
    private static Logger log = Logger.getLogger(CBFIni.class.getName());

    public CBFIni(String authServer) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("backups.conf"));
            boolean checkSection = false;

            for (String line : lines) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    if (!checkSection) { // Если секция еще не начиналась
                        checkSection = true; // Отмечаем, что встретили секцию
                        obj = new SectionFields(); // создаем объект класса с используемыми нами параметрами
                        sectionData.add(obj); // Создаем в списке новый элемент с этим объектом
                    }
                    matchParam = patternParam.matcher(line); // Выуживаем из строки имя параметра
                    if (matchParam.lookingAt()) { // если совпадение найдено (т.е. это строка с параметром)
                        setters = getSetters(SectionFields.class); // вызываем метод создания метода заполнения
                                                                   // значением поля класса с тем же именем, что и
                                                                   // параметр
                        setters.get(line.substring(matchParam.start(), matchParam.end()).toLowerCase()).invoke(obj,
                                line.substring(matchParam.end() + 1)); // находит поле и заполняет его значением
                                                                       // (выполняет метод заполнения данного поля
                                                                       // значением)
                    }
                } else {
                    checkSection = false;
                }
            }
            // тестовый блок вывода всех значений списка
            for (SectionFields sectionField : sectionData) {
                log.debug(sectionField.backup);
                log.debug(sectionField.description);
                log.debug(sectionField.type);
                log.debug(sectionField.authserver);
                log.debug(sectionField.folder);
                log.debug(sectionField.days);
                log.debug(sectionField.weeks);
                log.debug(sectionField.monthes);
                log.debug(sectionField.years);
                log.debug(sectionField.masterday);
                log.debug("------------------------");
            }
        } catch (NoSuchFileException e) {
            log.error((char) 27 + "[93m" + "Файл конфигурации бэкапов < backup.conf > не найден!" + (char) 27 + "[0m");
            e.printStackTrace();
        } catch (NullPointerException e) {
            log.error((char) 27 + "[93m" + "Формат файла < backup.conf >, возможно, не соответствует ожидаемому!"
                    + (char) 27 + "[0m");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static Map<String, MethodHandle> getSetters(Class<?> type) throws Exception {
        Map<String, MethodHandle> setters = new HashMap<>(); // таблица с набором "имя поля" - "метод заполнения
                                                             // значения поля"?
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
        String backup, description, type, authserver, folder, days, weeks, monthes, years, masterday;
    }

}

// XML parsing
//
//
 
public class Access {
 
    public static void main(String[] args) {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        // включаем поддержку пространства имен XML
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        try {
            builder = builderFactory.newDocumentBuilder();
            doc = builder.parse("access.xml");
 
            // Создаем объект XPathFactory
            XPathFactory xpathFactory = XPathFactory.newInstance();
 
            // Получаем экзмепляр XPath для создания XPathExpression выражений
            XPath xpath = xpathFactory.newXPath();

            String devName = getAuthServerById(doc, xpath, args[0]);
            System.out.println("Узел id = " + args[0] + ": " + devName);
 
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
 
    private static String getAuthServerById(Document doc, XPath xpath, String id) {
        String devName = null;
        try {
            XPathExpression xPathExpression = xpath.compile(
                    "/AuthData/AuthServer[@id='" + id + "']/Description/text()"
            );
            devName = (String) xPathExpression.evaluate(doc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return devName;
    }
}

