package intern;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class InternChallenge {

	public static void main(String[] args) {
		illinoisPhysicians("Karamchandani", "Kishore Karamchandani - Peoria");
		illinoisPhysicians("Gradishar", "William Gradishar - Chicago");
		illinoisPhysicians("Singh", "Kern Singh - Chicago");
		illinoisPhysicians("Shah", "Bharat Shah - Rockford");
	}

	private static void illinoisPhysicians(String lastName, String conditonString) {
		try {
			Document resultDoc = getDataFromUrl(lastName);

			Element table = resultDoc.select("table").first();

			Element header = table.getElementsByClass("cavu-grid-header").first();
			Elements rows = table.getElementsByClass("cavu-grid-row");
			Elements altRows = table.getElementsByClass("cavu-grid-alter");
			JSONObject jsonObj = new JSONObject();
			JSONArray jsonArr = new JSONArray();

			Elements headers = header.getElementsByTag("th");

			ArrayList<String> th = new ArrayList<String>();
			/* Creating the keys for the JSON */
			for (int x = 0; x < headers.size(); x++) {
				th.add(headers.get(x).text());
			}

			String[] conditions = conditonString.split("-");
			String firstName = conditions[0].split(" ")[0].trim();
			String location = conditions[1].trim();

			createPersonObject(rows, jsonArr, th, firstName, location);
			createPersonObject(altRows, jsonArr, th, firstName, location);

			jsonObj.put("Persons", jsonArr);

			persistJson(jsonObj, firstName);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Method to persist the JSON on the file system
	 * 
	 * @param jsonObj
	 *            The JSON object to be persisted
	 * @param name
	 *            The name for the JSON file
	 */
	private static void persistJson(JSONObject jsonObj, String name) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmsss");
			FileWriter fileWriter = new FileWriter("./JSON/" + name + "_" + format.format(new Date()) + ".json");
			fileWriter.write(jsonObj.toString());
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to create the JSON object for each person, after the validation
	 * for last name and city
	 * 
	 */
	private static void createPersonObject(Elements rows, JSONArray jsonArr, ArrayList<String> th, String firstName,
			String location) {
		for (Element row : rows) {
			Elements rowData = row.getElementsByTag("td");
			JSONObject jo = new JSONObject();
			boolean validated = true;
			for (int i = 1; i < rowData.size(); i++) {
				if ((i == 1 && !rowData.get(i).text().split(" ")[0].equalsIgnoreCase(firstName))
						|| (i == 4 && !rowData.get(i).text().split(",")[0].equalsIgnoreCase(location))) {
					validated = false;
					break;
				} else {
					jo.put(th.get(i), rowData.get(i).text());
				}
			}
			if (validated) {
				jsonArr.put(jo);
			}
		}
	}

	/**
	 * Method to get the data from submitting the form
	 * 
	 * @param lastName
	 *            The last name of the person
	 * @return Document object containing the HTML data
	 * @throws IOException
	 */
	private static Document getDataFromUrl(String lastName) throws IOException {
		Connection.Response response = Jsoup
				.connect("https://ilesonline.idfpr.illinois.gov/DPR/Lookup/LicenseLookup.aspx")
				.method(Connection.Method.GET).execute();
		Document doc = response.parse();

		Connection.Response result = Jsoup
				.connect("https://ilesonline.idfpr.illinois.gov/DPR/Lookup/LicenseLookup.aspx")
				.header("Content-type", "application/x-www-form-urlencoded; charset=utf-8").cookies(response.cookies())
				.data("ctl00$MainContentPlaceHolder$ucLicenseLookup$ctl01$ddDivision", "1")
				.data("ctl00$MainContentPlaceHolder$ucLicenseLookup$ctl01$tbLastName_Contact", lastName)
				.data("__EVENTTARGET", "ctl00$MainContentPlaceHolder$ucLicenseLookup$UpdtPanelGridLookup")
				.data("__VIEWSTATEGENERATOR", doc.select("[name$=__VIEWSTATEGENERATOR]").first().val())
				.data("__EVENTARGUMENT", "5").data("__VIEWSTATE", doc.select("[name$=__VIEWSTATE]").first().val())
				.data("ctl00$ScriptManager1",
						"ctl00$MainContentPlaceHolder$ucLicenseLookup$UpdtPanelGridLookup|ctl00$MainContentPlaceHolder$ucLicenseLookup$UpdtPanelGridLookup")
				.data("__ASYNCPOST", "true").method(Connection.Method.POST).execute();

		Document resultDoc = result.parse();
		return resultDoc;
	}
}
