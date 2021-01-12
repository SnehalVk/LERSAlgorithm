package com.kdd.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings("serial")
public class GUI extends JFrame{
	private JFrame frames;
	private JLabel header, dataFileLabel, attribFileLabel, initialDecisionLabel, finalDecisionLabel, decisionAttribBoxLabel;
	private JLabel stableAttribLabel, minConfLabel, minSupLabel;
	private JTextField dataFilePath, attribFilePath, minConf, minSup;
	private JButton dataFileChooser, attribFileChooser, loadDataButton, generateButton;
	private JComboBox<String> initialDecision, finalDecision, decisionAttribBox;
	private JScrollPane stableAttrib;
	private JList<String> stableAttribJList;

	File dataFile, attribFile;
	private GenerateActionRules data;

	public GUI() {
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		frames = new JFrame();
		frames.setBounds(250, 250, 700, 420);
		frames.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frames.getContentPane().setLayout(null);
		frames.setTitle("Action Rules Extraction Utility");
		frames.setResizable(false);

		header = new JLabel();
		attribFileLabel = new JLabel();
		dataFileLabel = new JLabel();
		initialDecisionLabel = new JLabel();
		finalDecisionLabel = new JLabel();
		decisionAttribBoxLabel = new JLabel();
		stableAttribLabel = new JLabel();
		minConfLabel = new JLabel();
		minSupLabel = new JLabel();
		dataFilePath = new JTextField();
		minConf= new JTextField();
		minSup= new JTextField();
		attribFilePath = new JTextField();
		initialDecision = new JComboBox<>();
		finalDecision = new JComboBox<>();
		decisionAttribBox = new JComboBox<>();
		stableAttrib = new JScrollPane();
		stableAttribJList = new JList<>();



		dataFileChooser = new JButton("Select"); 
		attribFileChooser = new JButton("Select");
		loadDataButton = new JButton("Load");

		header.setText("Action Rules Extraction Utility");
		header.setFont(new Font(Font.SERIF,  Font.BOLD, 18));
		header.setBounds(200, 2, 250, 40);


		dataFileLabel.setText("Data File:");
		dataFileLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		dataFileLabel.setBounds(50, 50, 100, 25);
		dataFilePath.setEditable(false);
		dataFilePath.setBounds(120, 50, 400, 25);
		
		dataFileChooser.setBounds(540, 50, 100, 25);

		dataFileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser selectFile = new JFileChooser();
                int returnVal = selectFile.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    dataFile =  selectFile.getSelectedFile();
                    dataFilePath.setText(dataFile.getPath());
                }else{
                    System.out.println("Data File Read error!!!!");
                    JOptionPane.showMessageDialog(null, "Data File Read error!!!!",
                            "OK", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
		

		attribFileLabel.setText("Attrib File:");
		attribFileLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		attribFileLabel.setBounds(50, 90, 100, 25);
		attribFilePath.setEditable(false);
		attribFilePath.setBounds(120, 90, 400, 25);
		
		attribFileChooser.setBounds(540, 90, 100, 25);
		attribFileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser selectedFile = new JFileChooser();
                int returnVal = selectedFile.showOpenDialog(null);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    attribFile =selectedFile.getSelectedFile();
                    attribFilePath.setText(attribFile.getPath());
                }else{
                    System.out.println("File Read error!!!!");
                    JOptionPane.showMessageDialog(null, "Attrib File Read error!!!!",
                            "OK", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        
        
		loadDataButton.setBounds(280,120,90,25);
		loadDataButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent arg0) {

	            	data= new GenerateActionRules();
					data.readFile(attribFile, dataFile);
	                List<String> attributeNames = data.attributeNames;
	                decisionAttribBox.removeAllItems();
	                decisionAttribBox.addItem("");
	                for(String name : attributeNames) {
	                	decisionAttribBox.addItem(name);
	                }

					stableAttribJList.setListData(data.attributeNames.toArray(new String[data.attributeNames.size()]));

	            }
	        });

		frames.getContentPane().add(header);
		frames.getContentPane().add(dataFileLabel);
		frames.getContentPane().add(dataFilePath);
		frames.getContentPane().add(dataFileChooser);
		frames.getContentPane().add(attribFileLabel);
		frames.getContentPane().add(attribFilePath);
		frames.getContentPane().add(attribFileChooser);
		frames.getContentPane().add(loadDataButton);



