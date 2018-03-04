import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilder;

import org.jdom2.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


/**
 * Written by Martina Reiter
 * 
 * Transformes BPMN_INPUT.xml to OWL_OUTPUT.owl.
 * 
 * @param args
 * @throws SAXException
 * @throws IOException
 * @throws ParserConfigurationException
 * @throws TransformerException
 * 
 */

public class BPMN2OWL {

	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String argv[])
			throws SAXException, IOException, ParserConfigurationException, TransformerException {
		try {

			// PrintWriter
			Scanner scanner = new Scanner(System.in);
			System.out.println("enter source file path:");
			String src = scanner.nextLine();
			File dir = new File("logs");
			dir.mkdir();
			PrintWriter writer = new PrintWriter("logs/OWL_OUTPUT.owl", "UTF-8");
			writer.println("<?xml version=\"1.0\"?>\r\n");

			// Nodes
			@SuppressWarnings("rawtypes")
			Set<String> classes = new HashSet();

			String classNodes = "";
			String dataProperties = "";
			String objectProperties = "";
			String restriction = "";
			String namedIndividuals = "";
			Integer individualsCounter = 1;
			String rdfNode = "<rdf:RDF " + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\r\n "
					+ "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\r\n ";

			// DocumentBuilder
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document document = null;
			try {
				document = docBuilder.parse(new File(src));
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Could not parse your file: "+src);

			}
			//Document document = docBuilder.parse(new File("src/bpmn/BPMN_INPUT.xml"));
			NodeList nodeList = document.getElementsByTagName("*");

			// get xmlns
			System.out.println("Get xmlns");
			String xmlns = "";
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if (node.getNodeName().equals("definitions")) {
						NamedNodeMap attributeList = node.getAttributes();
						for (int a = 0; a < attributeList.getLength(); a++) {
							String attributeName = attributeList.item(a).toString().split("\\=")[0];
							String attributeValue = attributeList.item(a).toString().split("\\=")[1].replace("\"", "");
							if (attributeName.equals("xmlns")) {
								xmlns = attributeValue;
							}
						}
					}
				}
			}

			// individualsCounter for instances, nodes without id and node order
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String newNodeName = node.getNodeName() + "_IC" + individualsCounter;
					individualsCounter = individualsCounter + 1;

					if (node.getNodeName().equals("definitions")) {
						document.renameNode(node, xmlns, newNodeName);
					} else {
						document.renameNode(node, "", newNodeName);

					}
					writeXmlFile(document, src);
				}
			}
			System.out.println("create individuals");
			// create individuals
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					namedIndividuals = createNamedIndividuals(node, namedIndividuals);
				}
			}

			// rename Nodes
			System.out.println("rename nodes");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String originalNodeName = node.getNodeName().split("_IC")[0];
					if (originalNodeName.equals("definitions")) {
						document.renameNode(node, xmlns, originalNodeName);

					} else {
						document.renameNode(node, "", originalNodeName);

					}
					writeXmlFile(document,src);
				}
			}

			System.out.println("create classes and properties");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					NamedNodeMap nodeAttributes = node.getAttributes();
					String childNode = node.getNodeName().toString();
					String parentNode = node.getParentNode().getNodeName();

					// Class can be root class -> then it belongs to owl:Thing
					// Class can be subclass of other class -> then we need <rdfs:subClassOf....
					if (node.getParentNode().getNodeName().equals("#document")) {
						parentNode = "document";
					}

					// create class
					classNodes = new StringBuilder(classNodes).append(
							"<owl:Class rdf:about=\"http://www.reiter.at/ontology/generated#" + childNode + "\">\r\n"
									+ "<rdfs:subClassOf rdf:resource=\"http://www.reiter.at/ontology/generated#"
									+ parentNode + "\"/>\r\n" + "<rdfs:subClassOf>\r\n" + "</rdfs:subClassOf>"
									+ "</owl:Class>")
							.toString();
					classes.add(childNode);

					// create object property
					objectProperties = new StringBuilder(objectProperties)
							.append("<owl:ObjectProperty rdf:about=\"http://www.reiter.at/ontology/generated#"
									+ childNode + "\"/>\r\n")
							.toString();

					if (parentNode.equals("#document")) {
						parentNode = "document";
					}

					// count node occurrences
					Integer nodeCounter = 0;
					NodeList parentNodeList = node.getParentNode().getChildNodes();
					for (int c = 0; c < parentNodeList.getLength(); c++) {
						Node currentChildNode = parentNodeList.item(c);
						if (currentChildNode.getNodeName().equals(nodeName)) {
							nodeCounter = nodeCounter + 1;
						}
					}

					// parent node has x child nodes
					restriction = new StringBuilder(restriction).append(""
							+ " <owl:Class rdf:about=\"http://www.reiter.at/ontology/generated#" + parentNode
							+ "\">\r\n" + "<rdfs:subClassOf>\r\n" + "<owl:Restriction>\r\n"
							+ "<owl:onProperty rdf:resource=\"http://www.reiter.at/ontology/generated#" + childNode
							+ "\"/>\r\n"
							+ "<owl:qualifiedCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#nonNegativeInteger\">"
							+ nodeCounter + "</owl:qualifiedCardinality>\r\n"
							+ "<owl:onClass rdf:resource=\"http://www.reiter.at/ontology/generated#" + childNode
							+ "\"/>\r\n" + "</owl:Restriction>\r\n" + "</rdfs:subClassOf>\r\n" + "</owl:Class>" + "")
							.toString();

					// Iterate over attributes
					for (int a = 0; a < nodeAttributes.getLength(); a++) {

						// Attribute Variables
						String attributeName = nodeAttributes.item(a).toString().split("\\=")[0];
						String attributeValue = nodeAttributes.item(a).toString().split("\\=")[1];

						// Test Cases
						String attributeIsFalse = "\"false\"";
						String attributeIsTrue = "\"true\"";
						Boolean attributeIsObjectProperty = false;
						String attributeType = "string";

						// fix präfix issue
						workaroundPräfixIssues(attributeName);

						// Boolean, String or Reference
						if (attributeValue.equals(attributeIsFalse) || attributeValue.equals(attributeIsTrue)) {
							attributeType = "boolean";
						}

						// Check if property is a reference
						for (String c : classes) {
							if (attributeName.equals("id") || attributeName.equals("targetNamespace")
									|| attributeName.equals("name") || attributeName.startsWith("xmlns")) {
							} else {
								// Prepare class
								String splittedClass = "";
								try {
									splittedClass = c.toString().split(":")[1];

								} catch (Exception e) {
									splittedClass = c.toString();
								}
								String upperSplittedClass = splittedClass.substring(0, 1).toUpperCase()
										+ splittedClass.substring(1);
								// Prepare attribute value
								String splittedAttributeValue = attributeValue;
								if (attributeValue.contains("_")) {
									splittedAttributeValue = splittedAttributeValue.split("_")[0].replaceAll("\"", "");
								} else if (splittedAttributeValue.contains(":")) {
									splittedAttributeValue = attributeValue.split(":")[1].replaceAll("\"", "");
								}
								if (upperSplittedClass.equals(splittedAttributeValue)) {
									attributeType = c.toString();
									attributeIsObjectProperty = true;
								}
							}
						}

						// create object Property
						if (attributeIsObjectProperty) {

							objectProperties = new StringBuilder(objectProperties)
									.append("<owl:ObjectProperty rdf:about=\"http://www.reiter.at/ontology/generated#"
											+ attributeName + "\"/>\r\n")
									.toString();

							restriction = new StringBuilder(restriction).append(
									" <owl:Class rdf:about=\"http://www.reiter.at/ontology/generated#" + childNode
											+ "\">\r\n" + "	<rdfs:subClassOf>\r\n" + "		<owl:Restriction>\r\n"
											+ "				<owl:onProperty rdf:resource=\"http://www.reiter.at/ontology/generated#"
											+ attributeName + "\"/>\r\n"
											+ "				<owl:qualifiedCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#nonNegativeInteger\">"
											+ nodeCounter + "</owl:qualifiedCardinality>\r\n"
											+ "				<owl:onClass rdf:resource=\"http://www.reiter.at/ontology/generated#"
											+ childNode + "\"/>\r\n" + "			</owl:Restriction>\r\n"
											+ "		</rdfs:subClassOf>\r\n" + "	</owl:Class>" + "")
									.toString();
						}
						// create data property
						else {

							dataProperties = new StringBuilder(dataProperties)
									.append("<owl:DatatypeProperty rdf:about=\"http://www.reiter.at/ontology/generated#"
											+ attributeName + "\"/>\r\n")
									.toString();

							restriction = new StringBuilder(restriction)
									.append("    <owl:Class rdf:about=\"http://www.reiter.at/ontology/generated#"
											+ childNode + "\">\r\n" + "        <rdfs:subClassOf>\r\n"
											+ "            <owl:Restriction>\r\n"
											+ "                <owl:onProperty rdf:resource=\"http://www.reiter.at/ontology/generated#"
											+ attributeName + "\"/>\r\n"
											+ "                <owl:qualifiedCardinality rdf:datatype=\"http://www.w3.org/2001/XMLSchema#nonNegativeInteger\">1</owl:qualifiedCardinality>\r\n"
											+ "                <owl:onDataRange rdf:resource=\"http://www.w3.org/2001/XMLSchema#"
											+ attributeType + "\"/>\r\n" + "            </owl:Restriction>\r\n"
											+ "        </rdfs:subClassOf>\r\n" + "    </owl:Class>")
									.toString();
						}
					}
				}
			}

			// Write it to Output File
			System.out.println("Write output file");

			// finish rdf:RDF Tag
			rdfNode = new StringBuilder(rdfNode).append("xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\r\n")
					.append("<owl:Ontology rdf:about=\"http://www.reiter.at/ontology/generated\"/>").toString();

			writer.println(rdfNode);
			writer.println("<!-- \r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "//\r\n" + "// objectProperties\r\n" + "//\r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "     -->");
			writer.println(objectProperties);

			writer.println("<!-- \r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "//\r\n" + "// dataProperties\r\n" + "//\r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "     -->");
			writer.println(dataProperties);
			writer.println("<!-- \r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "//\r\n" + "// restriction\r\n" + "//\r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "     -->");
			writer.println(restriction);
			writer.println("<!-- \r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "//\r\n" + "// namedIndividuals\r\n" + "//\r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "     -->");
			writer.println(namedIndividuals);
			writer.println("<!-- \r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "//\r\n" + "// classNodes\r\n" + "//\r\n"
					+ "///////////////////////////////////////////////////////////////////////////////////////\r\n"
					+ "     -->");
			writer.println(classNodes);

			// Close rdf:RDF Tag
			writer.println("</rdf:RDF>\r\n");

			System.out.println("BPMN2 successfully transformed to OWL! :)\n");

			// Close Writer
			writer.close();

			removeEmptyNamespace(src);

			Path originalInput = Paths.get(src);
			Files.delete(originalInput);

			Path writtenInput = Paths.get(src+"2");
			Files.move(writtenInput, writtenInput.resolveSibling(src));
			
			IPPRvsTeilOntology.main(argv);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void removeEmptyNamespace(String src) throws IOException {
		FileInputStream fstream = new FileInputStream(src);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		BufferedWriter writer = new BufferedWriter(new FileWriter(src+"2"));

		String strLine;
		while ((strLine = br.readLine()) != null) {
			if (strLine.contains("xmlns=\"\"")) {
				writer.write(strLine.replace("xmlns=\"\"", ""));
			} else {
				writer.write(strLine);
			}
		}
		writer.close();
		br.close();

	}

	private static String workaroundPräfixIssues(String attributeName) {
		// TODO find all präfix possibilities
		if (attributeName.startsWith("xmlns:")) {
			attributeName = "ResolveNamingIssuexmlns" + attributeName.replace("xmlns:", "");
		} else if (attributeName.startsWith("xsi:")) {
			attributeName = "ResolveNamingIssuexsi" + attributeName.replace("xsi:", "");
		} else if (attributeName.startsWith("ext:")) {
			attributeName = "ResolveNamingIssueext" + attributeName.replace("ext:", "");
		}
		return attributeName;
	}

	private static void writeXmlFile(Node doc, String src) throws TransformerException {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(src));
		transformer.transform(source, result);
		// Output to console for testing
		// StreamResult consoleResult = new StreamResult(System.out);
		// transformer.transform(source, consoleResult);

	}

	public static String createNamedIndividuals(Node childNode, String namedIndividuals) {
		try {
			// Text Content can be empty or set
			String textValue = "";
			try {
				textValue = childNode.getFirstChild().getNodeValue().replaceAll("\\s+", "");
			} catch (Exception e) {
			}
			// check if parent is root
			String parentName = childNode.getParentNode().getNodeName().toString().replace(":", "_");
			if (parentName.equals("#document")) {
				parentName = "document_IC0";
			}
			// create named individual
			String childName = childNode.getNodeName().toString().replace(":", "_");
			String name = childNode.getNodeName().toString().split("_IC")[0];
			namedIndividuals = new StringBuilder(namedIndividuals)
					.append("" + "\r\n<owl:NamedIndividual rdf:about=\"http://www.reiter.at/ontology/generated/"
							+ childName + "-belongsTo-" + parentName + "\">\r\n"
							+ "<rdf:type rdf:resource=\"http://www.reiter.at/ontology/generated#" + name + "\"/>\r\n")
					.toString();
			// Add text content of node
			if (!textValue.equals("")) {
				namedIndividuals = new StringBuilder(namedIndividuals)
						.append("<textValue rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + textValue
								+ "</textValue>\r\n")
						.toString();
			}
			// Add Attributes to NamedIndividual
			NamedNodeMap nodeAttributes = childNode.getAttributes();
			for (int a = 0; a < nodeAttributes.getLength(); a++) {
				String fixedAttributeName = workaroundPräfixIssues(
						nodeAttributes.item(a).toString().split("\\=")[0].replaceAll("\"", ""));
				namedIndividuals = new StringBuilder(namedIndividuals)
						.append("<" + fixedAttributeName + " rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
								+ nodeAttributes.item(a).toString().split("\\=")[1].replaceAll("\"", "") + "</"
								+ fixedAttributeName + ">\r\n")
						.toString();
			}
			// End NamedIndividual
			namedIndividuals = new StringBuilder(namedIndividuals).append("</owl:NamedIndividual>\r\n").toString();
		} catch (Exception e) {
		}
		return namedIndividuals;
	}
}