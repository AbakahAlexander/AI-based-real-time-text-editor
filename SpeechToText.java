package text_editor;

import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionConfig.*;
import com.google.cloud.speech.v1.RecognitionAudio.*;
import com.google.protobuf.ByteString;
import javax.sound.sampled.*;
import java.io.*;
import java.util.List;

public class SpeechToText {
    public static void main(String[] args) throws Exception {
        // Set Google Cloud credentials path
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "/home/alexander/git_repos/AI-based-real-time-text-editor/text_editor/credentials.json");

        // Set up the audio capture
        TargetDataLine microphone;
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Microphone not supported");
            System.exit(1);
        }
        
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        
        // Capture audio and send to Google Cloud API
        byte[] data = new byte[1024];
        AudioInputStream audioInputStream = new AudioInputStream(microphone);
        RecognitionAudio recognitionAudio = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(audioInputStream.read(data, 0, data.length))).build();
        
        // Set up Google Cloud Speech Client
        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("en-US")
                    .build();
            RecognizeRequest request = RecognizeRequest.newBuilder()
                    .setConfig(config)
                    .setAudio(recognitionAudio)
                    .build();

            // Get speech-to-text result
            RecognizeResponse response = speechClient.recognize(request);
            List<SpeechRecognitionResult> results = response.getResultsList();
            
            for (SpeechRecognitionResult result : results) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                String transcript = alternative.getTranscript();
                System.out.println("Transcript: " + transcript); // You can display this in the editor's text area
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
