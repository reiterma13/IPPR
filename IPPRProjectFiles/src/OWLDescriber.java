import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Written by Martina Reiter
 * 
 * Compares IPPR.owl with BUEPA.owl.
 * 
 * @param args
 * 
 */

public class OWLDescriber {

	@SuppressWarnings({})
	public static void main(String[] args) {

		try {

			Scanner scanner = new Scanner(System.in);
		

			File dir = new File("diffs");
			dir.mkdir();

			System.out.println("Enter file name (eg. IPPR.owl):");
			String fileName1 = scanner.nextLine();

			String fileName2 = fileName1;

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			String filePath2 = "diffs/";

			Document document = docBuilder.parse(new File(fileName1));
			Document document2 = docBuilder.parse(new File(fileName2));

			PrintWriter printWriter = new PrintWriter(filePath2 + "owlDescriberLog.txt");

			Set<String> totalSet = new HashSet<String>();

			Set<String> hierarchySet = new HashSet<String>();

			/////////////////////////////////////////////////////////////////////////
			/////////////////// READ FILE 1
			/////////////////////////////////////////////////////////////////////////

			File file1 = new File(fileName1);
			OWLOntologyManager manager1 = OWLManager.createOWLOntologyManager();
			OWLOntology ontology1;
			Set<String> classSet = new HashSet<String>();
			Set<String> objectPropertiesSet = new HashSet<String>();
			Set<String> dataPropertiesSet = new HashSet<String>();

			try {
				System.out.println("READ " + fileName1);
				ontology1 = manager1.loadOntologyFromOntologyDocument(file1);

				for (OWLClass x : ontology1.getClassesInSignature()) {

					classSet.add(x.getIRI().getShortForm());
					totalSet.add(x.getIRI().getShortForm());

				}
				for (OWLObjectProperty x : ontology1.getObjectPropertiesInSignature()) {
					objectPropertiesSet.add(x.getIRI().getShortForm());
					totalSet.add(x.getIRI().getShortForm());

				}
				for (OWLDataProperty x : ontology1.getDataPropertiesInSignature()) {
					dataPropertiesSet.add(x.getIRI().getShortForm());
					totalSet.add(x.getIRI().getShortForm());

				}
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}
			printWriter.println("-------------------------------------------------------------------------");
			printWriter
					.println("The IPPR ontology contains " + classSet.size() + " classes, " + objectPropertiesSet.size()
							+ " object properties and " + dataPropertiesSet.size() + " data properties.\n");

			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println("\nThe following classes are defined: \n");
			printWriter.println("-------------------------------------------------------------------------");
			sortStringSet(classSet).forEach(printWriter::println);

			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println("\nThe following data properties are defined: \n");
			printWriter.println("-------------------------------------------------------------------------");
			sortStringSet(dataPropertiesSet).forEach(printWriter::println);

			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println("\nThe following object properties are defined: \n");
			printWriter.println("-------------------------------------------------------------------------");
			sortStringSet(objectPropertiesSet).forEach(printWriter::println);

			/////////////////////////////////////////////////////////////////////////
			/////////////////// READ FILE 2
			/////////////////////////////////////////////////////////////////////////

			File file2 = new File(fileName2);
			OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
			OWLOntology ontology2;
			Set<String> classSet2 = new HashSet<String>();
			Set<String> objectPropertiesSet2 = new HashSet<String>();
			Set<String> dataPropertiesSet2 = new HashSet<String>();

			try {
				System.out.println("READ " + fileName2);
				ontology2 = manager2.loadOntologyFromOntologyDocument(file2);

				for (OWLClass x : ontology2.getClassesInSignature()) {
					classSet2.add(x.getIRI().getShortForm());
				}
				for (OWLObjectProperty x : ontology2.getObjectPropertiesInSignature()) {
					objectPropertiesSet2.add(x.getIRI().getShortForm());
				}
				for (OWLDataProperty x : ontology2.getDataPropertiesInSignature()) {
					dataPropertiesSet2.add(x.getIRI().getShortForm());
				}
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// CLASSES
			/////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////
			/////////////////// OBJECT PROPERTIES
			/////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////
			/////////////////// DATA PROPERTIES
			/////////////////////////////////////////////////////////////////////////

			/////////////////////////////////////////////////////////////////////////
			/////////////////// OBJECT RESTRICTIONS of File 1
			/////////////////////////////////////////////////////////////////////////

			System.out.println("Write undefinedDataProperties.txt");
			Set<String> objectRestrictionSet1 = new HashSet<String>();
			NodeList nodeList = document.getElementsByTagName("*");

			// get restrictions of file 1
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				String currentClass = "";
				String hasObjectProperty = "";
				String hasRefTo = "";
				String hasCardinality = "?";
				String hasRestrictionType = "some";

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					// get owl:onClass Node
					if (nodeName.equals("owl:onClass")) {
						// get class where object property is used
						NamedNodeMap classNodeMap = node.getParentNode().getParentNode().getParentNode()
								.getAttributes();
						for (int ab = 0; ab < classNodeMap.getLength(); ab++) {
							currentClass = classNodeMap.item(ab).toString().split("\\#")[1].replaceAll("\"", "");
						}
						// get object property
						hasObjectProperty = getObjectProperty(node, hasObjectProperty);

						// get restriction type and cardinality
						hasRestrictionType = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality,
								node)[0];
						hasCardinality = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality, node)[1];

						// get class to which the object property references to
						NamedNodeMap nodeAttributes = node.getAttributes();
						for (int a = 0; a < nodeAttributes.getLength(); a++) {
							hasRefTo = nodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");
							if (currentClass.equals("Definitions")) {
							} else {
								String restriction = currentClass + " has " + hasObjectProperty + " "
										+ hasRestrictionType + " " + hasCardinality + " " + hasRefTo;
								objectRestrictionSet1.add(restriction);

								String restrictionTS = currentClass + " has the object property " + hasObjectProperty
										+ " with the restriction type " + hasRestrictionType + " and the cardinality "
										+ hasCardinality + " refering to the class " + hasRefTo + ".";
								totalSet.add(restrictionTS);
							}
						}
					}
				}
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// OBJECT RESTRICTIONS of File 2
			/////////////////////////////////////////////////////////////////////////
			Set<String> objectRestrictionSet2 = new HashSet<String>();
			NodeList nodeList2 = document2.getElementsByTagName("*");

