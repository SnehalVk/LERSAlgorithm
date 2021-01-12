The ActionRules Project is developed in Java Programming language.
There are 3 main .java files:
1) GenerateActionRules.java - This file conatins the logic to generate rules from LERS - (Learning from Examples based on Rough Sets) algorithm. 
This is implemented using Map, HashMap, List, HashSet and used Files concept to read data and attributes and write the output into text files.


2) Action_Rules.java  - This file contains the logic for Graphical User Interface implemented using Java swing    
               concept and by creating object references using JFrame class. The GUI contains the interface to select  the appropriate data file, attribute file and allows user to enter the 
               min and max support values and button to execute the logic of LERSAlgorithm to generate the Action rules.  


3) LERSAlgorithm.java - This file contains logic for LERS Algorithm. Each iteration in this logic finds out certain rules and then from the set of possible rules combines the attributes and again make       them marked if possible.


Steps to run the Project :

1) Import the ActionRules project into Eclipse.
2) Run the GUI.java file which contains main method to launch the User Interface.
3) Click the Select button to pass the data file and Attribute file path respectively.
4) Click on Load button load the Attributes passed in attribute file.
5) Pass the minimum confidence and minimum support values in the textboxes provided and click on Generate button to generate the Action rules in Output.txt file.



  
