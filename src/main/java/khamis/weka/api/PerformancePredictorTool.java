package khamis.weka.api;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class PerformancePredictorTool extends JFrame {
	String file = null;
	String output;

	private JTextArea textArea;
	private JButton uploadFile = new JButton("Upload File");
	private JButton makePrediction = new JButton("Make Prediction");
	private JButton savePredictionResult = new JButton("Save Result");

	public PerformancePredictorTool() {
		super("Performance Predictor Tool");
		textArea = new JTextArea(50, 10);
		textArea.setEditable(false);
		textArea.setText("S/N \t ACTUAL \t PREDICTED \n");
		// creates the GUI
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.anchor = GridBagConstraints.WEST;

		add(uploadFile, constraints);

		constraints.gridx = 1;

		add(makePrediction, constraints);

		constraints.gridx = 2;

		add(savePredictionResult, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;

		add(new JScrollPane(textArea), constraints);

		// adds event handler for button Start
		uploadFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				uploadArff();
			}
		});

		// adds event handler for button Clear
		makePrediction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					predict();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(480, 320);
		setLocationRelativeTo(null); // centers on screen
		savePredictionResult.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					savePrediction();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private void uploadArff() {
		final JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("ARFF Files (*.arff)", "arff"));
		int returnVal = fc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile().getName();

		}
	}

	private void predict() throws Exception {

		// load classifier
		Classifier classifier = (Classifier) SerializationHelper.read("train.model");

		try {

			Instances unlabeled = new Instances(new BufferedReader(new FileReader(file)));
			unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

			// create copy
			Instances labeled = new Instances(unlabeled);

			int numberOfInstances = unlabeled.numInstances();

			// label instances
			for (int i = 0; i < numberOfInstances; i++) {
				double clsLabel = classifier.classifyInstance(unlabeled.instance(i));

				labeled.instance(i).setClassValue(clsLabel);

				String predicted = unlabeled.classAttribute().value((int) clsLabel);
				String actual = unlabeled.instance(i).toString(unlabeled.classIndex());

				output = (i + 1) + "\t" + actual + "\t" + predicted + "\n";

				textArea.append(output);
			}

		} catch (IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			System.out.println(e.getMessage());
		}
	}

	private void savePrediction() {
		FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("Text File", "txt");
		final JFileChooser savePrediction = new JFileChooser();
		savePrediction.setFileFilter(extensionFilter);
		int actionDialog = savePrediction.showSaveDialog(this);
		if (actionDialog != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = savePrediction.getSelectedFile();
		if (!file.getName().endsWith("txt")) {
			file = new File(file.getAbsolutePath() + ".txt");
		}

		BufferedWriter outFile = null;
		try {
			outFile = new BufferedWriter(new FileWriter(file));
			textArea.write(outFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (outFile != null) {
				try {
					outFile.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new PerformancePredictorTool().setVisible(true);
			}
		});
	}
}
