import org.xml.sax.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.ArrayList;

public class XMLParser {
	public ArrayList<AtomicModel> knicks;
	public ArrayList<AtomicModel> hawks;

	public XMLParser(String fileName, AtomicModel knicksHoop, AtomicModel hawksHoop){
		knicks = new ArrayList<AtomicModel>();
		hawks = new ArrayList<AtomicModel>();

		Document xmlDoc = getDocument(fileName);

		NodeList listOfPlayers = xmlDoc.getElementsByTagName("player");
		for (int i = 0; i < listOfPlayers.getLength(); i++){
			Node node = listOfPlayers.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE){
				Element element = (Element) node;
				String team = element.getElementsByTagName("team").item(0).getTextContent();
				String name = element.getElementsByTagName("name").item(0).getTextContent();
				String position = element.getElementsByTagName("position").item(0).getTextContent();
				double touches = Double.parseDouble(element.getElementsByTagName("touches").item(0).getTextContent());			
				double passes = Double.parseDouble(element.getElementsByTagName("passes").item(0).getTextContent());
				double twoPointAtt = Double.parseDouble(element.getElementsByTagName("twoPointAtt").item(0).getTextContent());
				double twoPointMade = Double.parseDouble(element.getElementsByTagName("twoPointMade").item(0).getTextContent());
				double threePointAtt = Double.parseDouble(element.getElementsByTagName("threePointAtt").item(0).getTextContent());
				double threePointMade = Double.parseDouble(element.getElementsByTagName("threePointMade").item(0).getTextContent());
				double turnovers = Double.parseDouble(element.getElementsByTagName("turnovers").item(0).getTextContent());
				
				makePlayer(position, name, team, touches, passes, twoPointAtt, twoPointMade, threePointAtt, threePointMade,
					turnovers);
			}
		}
		couple(knicks, knicksHoop);
		couple(hawks, hawksHoop);
	}

	private void couple(ArrayList<AtomicModel> team, AtomicModel hoop){
		for (AtomicModel player : team){
			for (AtomicModel p : team){
				player.addInput(p);
				player.addOutput(p);
				player.addOutput(hoop);
				hoop.addInput(player);
			}
		}
	}

	private void makePlayer(String pos, String name, String team, double touches, double passes, 
		double twoPointAtt, double twoPointMade, double threePointAtt, double threePointMade, double turnovers){

		double passPer = (passes/touches);
		double twoPtPer = (twoPointAtt/touches);
		double twoPtAcc = (twoPointMade/twoPointAtt);
		double threePtPer = (threePointAtt/touches);
		double threePtAcc = (threePointMade/threePointAtt);
		double turnoverPer = (turnovers/touches);
		
		if (pos.equals("PG")){
			AtomicModel<Network, AtomicModel> pg = new Player<Network, AtomicModel>(team, name, pos, passPer, twoPtPer, twoPtAcc,
				threePtPer, threePtAcc, turnoverPer);
			switch (team){
				case "Knicks": knicks.add(0, pg); break;
				case "Hawks": hawks.add(0, pg); break;
			}
		} else {
			AtomicModel<AtomicModel, AtomicModel> p = new Player<AtomicModel, AtomicModel>(team, name, pos, passPer, twoPtPer, twoPtAcc,
				threePtPer, threePtAcc, turnoverPer);
			switch (team){
				case "Knicks": knicks.add(p); break;
				case "Hawks": hawks.add(p); break;
			}
		}
	}
	private Document getDocument(String docString){
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(docString));
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
}