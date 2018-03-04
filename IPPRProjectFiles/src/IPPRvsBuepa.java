import java.io.File;
import java.io.PrintWriter;
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

public class IPPRvsBuepa {

	@SuppressWarnings({ })
	public static void main(String[] args) {

		try {
			
			Scanner scanner = new Scanner(System.in);
			System.out.println("Compare Ontologies:");
			System.out.println("---------------------------------------------------");

			File dir = new File("diffs");
			dir.mkdir();
			
			System.out.println("Please enter file name 1:");
			String fileName1 = scanner.nextLine();
			
			System.out.println("Please enter file name 2:");
			String fileName2 = scanner.nextLine();
			
			
			
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			String filePath2 = "diffs/";
			
			Document document = docBuilder.parse(new File(fileName1));
			Document document2 = docBuilder.parse(new File(fileName2));

			PrintWriter undefinedClassesWriter = new PrintWriter(filePath2 + "undefinedClasses.txt");
			PrintWriter undefinedObjectPropertiesWriter = new PrintWriter(filePath2 + "undefinedObjectProperties.txt");
			PrintWriter undefinedDataPropertiesWriter = new PrintWriter(filePath2 + "undefinedDataProperties.txt");
			PrintWriter undefinedObjectRestrictionsWriter = new PrintWriter(
					filePath2 + "undefinedObjectRestrictions.txt");
			PrintWriter undefinedDataRestrictionsWriter = new PrintWriter(filePath2 + "undefinedDataRestrictions.txt");
			PrintWriter subClassOfDifferencesWriter = new PrintWriter(filePath2 + "subClassOfDifferences.txt");
			PrintWriter undefinedIndividualsDifferencesWriter = new PrintWriter(filePath2 + "undefinedIndividuals.txt");

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
				}
				for (OWLObjectProperty x : ontology1.getObjectPropertiesInSignature()) {
					objectPropertiesSet.add(x.getIRI().getShortForm());
				}
				for (OWLDataProperty x : ontology1.getDataPropertiesInSignature()) {
					dataPropertiesSet.add(x.getIRI().getShortForm());
				}
			} catch (OWLOntologyCreationException e) {
				e.printStackTrace();
			}

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

			System.out.println("Write undefinedClasses.txt");
			undefinedClassesWriter.println("\n------------------------UNDEFINED CLASSES---------------------------\n");
			undefinedClassesWriter.println(fileName1 + " contains " + classSet.size() + " classes.");
			undefinedClassesWriter.println(fileName2 + " contains " + classSet2.size() + " classes.");

			Set<String> undefinedClassSet = compareSet(classSet, classSet2);
			undefinedClassesWriter
					.println("\n" + undefinedClassSet.size() + " classes are missing in the file " + fileName2);
			undefinedClassesWriter.println("--------------------------------------------------------------------");
			sortStringSet(undefinedClassSet).forEach(undefinedClassesWriter::println);

			Set<String> undefinedClassSet2 = compareSet(classSet2, classSet);
			undefinedClassesWriter
					.println("\n" + undefinedClassSet2.size() + " classes are missing in the file " + fileName1);
			undefinedClassesWriter.println("--------------------------------------------------------------------");
			sortStringSet(undefinedClassSet2).forEach(undefinedClassesWriter::println);
			undefinedClassesWriter.close();

			/////////////////////////////////////////////////////////////////////////
			/////////////////// OBJECT PROPERTIES
			/////////////////////////////////////////////////////////////////////////

			System.out.println("Write undefinedObjectProperties.txt");
			undefinedObjectPropertiesWriter
					.println("\n------------------------OBJECT PROPERTIES---------------------------\n");
			undefinedObjectPropertiesWriter
					.println(fileName1 + " contains " + objectPropertiesSet.size() + " object properties.");
			undefinedObjectPropertiesWriter
					.println(fileName2 + " contains " + objectPropertiesSet2.size() + " object properties.");

			Set<String> undefinedObjectProperties = compareSet(objectPropertiesSet, objectPropertiesSet2);
			undefinedObjectPropertiesWriter.println("\n" + undefinedObjectProperties.size()
					+ " object properties are missing in the file " + fileName2);
			undefinedObjectPropertiesWriter
					.println("--------------------------------------------------------------------");
			sortStringSet(undefinedObjectProperties).forEach(undefinedObjectPropertiesWriter::println);

