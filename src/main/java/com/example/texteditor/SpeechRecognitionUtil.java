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
import java.util.ArrayList;

public class SpeechRecognitionUtil {
    private final JTextPane textPane;
    private final JLabel statusLabel;
    private TargetDataLine microphone;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    
    // Improved audio buffer parameters
    private static final int SAMPLE_RATE = 16000;
    private static final int BUFFER_SIZE = 6400; // 400ms of audio at 16kHz
    private static final int BYTES_PER_SAMPLE = 2; // 16-bit audio
    
    /**
     * Constructor for the speech recognition utility.
     * 
     * @param textPane The JTextPane to append transcribed text to
     * @param statusLabel A JLabel to show the current status of speech recognition
     */
    public SpeechRecognitionUtil(JTextPane textPane, JLabel statusLabel) {
        this.textPane = textPane;
        this.statusLabel = statusLabel;
    }
    
    /**
     * Starts the speech recognition recording process.
     */
    public void startRecording() {
        if (isRecording.get()) {
            return;  // Already recording
        }
        
        isRecording.set(true);
        statusLabel.setText("Initializing speech recognition...");
        
        // Prompt user for credentials file location
        String defaultCredPath = "/home/alexander/git_repos/AI-based-real-time-text-editor/text_editor/credentials.json";
        File defaultCredFile = new File(defaultCredPath);
        
        int option = JOptionPane.showConfirmDialog(
            null,
            "Use credentials file at:\n" + defaultCredPath + "?",
            "Confirm Credentials",
            JOptionPane.YES_NO_CANCEL_OPTION
        );
        
        if (option == JOptionPane.CANCEL_OPTION) {
            isRecording.set(false);
            statusLabel.setText("Speech recognition canceled");
            return;
        }
        
        String credPath = defaultCredPath;
        
        if (option == JOptionPane.NO_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select credentials.json file");
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                credPath = fileChooser.getSelectedFile().getAbsolutePath();
            } else {
                isRecording.set(false);
                statusLabel.setText("Speech recognition canceled");
                return;
            }
        }
        
        final String finalCredPath = credPath;
        
        executor.submit(() -> {
            try {
                // Initialize microphone with better settings
                AudioFormat format = new AudioFormat(
                    SAMPLE_RATE, // Sample rate
                    16,          // Sample size in bits
                    1,           // Channels (1 = mono)
                    true,        // Signed
                    false        // Big endian
                );
                
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                
                if (!AudioSystem.isLineSupported(info)) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Microphone not supported");
                        JOptionPane.showMessageDialog(null, 
                            "Your system does not support microphone input with the required format.",
                            "Microphone Error", JOptionPane.ERROR_MESSAGE);
                    });
                    isRecording.set(false);
                    return;
                }
                
