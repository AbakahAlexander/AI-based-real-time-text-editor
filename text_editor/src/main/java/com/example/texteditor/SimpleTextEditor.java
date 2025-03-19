package com.example.texteditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.text.*;

import com.l2fprod.common.swing.JFontChooser;

public class SimpleTextEditor {
    private static SpeechRecognitionUtil speechRecognition;
    private static JButton speechButton;
    private static boolean isRecording = false;
    private static JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simple Text Editor");
            
            // Use JTextPane directly (no pagination)
            JTextPane textpane = new JTextPane();
            JScrollPane scrollPane = new JScrollPane(textpane);

            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);

            // Add formatting buttons
            JButton bolButton = new JButton("B");
            JButton italButton = new JButton("I");
            JButton underButton = new JButton("U");
            JButton strikeButton = new JButton("S");
            JButton leftButton = new JButton("L");
            JButton centerButton = new JButton("C");
            JButton rightButton = new JButton("R");
            JButton justifyButton = new JButton("J");
            JButton fontButton = new JButton("Font");
            JButton colorButton = new JButton("Color");

            toolBar.add(bolButton);
            toolBar.add(italButton);
            toolBar.add(underButton);
            toolBar.add(strikeButton);
            toolBar.addSeparator();
            toolBar.add(leftButton);
            toolBar.add(centerButton);
            toolBar.add(rightButton);
            toolBar.add(justifyButton);
            toolBar.addSeparator();
            toolBar.add(fontButton);
            toolBar.add(colorButton);

            // Button action listeners
            bolButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Bold", null);
                StyleConstants.setBold(style, true);
                doc.setCharacterAttributes(start, end - start, style, false);
            });

            italButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Italic", null);
                StyleConstants.setItalic(style, true);
                doc.setCharacterAttributes(start, end - start, style, false);
            });

            underButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Underline", null);
                StyleConstants.setUnderline(style, true);
                doc.setCharacterAttributes(start, end - start, style, false);
            });

            strikeButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Strike", null);
                StyleConstants.setStrikeThrough(style, true);
                doc.setCharacterAttributes(start, end - start, style, false);
            });

            leftButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Left", null);
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
                doc.setParagraphAttributes(start, end - start, style, false);
            });

            centerButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Center", null);
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
                doc.setParagraphAttributes(start, end - start, style, false);
            });

            rightButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Right", null);
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
                doc.setParagraphAttributes(start, end - start, style, false);
            });

            justifyButton.addActionListener(e -> {
                StyledDocument doc = textpane.getStyledDocument();
                int start = textpane.getSelectionStart();
                int end = textpane.getSelectionEnd();
                Style style = textpane.addStyle("Justify", null);
                StyleConstants.setAlignment(style, StyleConstants.ALIGN_JUSTIFIED);
                doc.setParagraphAttributes(start, end - start, style, false);
            });

            colorButton.addActionListener(e -> {
                Color color = JColorChooser.showDialog(frame, "Choose Text Color", Color.BLACK);
                if (color != null) {
                    StyledDocument doc = textpane.getStyledDocument();
                    int start = textpane.getSelectionStart();
                    int end = textpane.getSelectionEnd();
                    if (start != end) {  // Only apply if there is selected text
                        Style style = textpane.addStyle("SelectedColor", null);
                        StyleConstants.setForeground(style, color);
                        doc.setCharacterAttributes(start, end - start, style, false);
                    }
                }
            });

            fontButton.addActionListener(e -> {
                JFontChooser fontChooser = new JFontChooser();
                Font selectedFont = JFontChooser.showDialog(frame, "Choose Font", textpane.getFont());
            
                if (selectedFont != null) {  // User pressed OK
                    StyledDocument doc = textpane.getStyledDocument();
                    int start = textpane.getSelectionStart();
                    int end = textpane.getSelectionEnd();
                    if (start != end) {  // Only apply if there is selected text
                        Style style = textpane.addStyle("SelectedFont", null);
                        StyleConstants.setFontFamily(style, selectedFont.getFamily());
                        StyleConstants.setFontSize(style, selectedFont.getSize());
                        doc.setCharacterAttributes(start, end - start, style, false);
                    }
                }
            });
            
            // Resize listener for the scrollpane
            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    int width = frame.getContentPane().getWidth();
                    scrollPane.setPreferredSize(new Dimension(width, frame.getHeight()));
                    scrollPane.revalidate();
                }
            });
            
            // Status label and speech recognition
            statusLabel = new JLabel("Ready");
            statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            speechRecognition = new SpeechRecognitionUtil(textpane, statusLabel);
            
            // Menu setup
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem openItem = new JMenuItem("Open");
            JMenuItem saveItem = new JMenuItem("Save");
            JMenuItem exitItem = new JMenuItem("Exit");
            
            fileMenu.add(openItem);
            fileMenu.add(saveItem);
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);

            // Speech button
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
            
            toolBar.addSeparator();
            toolBar.add(speechButton);
            
            // Add components to frame
            frame.setJMenuBar(menuBar);
            frame.add(toolBar, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(statusLabel, BorderLayout.SOUTH);
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Window close handler
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (isRecording) {
                        speechRecognition.stopRecording();
                    }
                }
            });
            
            frame.setVisible(true);

            // File operations
            openItem.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showOpenDialog(frame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                        textpane.read(reader, null);
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
                        textpane.write(writer);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error saving file.");
                    }
                }
            });

            exitItem.addActionListener(e -> frame.dispose());
        });
    }
}