			Set<String> undefinedObjectProperties2 = compareSet(objectPropertiesSet2, objectPropertiesSet);
			undefinedObjectPropertiesWriter.println("\n" + undefinedObjectProperties2.size()
					+ " object properties are missing in the file " + fileName1);
			undefinedObjectPropertiesWriter
					.println("--------------------------------------------------------------------");
			sortStringSet(undefinedObjectProperties2).forEach(undefinedObjectPropertiesWriter::println);
			undefinedObjectPropertiesWriter.close();

			/////////////////////////////////////////////////////////////////////////
			/////////////////// DATA PROPERTIES
			/////////////////////////////////////////////////////////////////////////

			System.out.println("Write undefinedDataProperties.txt");
			undefinedDataPropertiesWriter
					.println("\n------------------------DATA PROPERTIES---------------------------\n");
			undefinedDataPropertiesWriter
					.println(fileName1 + " contains " + dataPropertiesSet.size() + " data properties.");
			undefinedDataPropertiesWriter
					.println(fileName2 + " contains " + dataPropertiesSet2.size() + " data properties.");

			Set<String> undefinedDataPropertiesSet = compareSet(dataPropertiesSet, dataPropertiesSet2);
			undefinedDataPropertiesWriter.println(
					"\n" + undefinedDataPropertiesSet.size() + " data properties are missing in the file " + fileName2);
			undefinedDataPropertiesWriter
					.println("--------------------------------------------------------------------");
			sortStringSet(undefinedDataPropertiesSet).forEach(undefinedDataPropertiesWriter::println);
			Set<String> undefinedDataPropertiesSet2 = compareSet(dataPropertiesSet2, dataPropertiesSet);
			undefinedDataPropertiesWriter.println("\n" + undefinedDataPropertiesSet2.size()
					+ " data properties are missing in the file " + fileName1);
			undefinedDataPropertiesWriter
					.println("--------------------------------------------------------------------");
			sortStringSet(undefinedDataPropertiesSet2).forEach(undefinedDataPropertiesWriter::println);
			undefinedDataPropertiesWriter.close();

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

			undefinedObjectRestrictionsWriter
					.println("\n------------------------OBJECT RESTRICTIONS---------------------------\n");
			undefinedObjectRestrictionsWriter
					.println(fileName1 + " has " + objectRestrictionSet1.size() + " restrictions.");
			undefinedObjectRestrictionsWriter
					.println(fileName2 + " has " + objectRestrictionSet2.size() + " restrictions.");

			Set<String> undefinedRestrictions = new HashSet<String>();
			undefinedRestrictions = compareSet(objectRestrictionSet1, objectRestrictionSet2);
			undefinedObjectRestrictionsWriter
					.println(undefinedRestrictions.size() + " restrictions are missing in the file " + fileName2);

			Set<String> undefinedRestrictions2 = new HashSet<String>();
			undefinedRestrictions2 = compareSet(objectRestrictionSet2, objectRestrictionSet1);
			undefinedObjectRestrictionsWriter
					.println(undefinedRestrictions2.size() + " restrictions are missing in the file " + fileName1);

			// get restriction differences
			Set<String> undefinedObjectRestrictions = new HashSet<String>();
			for (String s : undefinedRestrictions) {
				undefinedObjectRestrictions.add(s + " " + fileName1);
			}
			for (String s : undefinedRestrictions2) {
				undefinedObjectRestrictions.add(s + " " + fileName2);
			}
			undefinedObjectRestrictionsWriter
					.println("\n------------------- Object Restriction Differences -----------------\n");
			sortStringSet(undefinedObjectRestrictions).forEach(undefinedObjectRestrictionsWriter::println);
			undefinedObjectRestrictionsWriter.close();

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

			undefinedDataRestrictionsWriter
					.println("\n------------------------OBJECT RESTRICTIONS---------------------------\n");
			undefinedDataRestrictionsWriter
					.println(fileName1 + " has " + dataRestrictionSet1.size() + " restrictions.");
			undefinedDataRestrictionsWriter
					.println(fileName2 + " has " + dataRestrictionSet2.size() + " restrictions.");