                // Try to list available microphones for debugging
                Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
                System.out.println("Available audio devices:");
                for (Mixer.Info mixerInfo : mixerInfos) {
                    System.out.println("  " + mixerInfo.getName() + " - " + mixerInfo.getDescription());
                }
                
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format, BUFFER_SIZE * BYTES_PER_SAMPLE);
                microphone.start();
                
                // Verify credentials file exists
                File credFile = new File(finalCredPath);
                if (!credFile.exists()) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Error: credentials file not found");
                        JOptionPane.showMessageDialog(null, 
                            "Credentials file not found at:\n" + finalCredPath,
                            "File Not Found", JOptionPane.ERROR_MESSAGE);
                    });
                    isRecording.set(false);
                    return;
                }
                
                System.out.println("Using credentials at: " + finalCredPath);
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", finalCredPath);
                
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Listening... (speak clearly into your microphone)");
                });
                
                // Use a better recognition approach - collect larger chunks of audio
                long startTime = System.currentTimeMillis();
                
                // Use streaming recognition with accumulated audio
                ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE * BYTES_PER_SAMPLE];
                
                while (isRecording.get()) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    
                    if (bytesRead > 0) {
                        // Check audio levels to detect if speaking
                        double level = calculateAudioLevel(buffer, bytesRead);
                        updateAudioLevelIndicator(level);
                        
                        audioStream.write(buffer, 0, bytesRead);
                        
                        // Every 3 seconds, process the accumulated audio
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - startTime > 3000) {
                            byte[] audioData = audioStream.toByteArray();
                            processAudioChunk(audioData);
                            
                            // Reset for next chunk
                            audioStream.reset();
                            startTime = currentTime;
                        }
                    }
                    
                    // Small delay to prevent CPU overuse
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                // Process any remaining audio
                if (audioStream.size() > 0) {
                    processAudioChunk(audioStream.toByteArray());
                }
                
            } catch (LineUnavailableException e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                });
                e.printStackTrace();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "Speech recognition error: " + e.getMessage() + 
                        "\nSee console for details.", 
                        "Recognition Error", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                stopMicrophone();
            }
        });
    }
    
    /**
     * Stops the speech recognition recording process.
     */
    public void stopRecording() {
        isRecording.set(false);
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Ready");
        });
        stopMicrophone();
    }
    
    private void stopMicrophone() {
        if (microphone != null && microphone.isOpen()) {
            microphone.stop();
            microphone.close();
        }
    }
    
    /**
     * Calculates the audio level (volume) of the captured audio.
     */
    private double calculateAudioLevel(byte[] buffer, int bytesRead) {
        // For 16-bit samples
        long sum = 0;
        for (int i = 0; i < bytesRead; i += 2) {
            short sample = (short) ((buffer[i+1] << 8) | (buffer[i] & 0xff));
            sum += Math.abs(sample);
        }
        
        // Normalize to 0.0-1.0 range (16-bit has max value of 32768)
        double avgLevel = sum / (bytesRead / 2.0) / 32768.0;
        return avgLevel;
    }
    
    /**
     * Updates the status label with an indicator of audio level.
     */
    private void updateAudioLevelIndicator(double level) {
        // Only update occasionally to reduce UI updates
        if (Math.random() < 0.1) { // 10% chance to update
            final int bars = (int)(level * 20);
            final StringBuilder indicator = new StringBuilder("Listening [");
            for (int i = 0; i < 20; i++) {
                indicator.append(i < bars ? "|" : " ");
            }
            indicator.append("]");
            
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(indicator.toString());
            });
            
            if (level > 0.05) { // Roughly detect if there's meaningful audio
                System.out.println("Audio level: " + level + " (" + bars + " bars)");
            }
        }
    }
    
    /**
     * Process a larger chunk of audio for better recognition.
     */
    private void processAudioChunk(byte[] audioData) {
        if (audioData.length == 0) return;
        
        try (SpeechClient speechClient = SpeechClient.create()) {
            System.out.println("Processing audio chunk: " + audioData.length + " bytes");
            
            // Configure recognition request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(AudioEncoding.LINEAR16)
                    .setSampleRateHertz(SAMPLE_RATE)
                    .setLanguageCode("en-US")
                    .setEnableAutomaticPunctuation(true)  // Add punctuation automatically
                    .setModel("default")  // Use default model (most accurate)
                    .build();
            
            // Create the audio input object
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioData))
                    .build();
            
            // Request speech recognition
            RecognizeRequest request = RecognizeRequest.newBuilder()
                    .setConfig(config)
                    .setAudio(audio)
                    .build();
            
            // Get response
            RecognizeResponse response = speechClient.recognize(request);
            List<SpeechRecognitionResult> results = response.getResultsList();
            
            // Process results
            if (!results.isEmpty()) {
                for (SpeechRecognitionResult result : results) {
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                    String transcript = alternative.getTranscript();
                    
                    if (!transcript.isEmpty()) {
                        System.out.println("Transcript detected: " + transcript);
                        final String textToAdd = transcript + " ";
                        
                        // Update the text pane on the EDT
                        SwingUtilities.invokeLater(() -> {
                            try {
                                int caretPos = textPane.getCaretPosition();
                                StyledDocument doc = textPane.getStyledDocument();
                                doc.insertString(caretPos, textToAdd, null);
                                textPane.setCaretPosition(caretPos + textToAdd.length());
                            } catch (BadLocationException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } else {
                System.out.println("No speech detected in audio chunk (" + audioData.length + " bytes)");
            }
        } catch (Exception e) {
            System.err.println("Recognition error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
