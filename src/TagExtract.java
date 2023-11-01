import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class TagExtract extends JFrame {

    ArrayList<String> stopWords = new ArrayList<>();
    TreeMap<String, Integer> map = new TreeMap<>();
    String[] words;
    boolean halt;

    JPanel mainPanel;
    JPanel pickerPanel;
    JPanel textAreaPanel;
    JPanel controlPanel;

    JButton textFileButton;
    JButton stopFileButton;
    JButton startButton;
    JButton quitButton;
    JButton clearButton;
    JButton saveButton;

    JTextArea textArea;

    JScrollPane scrollPane;

    JFileChooser textFileChooser;
    File textSelectedFile;

    JFileChooser stopFileChooser;
    File stopSelectedFile;

    JFileChooser saveTextFile;
    File workingDirectory = new File(System.getProperty("user.dir"));
    TagExtract() {
        setTitle("Tag Extractor");
        mainPanel = new JPanel();
        setLayout(new BorderLayout());

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        setSize(3*(screenWidth / 4), 3*(screenHeight / 4));
        setLocationRelativeTo(null);

        setResizable(false);
        createPickerPanel();
        mainPanel.add(pickerPanel, BorderLayout.WEST);

        createTextAreaPanel();
        mainPanel.add(textAreaPanel, BorderLayout.CENTER);

        createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.EAST);

        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void createPickerPanel() {
        pickerPanel = new JPanel();
        pickerPanel.setLayout(new GridLayout(3, 1));
        textFileButton = new JButton();
        textFileButton.setText("Select Text File");
        textFileButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        textFileButton.addActionListener((ActionEvent ae) -> {
            textFileChooser = new JFileChooser();
            textFileChooser.setCurrentDirectory(workingDirectory);
            textFileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
            int result = textFileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                textSelectedFile = textFileChooser.getSelectedFile();
                textFileButton.setText(textSelectedFile.getName());
            }

            else if (result == JFileChooser.CANCEL_OPTION) {
                textSelectedFile = null;
            }});

        stopFileButton = new JButton();
        stopFileButton.setText("Select Stop File");
        stopFileButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        stopFileButton.addActionListener((ActionEvent ae) -> {
            stopFileChooser = new JFileChooser();
            stopFileChooser.setCurrentDirectory(workingDirectory);
            stopFileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
            int result = stopFileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                stopSelectedFile = stopFileChooser.getSelectedFile();
                stopFileButton.setText(stopSelectedFile.getName());
            }

            else if (result == JFileChooser.CANCEL_OPTION) {
                stopSelectedFile = null;
            }});

        startButton = new JButton();
        startButton.setText("Start");
        startButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        startButton.addActionListener((ActionEvent ae) -> {
            if (stopSelectedFile == null&& textSelectedFile == null) {
                JOptionPane.showMessageDialog(null, "Choose Text & Noise File Before Starting Tag Extractor", "ERROR!", JOptionPane.ERROR_MESSAGE);
            } else if (stopSelectedFile == null) {
                JOptionPane.showMessageDialog(null, "Choose Noise File Before Starting Tag Extractor", "ERROR!", JOptionPane.ERROR_MESSAGE);
            } else if (textSelectedFile == null) {
                JOptionPane.showMessageDialog(null, "Choose Text File Before Starting Tag Extractor", "ERROR!", JOptionPane.ERROR_MESSAGE);
            } else {
                textArea.append("=========================================================\n       File Name: " + textSelectedFile.getName() + "\n=========================================================\n");
                textArea.append("\n");
                validWords();
                display();
            }});

        pickerPanel.add(textFileButton);
        pickerPanel.add(stopFileButton);
        pickerPanel.add(startButton);
    }

    private void createTextAreaPanel() {
        textAreaPanel = new JPanel();
        textArea = new JTextArea(20, 55);
        textArea.setFont(new Font("Monospaced", Font.BOLD, 18));
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        textAreaPanel.add(scrollPane);
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 1));
        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        clearButton.addActionListener((ActionEvent ae) -> {
            textArea.setText("");
            textSelectedFile = null;
            textFileButton.setText("Select Text File");
            stopSelectedFile = null;
            stopFileButton.setText("Select Tag File");
        });

        quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        quitButton.addActionListener((ActionEvent ae) -> System.exit(0));

        saveButton = new JButton("Save");
        saveButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        saveButton.addActionListener((ActionEvent ae) -> {
            if (textArea.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "File is Null. Start Tag Extractor", "ERROR!", JOptionPane.ERROR_MESSAGE);
            } else {
                saveTextFile = new JFileChooser();
                saveTextFile.setCurrentDirectory(workingDirectory);
                saveTextFile.setFileFilter(new FileNameExtensionFilter("Text Files", "txt", "text"));
                int result = saveTextFile.showSaveDialog(null);
                File file = saveTextFile.getSelectedFile();
                BufferedWriter writer;
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        writer = new BufferedWriter(new FileWriter(file));
                        writer.write(textArea.getText());
                        writer.close();
                        JOptionPane.showMessageDialog(null, "The File Was Saved Successfully!", "Success!", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "The File Count Not Be Saved!", "ERROR!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }});

        controlPanel.add(saveButton);
        controlPanel.add(clearButton);
        controlPanel.add(quitButton);

    }

    private void validWords() {
        Scanner stopWordSrc = null;
        try {
            stopWordSrc = new Scanner(stopSelectedFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        while (true) {
            assert stopWordSrc != null;
            if (!stopWordSrc.hasNextLine()) break;
            String line = stopWordSrc.nextLine();
            stopWords.add(line.toLowerCase());
        }

        Scanner textFileSrc = null;
        try {
            textFileSrc = new Scanner(textSelectedFile);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        while (true) {
            assert textFileSrc!= null;
            if (!textFileSrc.hasNextLine()) break;
            String line = textFileSrc.nextLine().toLowerCase();
            words = line.split("[^a-zA-Z]+");
            for (String word : words) {
                halt = false;
                for (String stop : stopWords) {
                    if (word.equals(stop)) {
                        halt = true;
                        break;
                    }
                }

                if (!halt) {
                    if (!map.containsKey(word)) {
                        map.put(word, 1);
                    }

                    else {
                        map.put(word, map.get(word) + 1);
                    }
                }
            }
        }
    }

    private void display() {
        for (String key : map.keySet()) {
            if (key.length() > 2) {
                textArea.append(" Word \"" + key + "\"    detected " + map.get(key) + " times\n");
            }
        }
    }




}
