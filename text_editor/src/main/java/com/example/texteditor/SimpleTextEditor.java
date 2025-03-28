package com.example.texteditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.text.Highlighter.*;
import javax.swing.border.*;

public class SimpleTextEditor {
    private static SpeechRecognitionUtil speechRecognition;
    private static JButton speechButton;
    private static boolean isRecording = false;
    private static JLabel statusLabel;
    
   
    private static final Highlighter.HighlightPainter highlightPainter = 
            new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Simple Text Editor");
            
        
            PagedEditorPane textArea = new PagedEditorPane();
            
     
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            
     
            textArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Highlighter highlighter = textArea.getHighlighter();
                    highlighter.removeAllHighlights();
                }
            });
            
    
            final UndoManager undoManager = new UndoManager();
            Document docForUndo = textArea.getDocument();
            docForUndo.addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
            
            statusLabel = new JLabel("Ready");
            statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            speechRecognition = new SpeechRecognitionUtil(textArea, statusLabel);

            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem openItem = new JMenuItem("Open");
            JMenuItem saveItem = new JMenuItem("Save");
            JMenuItem savePdfItem = new JMenuItem("Save as PDF"); 
            JMenuItem exitItem = new JMenuItem("Exit");

            fileMenu.add(openItem);
            fileMenu.add(saveItem);
            fileMenu.add(savePdfItem); 
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);

           
            frame.setJMenuBar(menuBar);

            JToolBar toolBar = new JToolBar();
            
            JButton bolButton = new JButton("Bold");
            JButton italButton = new JButton("Italic");
            JButton underButton = new JButton("Underline");
            JButton strikeButton = new JButton("Strikethrough");
            JButton colorButton = new JButton("Color");
            JButton fontButton = new JButton("Font");
            JButton sizeButton = new JButton("Size");
            JButton alignButton = new JButton("Align");
            JButton indentButton = new JButton("Indent");
            JButton outdentButton = new JButton("Outdent");
            JButton undoButton = new JButton("Undo");
            JButton redoButton = new JButton("Redo");
            JButton cutButton = new JButton("Cut");
            JButton copyButton = new JButton("Copy");
            JButton pasteButton = new JButton("Paste");
            JButton findButton = new JButton("Find");
            JButton replaceButton = new JButton("Replace");
            
            toolBar.add(bolButton);
            toolBar.add(italButton);
            toolBar.add(underButton);
            toolBar.add(strikeButton);
            toolBar.add(colorButton);
            toolBar.add(fontButton);
            toolBar.add(sizeButton);
            toolBar.add(alignButton);
            toolBar.add(indentButton);
            toolBar.add(outdentButton);
            toolBar.add(undoButton);
            toolBar.add(redoButton);
            toolBar.add(cutButton);
            toolBar.add(copyButton);
            toolBar.add(pasteButton);
            toolBar.add(findButton);
            toolBar.add(replaceButton);

            bolButton.addActionListener(e -> {
                StyledDocument docStyle = textArea.getStyledDocument();
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                if (start == end) {
                    return;  
                }

                javax.swing.text.Style style = textArea.addStyle("Bold", null);
                StyleConstants.setBold(style, true);
                docStyle.setCharacterAttributes(start, end - start, style, false);
            });

            italButton.addActionListener(e -> {
                StyledDocument docStyle = textArea.getStyledDocument();
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                if (start == end) {
                    return;  
                }

                javax.swing.text.Style style = textArea.addStyle("Italic", null);
                StyleConstants.setItalic(style, true);
                docStyle.setCharacterAttributes(start, end - start, style, false);
            });

            underButton.addActionListener(e -> {
                StyledDocument docStyle = textArea.getStyledDocument();
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                if (start == end) {
                    return;  
                }

                javax.swing.text.Style style = textArea.addStyle("Underline", null);
                StyleConstants.setUnderline(style, true);
                docStyle.setCharacterAttributes(start, end - start, style, false);
            });

            strikeButton.addActionListener(e -> {
                StyledDocument docStyle = textArea.getStyledDocument();
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                if (start == end) {
                    return;  
                }

                javax.swing.text.Style style = textArea.addStyle("Strikethrough", null);
                StyleConstants.setStrikeThrough(style, true);
                docStyle.setCharacterAttributes(start, end - start, style, false);
            });

       
            colorButton.addActionListener(e -> {
                Color color = JColorChooser.showDialog(frame, "Choose a color", Color.BLACK);
                if (color != null) {
                    StyledDocument docStyle = textArea.getStyledDocument();
                    int start = textArea.getSelectionStart();
                    int end = textArea.getSelectionEnd();

                    if (start == end) {
                        return;  
                    }

                    javax.swing.text.Style style = textArea.addStyle("Color", null);
                    StyleConstants.setForeground(style, color);
                    docStyle.setCharacterAttributes(start, end - start, style, false);
                }
            });

            fontButton.addActionListener(e -> {
          
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                String[] fontNames = ge.getAvailableFontFamilyNames();
                
              
                JDialog fontDialog = new JDialog(frame, "Select Font", true);
                fontDialog.setLayout(new BorderLayout());
                
              
                JList<String> fontList = new JList<>(fontNames);
                fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                JScrollPane listScroller = new JScrollPane(fontList);
                
                JLabel previewArea = new JLabel("AaBbCcDdEe");
                previewArea.setFont(new Font(fontNames[0], Font.PLAIN, 18));
                previewArea.setBackground(fontDialog.getBackground());
                previewArea.setOpaque(true);
                previewArea.setHorizontalAlignment(JLabel.CENTER);
                previewArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
             
                fontList.addListSelectionListener(event -> {
                    if (!event.getValueIsAdjusting()) {
                        String selectedFont = fontList.getSelectedValue();
                        if (selectedFont != null) {
                            previewArea.setFont(new Font(selectedFont, Font.PLAIN, 18));
                        }
                    }
                });
                
        
                JPanel buttonPanel = new JPanel();
                JButton okButton = new JButton("OK");
                JButton cancelButton = new JButton("Cancel");
                
                okButton.addActionListener(ev -> {
                    String selectedFont = fontList.getSelectedValue();
                    if (selectedFont != null) {
                        StyledDocument docStyle = textArea.getStyledDocument();
                        int start = textArea.getSelectionStart();
                        int end = textArea.getSelectionEnd();
                        
                        if (start != end) {
                            javax.swing.text.Style style = textArea.addStyle("Font", null);
                            StyleConstants.setFontFamily(style, selectedFont);
                            docStyle.setCharacterAttributes(start, end - start, style, false);
                        }
                    }
                    fontDialog.dispose();
                });
                
                cancelButton.addActionListener(ev -> fontDialog.dispose());
                
                buttonPanel.add(okButton);
                buttonPanel.add(cancelButton);
                
             
                fontDialog.add(listScroller, BorderLayout.CENTER);
                fontDialog.add(previewArea, BorderLayout.NORTH);
                fontDialog.add(buttonPanel, BorderLayout.SOUTH);
                
                fontDialog.setSize(300, 400);
                fontDialog.setLocationRelativeTo(frame);
                fontDialog.setVisible(true);
            });

         
            sizeButton.addActionListener(e -> {
                String sizeStr = JOptionPane.showInputDialog(frame, "Enter font size:");
                if (sizeStr != null) {
                    try {
                        int size = Integer.parseInt(sizeStr);
                        StyledDocument docStyle = textArea.getStyledDocument();
                        int start = textArea.getSelectionStart();
                        int end = textArea.getSelectionEnd();

                        if (start == end) {
                            return;  
                        }

                        javax.swing.text.Style style = textArea.addStyle("Size", null);
                        StyleConstants.setFontSize(style, size);
                        docStyle.setCharacterAttributes(start, end - start, style, false);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Please enter a valid number for font size.");
                    }
                }
            });

          
            alignButton.addActionListener(e -> {
                String[] options = {"Left", "Center", "Right", "Justify"};
                String align = (String) JOptionPane.showInputDialog(frame, "Choose alignment:", "Alignment", 
                                    JOptionPane.PLAIN_MESSAGE, null, options, "Left");
                if (align != null) {
                    StyledDocument docStyle = textArea.getStyledDocument();
                    int start = textArea.getSelectionStart();
                    int end = textArea.getSelectionEnd();

                    if (start == end) {
                        return;  
                    }

                    javax.swing.text.Style style = textArea.addStyle("Alignment", null);
                    switch (align) {
                        case "Left":
                            StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
                            break;
                        case "Center":
                            StyleConstants.setAlignment(style, StyleConstants.ALIGN_CENTER);
                            break;
                        case "Right":
                            StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
                            break;
                        case "Justify":
                            StyleConstants.setAlignment(style, StyleConstants.ALIGN_JUSTIFIED);
                            break;
                    }
                    docStyle.setParagraphAttributes(start, end - start, style, false);
                }
            });

            // Indent and outdent buttons
            indentButton.addActionListener(e -> {
                StyledDocument docStyle = textArea.getStyledDocument();
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                if (start == end) {
                    return;  
                }

                javax.swing.text.Style style = textArea.addStyle("Indent", null);
                StyleConstants.setLeftIndent(style, 20);
                docStyle.setParagraphAttributes(start, end - start, style, false);
            });

            outdentButton.addActionListener(e -> {
                StyledDocument docStyle = textArea.getStyledDocument();
                int start = textArea.getSelectionStart();
                int end = textArea.getSelectionEnd();

                if (start == end) {
                    return;  
                }

                javax.swing.text.Style style = textArea.addStyle("Outdent", null);
                StyleConstants.setLeftIndent(style, 0);
                docStyle.setParagraphAttributes(start, end - start, style, false);
            });

           
            undoButton.addActionListener(e -> {
                try {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                } catch (CannotUndoException ex) {
                    System.out.println("Cannot undo: " + ex);
                }
            });

            redoButton.addActionListener(e -> {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                } catch (CannotRedoException ex) {
                    System.out.println("Cannot redo: " + ex);
                }
            });

       
            cutButton.addActionListener(e -> textArea.cut());
            copyButton.addActionListener(e -> textArea.copy());
            pasteButton.addActionListener(e -> textArea.paste());

            
            findButton.addActionListener(e -> {
                String findText = JOptionPane.showInputDialog(frame, "Enter text to find:");
                if (findText != null && !findText.isEmpty()) {
                   
                    Highlighter highlighter = textArea.getHighlighter();
                    highlighter.removeAllHighlights();
                    
                   
                    String content = textArea.getText();
                    int lastIndex = 0;
                    int findLength = findText.length();
                    boolean found = false;
                    int occurrences = 0;
                    
                    while (lastIndex != -1) {
                        lastIndex = content.indexOf(findText, lastIndex);
                        
                        if (lastIndex != -1) {
                            try {
                               
                                highlighter.addHighlight(lastIndex, lastIndex + findLength, highlightPainter);
                                occurrences++;
                                
                               
                                if (!found) {
                                    textArea.setCaretPosition(lastIndex);
                                    found = true;
                                }
                                
                                lastIndex += findLength;
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    
                    if (!found) {
                        JOptionPane.showMessageDialog(frame, "Text not found.");
                    } else {
                        statusLabel.setText("Found " + occurrences + " occurrence(s) of \"" + findText + "\"");
                    }
                }
            });

            replaceButton.addActionListener(e -> {
                String findText = JOptionPane.showInputDialog(frame, "Enter text to find:");
                if (findText != null) {
                    String replaceText = JOptionPane.showInputDialog(frame, "Enter text to replace:");
                    if (replaceText != null) {
                        String content = textArea.getText();
                        content = content.replace(findText, replaceText);
                        textArea.setText(content);
                    }
                }
            });
            
            // Speech recognition button
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
            
           
            frame.setLayout(new BorderLayout());
            frame.add(toolBar, BorderLayout.NORTH);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(statusLabel, BorderLayout.SOUTH);
            frame.setSize(900, 700);  
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

            // File operations
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

            savePdfItem.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
                int option = fileChooser.showSaveDialog(frame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".pdf")) {
                        filePath += ".pdf";
                    }
                    
                    try {
                        exportToPdf(textArea, filePath);
                        JOptionPane.showMessageDialog(frame, "PDF saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Error saving PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            });

            exitItem.addActionListener(e -> frame.dispose());
        });
    }
  
    private static void exportToPdf(JTextPane textPane, String filePath) throws Exception {
        try {
            
            org.apache.pdfbox.pdmodel.PDDocument document = new org.apache.pdfbox.pdmodel.PDDocument();
            
          
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage(
                org.apache.pdfbox.pdmodel.common.PDRectangle.A4
            );
            document.addPage(page);
            

            org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = 
                new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
                
            org.apache.pdfbox.pdmodel.font.PDType1Font font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
            float fontSize = 12;
            contentStream.setFont(font, fontSize);
            
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float startX = margin;
            float leading = 1.5f * fontSize;
            
            String text = textPane.getText();
            
         
            String[] lines = text.split("\n");
            
          
            contentStream.beginText();
            contentStream.newLineAtOffset(startX, yPosition);
            
           
            for (String line : lines) {
                
                line = line.replace("\t", "    ");
                
                
                line = line.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
                
             
                if (yPosition < margin + leading) {
                    contentStream.endText();
                    contentStream.close();
                    
                   
                    page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                    document.addPage(page);
                    
                    contentStream = new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page);
                    contentStream.setFont(font, fontSize);
                    yPosition = page.getMediaBox().getHeight() - margin;
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(startX, yPosition);
                }
                
                
                if (!line.isEmpty()) {
                    try {
                        contentStream.showText(line);
                    } catch (IllegalArgumentException e) {
                        
                        System.err.println("Warning: Skipping problematic line: " + e.getMessage());
                    }
                }
                
                contentStream.newLineAtOffset(0, -leading);
                yPosition -= leading;
            }
            
        
            contentStream.endText();
            contentStream.close();
            
            document.save(filePath);
            document.close();
            
        } catch (Exception e) {
            throw new Exception("Error creating PDF: " + e.getMessage(), e);
        }
    }
}
