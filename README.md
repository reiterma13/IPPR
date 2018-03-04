# IPPR
The intention of the integrated practise project is to find out if Business Process Model and Notation (BPMN) diagrams are conform to the BPMN 2.0 Specification. The outcome is are three java-based prototypes for the comparison of BPMN ontologies with the standard or other ontologies and a JAR file which describes an ontology. The scientific paper and the prototypes are intended for people, who want to check if their BPMN diagrams are conform to the BPMN 2.0 Specification. The folders BPMN2OWL, CompareOWLs and OWLDescriber contain runnable JAR files. The folder IPPRProjectFiles contains the source code.

-----------------------------------------------------------------------------------------------------
Installation and Configuration Steps
-----------------------------------------------------------------------------------------------------

JDK
- http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
- jdk-8u151-windows-x64.exe
- Set the system variables: JAVA_HOME und Path 

Eclipse
- https://www.eclipse.org/downloads/download.php?file=/oomph/epp/oxygen/R/eclipse-inst-win64.exe
- Eclipse IDE for Java Developers

BPMN2-Modeler
- Eclipse Marketplace
- Eclipse BPMN2 Modeler 1.4.1

OWL API
- https://github.com/owlcs/owlapi
- Import existing maven project

OWL
- Create Java Project
- Configure Build Path -> Projects -> Add all owlapi projects
- Run Configuration -> Classpath -> JRE System Library [JavaSE-1.8] und OWL
- Download Folder 4 from https://github.com/owlcs/releases/tree/master/owlapi
- and add external JAR Files from releases-master version 4

JDOM
- http://www.jdom.org/downloads/
- Add external JAR Files

Protege
- https://protege.stanford.edu/products.php#desktop-protege
- Program to view ontologies.

Graphviz
- https://graphviz.gitlab.io/_pages/Download/Download_windows.html
- OWL Viz uses Ghraphviz to display classes.

OWL VIZ
- Is a Protege Plugin and depends on Graphviz.
- Protege: File -> Preferences -> OWL Viz -> Path to DOT ..\graphviz-2.38\release\bin\dot.exe

-----------------------------------------------------------------------------------------------------
IPPRProjectFiles -> Project structure
-----------------------------------------------------------------------------------------------------

default
- BPMN2OWL -> Transforms BPMN_INPUT.xml to OWL_OUTPUT.owl 
- IPPRvsBueps -> Compares IPPR.owl and BUEPA.owl
- IPPRvsTeilOntology -> Compares IPPR.owl and OWL_OUTPUT.owl
- OWL2BPMN -> Transforms OLW_OUTPUT.owl to BPMN_Generated.xml
- OWLDescriber -> Takes input owl an describes it.

bpmn
- BPMN_Generated -> Generated from OWL_OUTPUT.owl
- BPMN_INPUT -> Contains the xml source code of the diagram which should be converted to owl

diagrams
- Contains test diagrams from BPMN2-Modeler, Signavio and www.demo.bpmn.io

documentation
- Installation and Configuration Steps:
  Contains all installation and configuration steps
- Project structure: 
  Contains explanation of packages, files and cohesion 
  
generatedOwlLogs
- Shows differences of IPPR.owl and OWL_OUTPUT

logs
- Shows differences of IPPR.owl and BUEPA.owl

owl
- IPPR.owl
- BUEPA.owl
- OWL_OUTPUT.owl -> Generated from BPMN_INPUT.xml

-----------------------------------------------------------------------------------------------------
Developer Information
-----------------------------------------------------------------------------------------------------

If you make source code changes, you can export a runable JAR file, however you need to change the main class in the manifest.

