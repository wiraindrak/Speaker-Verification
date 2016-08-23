/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speakerverification_plp_svm;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author WiraIndra
 */
public class Audio {
    
    public File file;
    public Double[] amplitudeData = null;
    public byte[] audioData = null;    
    
    public Audio(File file){
        this.file = file;
    }
    
    private Double[] getData(byte[] audioData) {
        int N = audioData.length;
        Double[] data = new Double[N / 2];
        for (int i = 0; i < N / 2; i++) {
            data[i] = ((short) (((audioData[2 * i + 1] & 0xFF) << 8) + (audioData[2 * i] & 0xFF))) / ((double) Short.MAX_VALUE);
        }
        return data;
    }

    public void getAudioData() {
        AudioInputStream stream = null;
        try {
            stream = AudioSystem.getAudioInputStream(file);

            audioData = new byte[stream.available()];
            stream.read(audioData);
            amplitudeData = getData(audioData);
        } catch (UnsupportedAudioFileException | IOException ex) {
            System.out.println("An error occured while loading audio file.");
        }
    }
}