		decisionAttribBoxLabel.setText("Decision Attrib:");
		decisionAttribBoxLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		decisionAttribBoxLabel.setBounds(20, 160, 100, 25);
		decisionAttribBox.setBounds(120, 160, 100, 25);
		decisionAttribBox.setEnabled(true);
		decisionAttribBox.setEditable(false);
		decisionAttribBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                if(arg0.getStateChange() == ItemEvent.SELECTED) {
                    initialDecision.removeAllItems();
                    finalDecision.removeAllItems();

					HashSet<String> distinctValues = data.getUniqueValues((String)arg0.getItem());

					if(distinctValues==null || distinctValues.size()==0){

					}else{
						for(String value : distinctValues) {
							initialDecision.addItem(value);
							finalDecision.addItem(value);
						}
						initialDecision.setEnabled(true);
						finalDecision.setEnabled(true);
					}
				}else {
					initialDecision.setEnabled(false);
					finalDecision.setEnabled(false);
				}

            }
        });

		initialDecisionLabel.setText("Initial Attrib:");
		initialDecisionLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		initialDecisionLabel.setBounds(250, 160, 100, 25);
		initialDecision.setBounds(330, 160, 100, 25);
		initialDecision.setEnabled(true);
		initialDecision.setEditable(false);

		finalDecisionLabel.setText("Final Attrib:");
		finalDecisionLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		finalDecisionLabel.setBounds(490, 160, 100, 25);
		finalDecision.setBounds(560, 160, 100, 25);
		finalDecision.setEnabled(true);
		finalDecision.setEditable(false);

		frames.getContentPane().add(decisionAttribBoxLabel);
		frames.getContentPane().add(decisionAttribBox);
		frames.getContentPane().add(initialDecisionLabel);
		frames.getContentPane().add(initialDecision);
		frames.getContentPane().add(finalDecisionLabel);
		frames.getContentPane().add(finalDecision);

		stableAttribLabel.setText("Stable Attribute:");
		stableAttribLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		stableAttribLabel.setBounds(20, 190, 100, 25);
		stableAttrib.setBounds(20, 220, 100, 150);
		stableAttrib.setEnabled(true);
		stableAttrib.setViewportView(stableAttribJList);
		
		minConfLabel.setText("Min Conf:");
		minConfLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		minConfLabel.setBounds(250, 230, 100, 25);
		minConf.setBounds(330, 230, 100, 25);
		minConf.setEnabled(true);

		minSupLabel.setText("Min Sup:");
		minSupLabel.setFont(new Font(Font.SERIF,  Font.PLAIN, 14));
		minSupLabel.setBounds(490, 230, 100, 25);
		minSup.setBounds(560, 230, 100, 25);
		minSup.setEnabled(true);

		frames.getContentPane().add(stableAttribLabel);
		frames.getContentPane().add(stableAttrib);
		frames.getContentPane().add(minConfLabel);
		frames.getContentPane().add(minConf);
		frames.getContentPane().add(minSupLabel);
		frames.getContentPane().add(minSup);

		generateButton = new JButton("Generate");
		generateButton.setBounds(560, 290, 100, 40);
		generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
				boolean isValid = true;

				HashSet<String> stable = new HashSet<>();
				stable.addAll(stableAttribJList.getSelectedValuesList());
				data.setFlexibleAttributes(stable);

				if (stable.contains(decisionAttribBox.getSelectedItem())) {
					JOptionPane.showMessageDialog(null, "Decision attribute cannot be stable.",
							"Decision attribute error", JOptionPane.ERROR_MESSAGE);
					isValid = false;
				}

				try {
					if (Integer.parseInt(minSup.getText()) <= 0 ||
							Integer.parseInt(minConf.getText()) < 0) {
						isValid = false;
						JOptionPane.showMessageDialog(null, "Support and confidence values must be greater than 0",
								"Value error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (NullPointerException err) {
					isValid = false;
					JOptionPane.showMessageDialog(null, "Must enter support and confidence values",
							"Value missing", JOptionPane.ERROR_MESSAGE);
				} catch (NumberFormatException err) {
					isValid = false;
					JOptionPane.showMessageDialog(null, "Support and confidence values must be integers",
							"Value error", JOptionPane.ERROR_MESSAGE);
				}

				if (isValid) {
					data.setSupportAndConfidence(Integer.parseInt(minSup.getText()),
							Integer.parseInt(minConf.getText()));

					String decisionName = (String) decisionAttribBox.getSelectedItem();

					data.setDecisionAttributes(decisionName + ((String) initialDecision.getSelectedItem()),
							decisionName + (String) finalDecision.getSelectedItem());
					data.generateActionRules();

					JOptionPane.showMessageDialog(null, "Action Rules and Certain Rules are generated, please open output.txt to view both",
							"Execution Result", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		frames.getContentPane().add(generateButton);
		frames.setVisible(true);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GUI instance = new GUI();
	}

}
