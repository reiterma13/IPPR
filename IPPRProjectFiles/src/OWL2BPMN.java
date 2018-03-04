import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Written by Martina Reiter :)
 * 
 * Transforms OLW_OUTPUT.owl to BPMN_Generated.xml.
 * 
 * @param args
 * @throws ParserConfigurationException
 * @throws SAXException
 * @throws IOException
 */


public class OWL2BPMN {

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

		System.out.println("Load OWL_OUTPUT ... ");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document owlDocument = documentBuilder.parse(new File("src/owl/OWL_OUTPUT.owl"));
		owlDocument.getElementsByTagName("*");
		File file = new File("src/owl/OWL_OUTPUT.owl");
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		String xmlns = "";

		System.out.println("Create File BPMN_Generated ... ");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology o = manager.loadOntologyFromOntologyDocument(file);
			Set<String> owlClassSet = new HashSet<String>();
			Element childElement = null;
			Element rootElement = null;
			
			System.out.println("Get xml namespace");
			NodeList nodeList = owlDocument.getElementsByTagName("*");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					if (node.getNodeName().equals("xmlns")) {
						xmlns = node.getTextContent();
					}
				}
			}			

			System.out.println("Create Root ... ");
			for (OWLClass cls : o.getClassesInSignature()) {
				OWLReasoner reasoner = reasonerFactory.createReasoner(o);
				NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true);
				for (OWLNamedIndividual i : instances.getFlattened()) {
					String individualWithParentInformation = i.getIRI().getFragment();
					String child = individualWithParentInformation.split("-belongsTo-")[0];
					String parent = individualWithParentInformation.split("-belongsTo-")[1];
					if (parent.equals("document_IC0")) {
						// add root to doc
						rootElement = doc.createElement(child);
						doc.appendChild(rootElement);
						setNodeAttributes(i, o, rootElement, doc);
					}
				}
			}

			System.out.println("Create Nodes ... ");
			for (OWLClass cls : o.getClassesInSignature()) {
				OWLReasoner reasoner = reasonerFactory.createReasoner(o);
				NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true); // true gets also indirect //
																							// children
				for (OWLNamedIndividual i : instances.getFlattened()) {
					String individualWithParentInformation = i.getIRI().getFragment();
					owlClassSet.add(individualWithParentInformation);
					String parentNodeId = individualWithParentInformation.split("-belongsTo-")[1].split("_IC")[0];
					if (!parentNodeId.equals("document")) {
						String child = individualWithParentInformation.split("-belongsTo-")[0];
						individualWithParentInformation.split("-belongsTo-");
						childElement = doc.createElement(child);
						// add child node to root node
						rootElement.appendChild(childElement);
						setNodeAttributes(i, o, childElement, doc);
					}
				}
			}

			System.out.println("Create Hierarchy ... ");
			for (OWLClass cls : o.getClassesInSignature()) {
				OWLReasoner reasoner = reasonerFactory.createReasoner(o);
				NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true); // true gets also indirect
																							// children
				for (OWLNamedIndividual i : instances.getFlattened()) {
					String individualWithParentInformation = i.getIRI().getFragment();
					owlClassSet.add(individualWithParentInformation);
					String parentNodeId = individualWithParentInformation.split("-belongsTo-")[1].split("_IC")[0];
					if (!parentNodeId.equals("document")) {
						String child = individualWithParentInformation.split("-belongsTo-")[0];
						String parent = individualWithParentInformation.split("-belongsTo-")[1];
						doc.createElement(child);
						NodeList childNodeList = doc.getElementsByTagName(child);
						Node currentChild = null;
						for (int n = 0; n < childNodeList.getLength(); n++) {
							currentChild = childNodeList.item(n);
						}
						NodeList parentNodeList = doc.getElementsByTagName(parent);
						Node currentParent = null;
						for (int n = 0; n < parentNodeList.getLength(); n++) {
							currentParent = parentNodeList.item(n);
						}
						currentParent.appendChild(currentChild);
					}
				}
			}
			writeXmlFile(doc);
			orderSiblings(doc, xmlns);
			System.out.println("OWL successfully transformed to BPMN! :) \r\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void renameNodes(Document doc, String xmlns) throws TransformerException {
		NodeList nodeList = doc.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String originalNodeName = node.getNodeName().split("_IC")[0].replace("_", ":");
				doc.renameNode(node, "", originalNodeName);
				writeXmlFile(doc);
			}
		}
	}

	private static void orderSiblings(Document doc, String xmlns)
			throws ParserConfigurationException, SAXException, IOException, TransformerException {

		// get all siblings of a node and order it based on the individual counter
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document document = docBuilder.parse(new File("src/bpmn/BPMN_Generated.xml"));
		NodeList nodeList = document.getElementsByTagName("*");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			Node parent = node.getParentNode();
			NodeList sibs = parent.getChildNodes();
			Set<Integer> sortInts = new HashSet<Integer>();

			// add siblings to set
			for (int x = 0; x < sibs.getLength(); x++) {
				try {
					sortInts.add(Integer.parseInt(sibs.item(x).getNodeName().split("_IC")[1].split("_CN")[0]));

				} catch (Exception e) {
				}
			}

			// sort individual counter
			TreeSet<Integer> tset = new TreeSet<Integer>(sortInts);
			for (int sd : tset) {
				for (int x = 0; x < sibs.getLength(); x++) {
					Node si = sibs.item(x);
					try {
						if (sd == Integer.parseInt(si.getNodeName().split("_IC")[1].split("_CN")[0])) {
							try {
								parent.appendChild(si);
							} catch (Exception e) {
							}
						}
					} catch (Exception e) {
					}
				}
			}
		}
		writeXmlFile(document);
		System.out.println("Rename Nodes ... ");
		renameNodes(document, xmlns);
		removeEmptyNamespace(xmlns);
	}
	
	private static void removeEmptyNamespace(String xmlns) throws IOException {
		FileInputStream fstream = new FileInputStream("src/bpmn/BPMN_Generated.xml");
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		BufferedWriter writer = new BufferedWriter(new FileWriter("src/bpmn/BPMN_Generated2.xml"));

		String strLine;
		while ((strLine = br.readLine()) != null)   {
			if(strLine.contains("xmlns=\"\"")) {
				  writer.write(strLine.replace("xmlns=\"\"", "xmlns=\""+xmlns+"\""));
			}else {
				writer.write(strLine);
			}
		}
		writer.close();
		br.close();		
		
		Path originalInput = Paths.get("src/bpmn/BPMN_Generated.xml");
		Files.delete(originalInput);

		Path writtenInput = Paths.get("src/bpmn/BPMN_Generated2.xml");
		Files.move(writtenInput, writtenInput.resolveSibling("BPMN_Generated.xml"));


	}

	private static void writeXmlFile(Node doc) throws TransformerException {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File("src/bpmn/BPMN_Generated.xml"));
		transformer.transform(source, result);
		// Output to console for testing
		// StreamResult consoleResult = new StreamResult(System.out);
		// transformer.transform(source, consoleResult);

	}

	private static void setNodeAttributes(OWLNamedIndividual i, OWLOntology o, Element childElement, Document doc) {
		Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(i.getIRI(), o);
		for (OWLAnnotation a : annotations) {
			String annotationProperty = a.getProperty().toString().replaceAll("<", "").replaceAll(">", "");
			annotationProperty = resolveNamingIssues(annotationProperty);

			if (annotationProperty.equals("textValue")) {
				childElement.setTextContent(a.toString().split("\\\"")[1]);
			} else {
				Attr attr = doc.createAttribute(annotationProperty);
				attr.setValue(a.toString().split("\\\"")[1]);
				childElement.setAttributeNode(attr);
			}
		}
	}

	private static String resolveNamingIssues(String annotationProperty) {
		if (annotationProperty.startsWith("ResolveNamingIssuexsi")) {
			annotationProperty = annotationProperty.replaceAll("ResolveNamingIssuexsi", "xsi:");
		} else if (annotationProperty.startsWith("ResolveNamingIssuexmlns")) {
			annotationProperty = annotationProperty.replaceAll("ResolveNamingIssuexmlns", "xmlns:");
		} else if (annotationProperty.startsWith("ResolveNamingIssueext")) {
			annotationProperty = annotationProperty.replaceAll("ResolveNamingIssueext", "ext:");
		}
		return annotationProperty;
	}
}
