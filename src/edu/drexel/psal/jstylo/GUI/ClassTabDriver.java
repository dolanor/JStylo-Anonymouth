package edu.drexel.psal.jstylo.GUI;

import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.LogOut;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import weka.classifiers.*;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.*;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.*;
import weka.core.Option;

public class ClassTabDriver {

	/* =========================
	 * Classifiers tab listeners
	 * =========================
	 */
	
	protected static Classifier tmpClassifier;
	
	/**
	 * Initialize all documents tab listeners.
	 */
	protected static void initListeners(final GUIMain main) {
		
		// available classifiers tree
		// ==========================
		
		main.classJTree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				// if unselected
				if (main.classJTree.getSelectionCount() == 0) {
					Logger.logln("Classifier tree unselected in the classifiers tab.");
					resetAvClassSelection(main);
					return;
				}
				
				// unselect selected list
				main.classJList.clearSelection();
				
				Object[] path = main.classJTree.getSelectionPath().getPath();
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)path[path.length-1];
				
				// if selected a classifier
				if (selectedNode.isLeaf()) {
					Logger.logln("Classifier selected in the available classifiers tree in the classifiers tab: "+selectedNode.toString());
					
					// get classifier
					String className = getClassNameFromPath(path);
					tmpClassifier = null;
					try {
						//tmpClassifier = Classifier.forName(className, null);						//TODO
						tmpClassifier = (Classifier) Class.forName(className).newInstance();
					} catch (Exception e) {
						Logger.logln("Could not create classifier out of class: "+className);
						JOptionPane.showMessageDialog(main,
								"Could not generate classifier for selected class:\n"+className,
								"Classifier Selection Error",
								JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
						return;
					}
					
					// show options and description
					main.classAvClassArgsJTextField.setText(getOptionsStr(tmpClassifier.getOptions()));
					main.classDescJTextPane.setText(getDesc(tmpClassifier));
				}
				// otherwise
				else {
					resetAvClassSelection(main);
				}
			}
		});
		
		// add button
		// ==========
		
		main.classAddJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Add' button clicked in the analysis tab.");

				// check if classifier is selected
				if (tmpClassifier == null) {
					JOptionPane.showMessageDialog(main,
							"You must select a classifier to be added.",
							"Add Classifier Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				
				} else {
					// check classifier options
					try {
						tmpClassifier.setOptions(main.classAvClassArgsJTextField.getText().split(" "));
					} catch (Exception e) {
						Logger.logln("Invalid options given for classifier.",LogOut.STDERR);
						JOptionPane.showMessageDialog(main,
								"The classifier arguments entered are invalid.\n"+
										"Restoring original options.",
										"Classifier Options Error",
										JOptionPane.ERROR_MESSAGE);
						main.classAvClassArgsJTextField.setText(getOptionsStr(tmpClassifier.getOptions()));
						return;
					}
					
					// add classifier
					main.classifiers.add(tmpClassifier);
					GUIUpdateInterface.updateClassList(main);
					resetAvClassSelection(main);
					main.classJTree.clearSelection();
				}
			}
		});
		
		// selected classifiers list
		// =========================
		
		main.classJList.addListSelectionListener(new ListSelectionListener() {
			int lastSelected = -2;
			
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int selected = main.classJList.getSelectedIndex();
				if (selected == lastSelected)
					return;
				lastSelected = selected;
				
				// if unselected
				if (selected == -1) {
					Logger.logln("Classifier list unselected in the classifiers tab.");
					resetSelClassSelection(main);
					return;
				}

				// unselect available classifiers tree
				main.classJTree.clearSelection();

				String className = main.classJList.getSelectedValue().toString();
				Logger.logln("Classifier selected in the selected classifiers list in the classifiers tab: "+className);

				// show options and description
				main.classSelClassArgsJTextField.setText(getOptionsStr(main.classifiers.get(selected).getOptions()));
				main.classDescJTextPane.setText(getDesc(main.classifiers.get(selected)));
			}
		});
		
		// remove button
		// =============
		
		main.classRemoveJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.log("'Remove' button clicked in the classifiers tab.");
				int selected = main.classJList.getSelectedIndex();
				
				// check if selected
				if (selected == -1) {
					JOptionPane.showMessageDialog(main,
							"You must select a classifier to be removed.",
							"Remove Classifier Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// remove classifier
				main.classifiers.remove(selected);
				GUIUpdateInterface.updateClassList(main);
			}
		});
		
		// about button
		// ============

		main.classAboutJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				GUIUpdateInterface.showAbout(main);
			}
		});
		
		// back button
		// ===========
		
		main.classBackJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln("'Back' button clicked in the classifiers tab");
				main.mainJTabbedPane.setSelectedIndex(1);
			}
		});
		
		// next button
		// ===========
		
		main.classNextJButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln("'Next' button clicked in the classifiers tab");
				
				if (main.classifiers.isEmpty()) {
					JOptionPane.showMessageDialog(main,
							"You must add at least one classifier.",
							"Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					main.mainJTabbedPane.setSelectedIndex(3);
				}
			}
		});
	}
	
	/**
	 * Clears the GUI when no available classifier is selected.
	 */
	protected static void resetAvClassSelection(GUIMain main) {
		// clear everything
		tmpClassifier = null;
		main.classAvClassArgsJTextField.setText("");
		main.classDescJTextPane.setText("");
	}
	
	/**
	 * Clears the GUI when no selected classifier is selected.
	 */
	protected static void resetSelClassSelection(GUIMain main) {
		// clear everything
		main.classSelClassArgsJTextField.setText("");
		main.classDescJTextPane.setText("");
	}
	
	/**
	 * Creates a classifier options string.
	 */
	public static String getOptionsStr(String[] options) {
		String optionStr = "";
		for (String option: options)
			optionStr += option+" ";
		return optionStr;
	}
	
	
	/**
	 * Constructs the class name out of a tree path.
	 */
	protected static String getClassNameFromPath(Object[] path) {
		String res = "";
		for (Object o: path) {
			res += o.toString()+".";
		}
		res = res.substring(0,res.length()-1);
		return res;
	}
	
	/* ======================
	 * initialization methods
	 * ======================
	 */

	// build classifiers tree from list of class names
	protected static String[] classNames = new String[] {
		// bayes
		//"weka.classifiers.bayes.BayesNet",
		"weka.classifiers.bayes.NaiveBayes",
		"weka.classifiers.bayes.NaiveBayesMultinomial",
		//"weka.classifiers.bayes.NaiveBayesMultinomialUpdateable",
		//"weka.classifiers.bayes.NaiveBayesUpdateable",

		// functions
		"weka.classifiers.functions.Logistic",
		"weka.classifiers.functions.MultilayerPerceptron",
		"weka.classifiers.functions.SMO",
		"weka.classifiers.functions.LibSVM",

		// lazy
		"weka.classifiers.lazy.IBk",

		// meta


		// misc


		// rules
		"weka.classifiers.rules.ZeroR",

		// trees
		"weka.classifiers.trees.J48",
	};
	
	/**
	 * Initialize available classifiers tree
	 */
	@SuppressWarnings("unchecked")
	protected static void initWekaClassifiersTree(GUIMain main) {
		// create root and set to tree
		DefaultMutableTreeNode wekaNode = new DefaultMutableTreeNode("weka");
		DefaultMutableTreeNode classifiersNode = new DefaultMutableTreeNode("classifiers");
		wekaNode.add(classifiersNode);
		DefaultTreeModel model = new DefaultTreeModel(wekaNode);
		main.classJTree.setModel(model);
		
		// add all classes
		DefaultMutableTreeNode currNode, child;
		for (String className: classNames) {
			String[] nameArr = className.split("\\.");
			currNode = classifiersNode;
			for (int i=2; i<nameArr.length; i++) {
				// look for node
				Enumeration<DefaultMutableTreeNode> children = currNode.children();
				while (children.hasMoreElements()) {
					child = children.nextElement();
					if (child.getUserObject().toString().equals(nameArr[i])) {
						currNode = child;
						break;
					}
				}
				
				// if not found, create a new one
				if (!currNode.getUserObject().toString().equals(nameArr[i])) {
					child = new DefaultMutableTreeNode(nameArr[i]);
					currNode.add(child);
					currNode = child;
				}
			}
		}
		
		// expand tree
		int row = 0;
		while (row < main.classJTree.getRowCount())
			main.classJTree.expandRow(row++);
	}
	
	/**
	 * Initialize map of classifier class-name to its description.
	 */
	protected static String getDesc(Classifier c) {
		// bayes
		if (c instanceof NaiveBayes) {
			return ((NaiveBayes) c).globalInfo();
		} else if (c instanceof NaiveBayesMultinomial) {
			return ((NaiveBayesMultinomial) c).globalInfo();
		}
		
		// functions
		else if (c instanceof Logistic) {
			return ((Logistic) c).globalInfo();
		}
		else if (c instanceof MultilayerPerceptron) {
			return ((MultilayerPerceptron) c).globalInfo();
		}
		else if (c instanceof SMO) {
			return ((SMO) c).globalInfo();
		}
		else if (c instanceof LibSVM) {
			LibSVM s = (LibSVM) c;
			String res = s.globalInfo()+"\n\nOptions:\n";
			Enumeration e = s.listOptions();
			while (e.hasMoreElements()) {
				Option o = (Option) e.nextElement();
				res += "-"+o.name()+": "+o.description()+"\n\n";
			}
			return res;
		}
		
		// lazy
		else if (c instanceof IBk) {
			return ((IBk) c).globalInfo();
		}
		
		// meta

		// misc

		// rules
		else if (c instanceof ZeroR) {
			return ((ZeroR) c).globalInfo();
		}

		// trees
		else if (c instanceof J48) {
			return ((J48) c).globalInfo();
		}
		
		else {
			return "No description available.";
		}
	}
}