			Set<String> undefinedDataRestrictions = new HashSet<String>();
			undefinedDataRestrictions = compareSet(dataRestrictionSet1, dataRestrictionSet2);
			undefinedDataRestrictionsWriter
					.println(undefinedDataRestrictions.size() + " restrictions are missing in the file " + fileName2);

			Set<String> undefinedDataRestrictions2 = new HashSet<String>();
			undefinedDataRestrictions2 = compareSet(dataRestrictionSet2, dataRestrictionSet1);
			undefinedDataRestrictionsWriter
					.println(undefinedDataRestrictions2.size() + " restrictions are missing in the file " + fileName1);

			// get restriction differences
			Set<String> dataRestrictionsDifferences = new HashSet<String>();
			for (String s : undefinedDataRestrictions) {
				dataRestrictionsDifferences.add(s + " " + fileName1);
			}
			for (String s : undefinedDataRestrictions2) {
				dataRestrictionsDifferences.add(s + " " + fileName2);
			}
			undefinedDataRestrictionsWriter
					.println("\n------------------- Data Restriction Differences -----------------\n");
			sortStringSet(dataRestrictionsDifferences).forEach(undefinedDataRestrictionsWriter::println);
			undefinedDataRestrictionsWriter.close();

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

			subClassOfDifferencesWriter.println("\n------------------------Sub Classes---------------------------\n");
			subClassOfDifferencesWriter.println(fileName1 + " has " + subClassOfFile1.size() + " sub classes.");
			subClassOfDifferencesWriter.println(fileName2 + " has " + subClassOfFile2.size() + " sub classes.");

			Set<String> undefinedSubClasses = new HashSet<String>();
			undefinedSubClasses = compareSet(subClassOfFile1, subClassOfFile2);
			subClassOfDifferencesWriter.println(undefinedSubClasses.size() + " sub classes are missing in the file " + fileName2);

			Set<String> undefinedSubClasses2 = new HashSet<String>();
			undefinedSubClasses2 = compareSet(subClassOfFile2, subClassOfFile1);
			subClassOfDifferencesWriter.println(undefinedSubClasses2.size() + " sub classes are missing in the file " + fileName1);

			// get restriction differences
			Set<String> subClassDifferences = new HashSet<String>();
			for (String s : undefinedSubClasses) {
				subClassDifferences.add(s + " " + fileName1);
			}
			for (String s : undefinedSubClasses2) {
				subClassDifferences.add(s + " " + fileName2);
			}
			subClassOfDifferencesWriter.println("\n------------------- SubClassOf Differences -----------------\n");
			sortStringSet(subClassDifferences).forEach(subClassOfDifferencesWriter::println);
			subClassOfDifferencesWriter.close();

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
			undefinedIndividualsDifferencesWriter.println("\n------------------------Individuals---------------------------\n");
			undefinedIndividualsDifferencesWriter.println(fileName1 + " has " + individualsFile1.size() + " individuals.");
			undefinedIndividualsDifferencesWriter.println(fileName2 + " has " + individualsFile2.size() + " individuals.\n");
			
			Set<String> undefinedIndividualsClasses = compareSet(individualsFile1, individualsFile2);
			undefinedIndividualsDifferencesWriter.println(undefinedIndividualsClasses.size() + " individuals are missing in the file " + fileName2);
			undefinedIndividualsDifferencesWriter.println("--------------------------------------------------------------------\n");
			sortStringSet(undefinedIndividualsClasses).forEach(undefinedIndividualsDifferencesWriter::println);

			
			Set<String> undefinedIndividualsClasses2 = compareSet(individualsFile2, individualsFile1);
			undefinedIndividualsDifferencesWriter.println("\n"+undefinedIndividualsClasses2.size() + " individuals are missing in the file " + fileName1);
			undefinedIndividualsDifferencesWriter.println("--------------------------------------------------------------------\n");
			sortStringSet(undefinedIndividualsClasses2).forEach(undefinedIndividualsDifferencesWriter::println);

			

			
			undefinedIndividualsDifferencesWriter.close();
			
			 try {
		            System.out.println("BPMN2OWL done");

		            Runtime.getRuntime().exec("taskkill /f /im cmd.exe") ;
		        } catch (Exception e) {
		            e.printStackTrace();
		            System.out.println("BPMN2OWL done");
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
