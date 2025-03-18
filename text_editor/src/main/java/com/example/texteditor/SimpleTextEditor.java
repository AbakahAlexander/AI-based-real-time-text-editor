package com.example.texteditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SimpleTextEditor {
    private static SpeechRecognitionUtil speechRecognition;
    private static JButton speechButton;
    private static boolean isRecording = false;
    private static JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simple Text Editor");
            JTextArea textArea = new JTextArea();
            JScrollPane scrollPane = new JScrollPane(textArea);
            
            statusLabel = new JLabel("Ready");
            statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            speechRecognition = new SpeechRecognitionUtil(textArea, statusLabel);

            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem openItem = new JMenuItem("Open");
            JMenuItem saveItem = new JMenuItem("Save");
            JMenuItem exitItem = new JMenuItem("Exit");

            fileMenu.add(openItem);
            fileMenu.add(saveItem);
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);

            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            
            speechButton = new JButton("Start Speech Recognition");
            speechButton.addActionListener(e -> {
                if (!isRecording) {
                    speechButton.setText("Stop Speech Recognition");
                    isRecording = true;
                    speechRecognition.startRecording();
                } else {
                    speechButton.setText("Start Speech Recognition");
                    isRecording = false;
                    speechRecognition.stopRecording();
                }
            });
            
            toolBar.add(speechButton);
            
            frame.setJMenuBar(menuBar);
            frame.add(toolBar, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(statusLabel, BorderLayout.SOUTH);
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (isRecording) {
                        speechRecognition.stopRecording();
                    }
                }
            });
            
            frame.setVisible(true);

            openItem.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(frame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                        textArea.read(reader, null);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error opening file.");
                    }
                }
            });

            saveItem.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showSaveDialog(frame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                        textArea.write(writer);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error saving file.");
                    }
                }
            });

            exitItem.addActionListener(e -> frame.dispose());
        });
    }
}
