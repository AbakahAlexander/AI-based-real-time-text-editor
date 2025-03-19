package com.example.texteditor;

import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.protobuf.ByteString;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SpeechRecognitionUtil {
    private final JTextArea textArea;
    private volatile boolean isRecording = false;
    private Thread recordingThread;
    private TargetDataLine microphone;
    private static final Logger logger = Logger.getLogger(SpeechRecognitionUtil.class.getName());
    private JLabel statusLabel = null;
    
    public SpeechRecognitionUtil(JTextArea textArea) {
        this.textArea = textArea;
    }
    
    public SpeechRecognitionUtil(JTextArea textArea, JLabel statusLabel) {
        this.textArea = textArea;
        this.statusLabel = statusLabel;
    }
    
    public void startRecording() {
        if (isRecording) {
            return;
        }
        
        try {
            String credentialsPath = promptForCredentialsFile();
            if (credentialsPath == null || credentialsPath.isEmpty()) {
                updateStatus("Speech recognition cancelled - no credentials file selected");
                return;
            }
            
            System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);
            updateStatus("Listening for speech...");
            
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                updateStatus("ERROR: Microphone not supported");
                return;
            }
            
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            
            isRecording = true;
            
            recordingThread = new Thread(() -> {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bufferSize = 0;
                
                try {
                    try (SpeechClient speechClient = SpeechClient.create()) {
                        logger.info("Connected to Google Speech API");
                        
                        while (isRecording) {
                            bufferSize = microphone.read(buffer, 0, buffer.length);
                            
                            if (bufferSize > 0) {
                                out.write(buffer, 0, bufferSize);
                                
                                if (out.size() > 16000) {
                                    byte[] audioData = out.toByteArray();
                                    out.reset();
                                    
                                    new Thread(() -> processAudioChunk(audioData, speechClient)).start();
                                }
                            }
                            
                            Thread.sleep(100);
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error in speech recognition", e);
                    updateStatus("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            recordingThread.start();
            
        } catch (Exception e) {
            updateStatus("Error: " + e.getMessage());
            e.printStackTrace();
            stopRecording();
        }
    }
    
    private String promptForCredentialsFile() {
        String defaultPath = "/home/alexander/git_repos/text_editor/credentials.json";
        if (Files.exists(Paths.get(defaultPath))) {
            return defaultPath;
        }
        
        String envPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (envPath != null && !envPath.isEmpty() && Files.exists(Paths.get(envPath))) {
            int response = JOptionPane.showConfirmDialog(
                null,
                "Use existing credentials file?\n" + envPath,
                "Credentials Found",
                JOptionPane.YES_NO_OPTION
            );
            
            if (response == JOptionPane.YES_OPTION) {
                return envPath;
            }
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Google Cloud Credentials File (JSON)");
        
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome + "/Downloads");
        if (downloadsDir.exists()) {
            fileChooser.setCurrentDirectory(downloadsDir);
        }
        
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        
        return null;
    }
    
    private void processAudioChunk(byte[] audioData, SpeechClient speechClient) {
        try {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("en-US")
                .build();
            
            ByteString audioBytes = ByteString.copyFrom(audioData);
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(audioBytes)
                .build();
            
            RecognizeRequest request = RecognizeRequest.newBuilder()
                .setConfig(config)
                .setAudio(audio)
                .build();
            
            RecognizeResponse response = speechClient.recognize(request);
            
            if (response.getResultsCount() > 0) {
                for (SpeechRecognitionResult result : response.getResultsList()) {
                    String transcript = result.getAlternativesList().get(0).getTranscript();
                    if (!transcript.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            textArea.append(transcript + " ");
                        });
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing audio chunk", e);
        }
    }
    
    public void stopRecording() {
        isRecording = false;
        updateStatus("Speech recognition stopped");
        
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }
    }
    
    private void updateStatus(String message) {
        logger.info(message);
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(message));
        }
    }
}