			for (int i = 0; i < nodeList2.getLength(); i++) {

				Node node = nodeList2.item(i);
				String currentClass = "";
				String hasObjectProperty = "";
				String hasRefTo = "";
				String hasCardinality = "?";
				String hasRestrictionType = "some";

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();

					// get owl:onClass Node
					if (nodeName.equals("owl:onClass")) {

						// get class where object property is used
						NamedNodeMap classNodeMap = node.getParentNode().getParentNode().getParentNode()
								.getAttributes();
						for (int ab = 0; ab < classNodeMap.getLength(); ab++) {
							currentClass = classNodeMap.item(ab).toString().split("\\#")[1].replaceAll("\"", "");
						}
						// get object property
						hasObjectProperty = getObjectProperty(node, hasObjectProperty);

						// get restriction type and cardinality
						hasRestrictionType = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality,
								node)[0];
						hasCardinality = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality, node)[1];

						// get class to which the object property references to
						NamedNodeMap nodeAttributes = node.getAttributes();
						for (int a = 0; a < nodeAttributes.getLength(); a++) {
							hasRefTo = nodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");
							if (currentClass.equals("Definitions")) {
							} else {
								String restriction = currentClass + " has " + hasObjectProperty + " "
										+ hasRestrictionType + " " + hasCardinality + " " + hasRefTo;
								objectRestrictionSet2.add(restriction);
							}
						}
					}
				}
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// Missing Object Restrictions
			/////////////////////////////////////////////////////////////////////////

			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println(fileName1 + " has " + objectRestrictionSet1.size() + " object restrictions.");
			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println("\nThe following object restrictions are defined\n");
			printWriter.println("-------------------------------------------------------------------------");
			sortStringSet(objectRestrictionSet1).forEach(printWriter::println);

			Set<String> undefinedRestrictions = new HashSet<String>();
			Set<String> undefinedRestrictions2 = new HashSet<String>();

			// get restriction differences
			Set<String> undefinedObjectRestrictions = new HashSet<String>();
			for (String s : undefinedRestrictions) {
				undefinedObjectRestrictions.add(s + " " + fileName1);
			}
			for (String s : undefinedRestrictions2) {
				undefinedObjectRestrictions.add(s + " " + fileName2);
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// DATA RESTRICTIONS of File 1
			/////////////////////////////////////////////////////////////////////////

			System.out.println("Write dataProperties.txt");
			Set<String> dataRestrictionSet1 = new HashSet<String>();
			for (int i = 0; i < nodeList.getLength(); i++) {

				Node node = nodeList.item(i);
				String currentClass = "";
				String hasDataProperty = "";
				String type = "";
				String hasCardinality = "?";
				String hasRestrictionType = "some";

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					// get owl:onClass Node
					if (nodeName.equals("owl:onDataRange")) {
						// get class where data property is uses
						NamedNodeMap classNodeMap = node.getParentNode().getParentNode().getParentNode()
								.getAttributes();
						for (int ab = 0; ab < classNodeMap.getLength(); ab++) {
							currentClass = classNodeMap.item(ab).toString().split("\\#")[1].replaceAll("\"", "");
						}
						// get data property
						hasDataProperty = getObjectProperty(node, hasDataProperty);

						// get restriction type and cardinality
						hasRestrictionType = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality,
								node)[0];
						hasCardinality = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality, node)[1];

						// get class to which the data property references to
						NamedNodeMap nodeAttributes = node.getAttributes();
						for (int a = 0; a < nodeAttributes.getLength(); a++) {
							type = nodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");
							if (currentClass.equals("Definitions")) {
							} else {
								String restriction = currentClass + " has " + hasDataProperty + " " + hasRestrictionType
										+ " " + hasCardinality + " " + type;
								dataRestrictionSet1.add(restriction);

								String restrictionTS = currentClass + " has the data property " + hasDataProperty
										+ " with the restriction type " + hasRestrictionType + " and the cardinality "
										+ hasCardinality + " refering to the type " + type + ".";
								totalSet.add(restrictionTS);

							}
						}
					}
				}
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// DATA RESTRICTIONS of File 2
			/////////////////////////////////////////////////////////////////////////
			Set<String> dataRestrictionSet2 = new HashSet<String>();

			for (int i = 0; i < nodeList2.getLength(); i++) {

				Node node = nodeList2.item(i);
				String currentClass = "";
				String hasDataProperty = "";
				String type = "";
				String hasCardinality = "?";
				String hasRestrictionType = "some";

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					// get owl:onClass Node
					if (nodeName.equals("owl:onDataRange")) {
						// get class where data property is uses
						NamedNodeMap classNodeMap = node.getParentNode().getParentNode().getParentNode()
								.getAttributes();
						for (int ab = 0; ab < classNodeMap.getLength(); ab++) {
							currentClass = classNodeMap.item(ab).toString().split("\\#")[1].replaceAll("\"", "");
						}
						// get data property
						hasDataProperty = getObjectProperty(node, hasDataProperty);

						// get restriction type and cardinality
						hasRestrictionType = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality,
								node)[0];
						hasCardinality = getRestrictionTypeAndCardinality(hasRestrictionType, hasCardinality, node)[1];

						// get class to which the data property references to
						NamedNodeMap nodeAttributes = node.getAttributes();
						for (int a = 0; a < nodeAttributes.getLength(); a++) {
							type = nodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");
							if (currentClass.equals("Definitions")) {
							} else {
								String restriction = currentClass + " has " + hasDataProperty + " " + hasRestrictionType
										+ " " + hasCardinality + " " + type;
								dataRestrictionSet2.add(restriction);
							}
						}
					}
				}
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// Missing Data Restrictions
			/////////////////////////////////////////////////////////////////////////

			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println(fileName1 + " has " + dataRestrictionSet1.size() + " data restrictions.");
			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println("\nThe following data restrictions are defined\n");
			printWriter.println("-------------------------------------------------------------------------");
			sortStringSet(dataRestrictionSet1).forEach(printWriter::println);

			Set<String> undefinedDataRestrictions = new HashSet<String>();
			undefinedDataRestrictions = compareSet(dataRestrictionSet1, dataRestrictionSet2);
			Set<String> undefinedDataRestrictions2 = new HashSet<String>();
			undefinedDataRestrictions2 = compareSet(dataRestrictionSet2, dataRestrictionSet1);

			// get restriction differences
			Set<String> dataRestrictionsDifferences = new HashSet<String>();
			for (String s : undefinedDataRestrictions) {
				dataRestrictionsDifferences.add(s + " " + fileName1);
			}
			for (String s : undefinedDataRestrictions2) {
				dataRestrictionsDifferences.add(s + " " + fileName2);
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// SubClassesOf File 1
			/////////////////////////////////////////////////////////////////////////

			System.out.println("Write subClassOfDifferences.txt");

			Set<String> subClassOfFile1 = new HashSet<String>();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();

					// get rdfs:subClassOf Node
					if (nodeName.equals("rdfs:subClassOf")) {

						// get parents
						NamedNodeMap nodeAttributes = node.getAttributes();
						for (int a = 0; a < nodeAttributes.getLength(); a++) {
							String parent = nodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");

							// get child
							NamedNodeMap childNodeAttributes = node.getParentNode().getAttributes();
							for (int at = 0; at < childNodeAttributes.getLength(); at++) {
								String child = childNodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");
								subClassOfFile1.add(child + " is subClassOf " + parent);
								String subClassOfTS = child + " is a sub class of the class " + parent + ".";
								totalSet.add(subClassOfTS);

								totalSet.add(parent + " is the parent class of the class " + child + ".");

							}
						}
					}
				}
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// SubClassesOf File 2
			/////////////////////////////////////////////////////////////////////////

			Set<String> subClassOfFile2 = new HashSet<String>();
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node node = nodeList2.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();

					// get rdfs:subClassOf Node
					if (nodeName.equals("rdfs:subClassOf")) {

						// get parents
						NamedNodeMap nodeAttributes = node.getAttributes();
						for (int a = 0; a < nodeAttributes.getLength(); a++) {
							String parent = nodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");

							// get child
							NamedNodeMap childNodeAttributes = node.getParentNode().getAttributes();
							for (int at = 0; at < childNodeAttributes.getLength(); at++) {
								String child = childNodeAttributes.item(a).toString().split("\\#")[1].replace("\"", "");

								subClassOfFile2.add(child + " is subClassOf " + parent);

							}
						}
					}
				}
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// SubClassOf Differences
			/////////////////////////////////////////////////////////////////////////

			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println(fileName1 + " has " + subClassOfFile1.size() + " subclasses.");
			printWriter.println("-------------------------------------------------------------------------");
			printWriter.println("\nThe following subclasses are defined\n");
			printWriter.println("-------------------------------------------------------------------------");
			sortStringSet(subClassOfFile1).forEach(printWriter::println);

			Set<String> undefinedSubClasses = new HashSet<String>();
			undefinedSubClasses = compareSet(subClassOfFile1, subClassOfFile2);

			Set<String> undefinedSubClasses2 = new HashSet<String>();
			undefinedSubClasses2 = compareSet(subClassOfFile2, subClassOfFile1);

			// get restriction differences
			Set<String> subClassDifferences = new HashSet<String>();
			for (String s : undefinedSubClasses) {
				subClassDifferences.add(s + " " + fileName1);
			}
			for (String s : undefinedSubClasses2) {
				subClassDifferences.add(s + " " + fileName2);
			}
			/////////////////////////////////////////////////////////////////////////
			/////////////////// Individuals of File 1
			/////////////////////////////////////////////////////////////////////////

			OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			File file = new File(fileName1);
			OWLOntology o1 = manager.loadOntologyFromOntologyDocument(file);
			Set<String> individualsFile1 = new HashSet<String>();

			for (OWLClass cls : o1.getClassesInSignature()) {
				OWLReasoner reasoner = reasonerFactory.createReasoner(o1);
				NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true);

				for (OWLNamedIndividual i : instances.getFlattened()) {
					individualsFile1.add(i.getIRI().getFragment());

				}
			}

			/////////////////////////////////////////////////////////////////////////
			/////////////////// Individuals of File 2
			/////////////////////////////////////////////////////////////////////////

			OWLReasonerFactory reasonerFactory2 = new StructuralReasonerFactory();
			OWLOntologyManager manager21 = OWLManager.createOWLOntologyManager();
			File file21 = new File(fileName2);
			OWLOntology o2 = manager21.loadOntologyFromOntologyDocument(file21);
			Set<String> individualsFile2 = new HashSet<String>();

			for (OWLClass cls : o2.getClassesInSignature()) {
				OWLReasoner reasoner = reasonerFactory2.createReasoner(o2);
				NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, true);

				for (OWLNamedIndividual i : instances.getFlattened()) {
					individualsFile2.add(i.getIRI().getFragment());
				}
			}
			/////////////////////////////////////////////////////////////////////////
			/////////////////// individuals Differences
			/////////////////////////////////////////////////////////////////////////
			// TODO show diffs

			Set<String> undefinedIndividualsClasses = compareSet(individualsFile1, individualsFile2);
			Set<String> undefinedIndividualsClasses2 = compareSet(individualsFile2, individualsFile1);

			printWriter.close();

			try {
				System.out.println("BPMNDescriber done");

				// Runtime.getRuntime().exec("taskkill /f /im cmd.exe");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("BPMNDescriber done");
			}

		} catch (Exception e) {
		}
	}

	private static Set<String> compareSet(Set<String> set1, Set<String> set2) {
		Set<String> defined = new HashSet<String>();
		Set<String> undefined = new HashSet<String>();
		for (String s : set1) {
			if (set2.contains(s)) {
				defined.add(s);
			} else {
				undefined.add(s);
			}
		}
		return undefined;
	}

	private static List<String> sortStringSet(Collection<String> unsortedList) {
		List<String> sortedList = new ArrayList<String>(unsortedList);
		Collections.sort(sortedList);
		return sortedList;
	}

	private static String getObjectProperty(Node node, String hasObjectProperty) {
		NamedNodeMap nodeAttributesFromOnProperty = node.getParentNode().getFirstChild().getNextSibling()
				.getAttributes();
		for (int ab = 0; ab < nodeAttributesFromOnProperty.getLength(); ab++) {
			hasObjectProperty = nodeAttributesFromOnProperty.item(ab).toString().split("\\#")[1].replaceAll("\"", "");
			hasObjectProperty = Character.toString(hasObjectProperty.charAt(0)).toLowerCase()
					+ hasObjectProperty.substring(1);

			if (hasObjectProperty.startsWith("bPMN")) {
				hasObjectProperty = hasObjectProperty.split("\\_")[0].replaceFirst("bPMN", "");
				hasObjectProperty = Character.toString(hasObjectProperty.charAt(0)).toLowerCase()
						+ hasObjectProperty.substring(1);
			}
		}
		return hasObjectProperty;
	}

	private static String[] getRestrictionTypeAndCardinality(String hasRestrictionType, String hasCardinality,
			Node node) {
		NodeList restrictionChilds = node.getParentNode().getChildNodes();
		for (int rc = 0; rc < restrictionChilds.getLength(); rc++) {
			String child = restrictionChilds.item(rc).getNodeName();
			String cardinality = restrictionChilds.item(rc).getTextContent();
			if (child.equals("owl:minQualifiedCardinality")) {
				hasRestrictionType = "min";
				hasCardinality = cardinality;
			}
			if (child.equals("owl:maxQualifiedCardinality")) {
				hasRestrictionType = "max";
				hasCardinality = cardinality;
			}
			if (child.equals("owl:qualifiedCardinality")) {
				hasRestrictionType = "exactly";
				hasCardinality = cardinality;
			}
		}
		return new String[] { hasRestrictionType, hasCardinality };
	}
}
