package com.example.texteditor;

import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.protobuf.ByteString;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpeechRecognitionUtil {
    private final JTextPane textPane;
    private final JLabel statusLabel;
    private TargetDataLine microphone;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    
    public SpeechRecognitionUtil(JTextPane textPane, JLabel statusLabel) {
        this.textPane = textPane;
        this.statusLabel = statusLabel;
        System.out.println("SpeechRecognitionUtil initialized");
    }
    
    public void startRecording() {
        if (isRecording.get()) {
            return;
        }
        
        isRecording.set(true);
        statusLabel.setText("Starting speech recognition...");
        System.out.println("Speech recognition starting");
        
        String credPath = "/home/alexander/git_repos/AI-based-real-time-text-editor/text_editor/credentials.json";
        
        File credFile = new File(credPath);
        if (!credFile.exists()) {
            System.err.println("Credentials file not found: " + credPath);
            JOptionPane.showMessageDialog(null, 
                "Credentials file not found at:\n" + credPath, 
                "Error", JOptionPane.ERROR_MESSAGE);
            isRecording.set(false);
            statusLabel.setText("Ready");
            return;
        } else {
            System.out.println("Using credentials at: " + credPath);
        }
        
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credPath);
        
        executor.submit(() -> {
            try {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                
                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Microphone format not supported");
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Microphone not supported");
                    });
                    isRecording.set(false);
                    return;
                }
                
                try {
                    microphone = (TargetDataLine) AudioSystem.getLine(info);
                    microphone.open(format);
                    microphone.start();
                    System.out.println("Microphone started successfully");
                } catch (Exception e) {
                    System.err.println("Error opening microphone: " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Microphone error");
                    });
                    isRecording.set(false);
                    return;
                }
                
                statusLabel.setText("Listening...");
                
                // Try to create a speech client first to check API connection
                try (SpeechClient testClient = SpeechClient.create()) {
                    System.out.println("Successfully connected to Speech API");
                } catch (Exception e) {
                    System.err.println("Failed to connect to Speech API: " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("API Connection Error");
                    });
                    isRecording.set(false);
                    stopMicrophone();
                    return;
                }
                
                // Use a buffer for collecting more audio data
                ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
                long startTime = System.currentTimeMillis();
                
                while (isRecording.get()) {
                    byte[] data = new byte[4096];
                    int bytesRead = microphone.read(data, 0, data.length);
                    
                    if (bytesRead > 0) {
                        audioBuffer.write(data, 0, bytesRead);
                        
                        // Process audio in chunks for better recognition
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - startTime > 1000) { // Process every 1 second
                            byte[] audioData = audioBuffer.toByteArray();
                            System.out.println("Processing " + audioData.length + " bytes of audio");
                            
                            processAudioData(audioData);
                            
                            // Reset buffer
                            audioBuffer.reset();
                            startTime = currentTime;
                        }
                    }
                    
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                // Process any remaining audio
                byte[] remainingAudio = audioBuffer.toByteArray();
                if (remainingAudio.length > 0) {
                    processAudioData(remainingAudio);
                }
                
            } catch (Exception e) {
                System.err.println("Error in recording thread: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                });
            } finally {
                stopMicrophone();
            }
        });
    }
    
    private void processAudioData(byte[] audioData) {
        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("en-US")
                    .build();
            
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioData))
                    .build();
            
            RecognizeRequest request = RecognizeRequest.newBuilder()
                    .setConfig(config)
                    .setAudio(audio)
                    .build();
            
            RecognizeResponse response = speechClient.recognize(request);
            List<SpeechRecognitionResult> results = response.getResultsList();
            
            if (!results.isEmpty()) {
                System.out.println("Speech detected! Results count: " + results.size());
                
                for (SpeechRecognitionResult result : results) {
                    for (SpeechRecognitionAlternative alternative : result.getAlternativesList()) {
                        String transcript = alternative.getTranscript();
                        
                        System.out.println("Transcript: \"" + transcript + 
                                           "\" (confidence: " + alternative.getConfidence() + ")");
                        
                        if (!transcript.isEmpty()) {
                            final String textToAdd = transcript + " ";
                            
                            SwingUtilities.invokeLater(() -> {
                               
                               
                                
                                // Verify the text area is available
                                if (textPane != null) {
                                    System.out.println("Adding text to text area: " + textToAdd);
                                    // textArea.append(textToAdd);
                                    StyledDocument doc = textPane.getStyledDocument();
                                    try {
                                        doc.insertString(doc.getLength(), textToAdd, null);  // 'null' means no style
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.err.println("Text area is null!");
                                }
                            });
                        }
                    }
                }
            } else {
                System.out.println("No speech detected in " + audioData.length + " bytes of audio");
            }
        } catch (Exception e) {
            System.err.println("Error in speech recognition: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stopRecording() {
        isRecording.set(false);
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Ready");
        });
        stopMicrophone();
        System.out.println("Speech recognition stopped");
    }
    
    private void stopMicrophone() {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
            System.out.println("Microphone stopped");
        }
    }
}
