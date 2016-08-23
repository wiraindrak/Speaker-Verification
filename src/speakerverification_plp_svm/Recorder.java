package speakerverification_plp_svm;

import java.io.File;
import java.io.IOException;
import static java.util.Arrays.stream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import static java.nio.file.Files.list;
import static java.rmi.Naming.list;
import static java.util.Arrays.stream;
import static java.util.Collections.list;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Recorder {
    private int DURATION;
    private int NUM_BITS_PER_SAMPLE;
    private float SAMPLE_RATE;
    private int N_CHANNEL;
    private static boolean stopped;
    private static TargetDataLine line = null;
    private String audioFileName;
    
    public double[] amplitudeData = null;
    public byte[] audioData = null;

    public Recorder(){
        this.DURATION = 3000;
        this.NUM_BITS_PER_SAMPLE = 8;
        this.SAMPLE_RATE = 10000;
        this.N_CHANNEL = 1;
        stopped = true;
    }
    
    public void setNChannel(int ch){
        this.N_CHANNEL = ch;
    }
    
    public void setNumBytesPerSample(int nbps){
        this.NUM_BITS_PER_SAMPLE = nbps;
    }
    
    public void setSampleRate(float sr){
        this.SAMPLE_RATE = sr;
    }
    
    public void setAudioFileName(String afn){
        this.audioFileName = afn + ".wav";
    }
    
    public boolean isRecording(){
        return !stopped;
    }
    
    public void startRecord(){
        stopped = false;
        AudioFormat format = new AudioFormat(SAMPLE_RATE, NUM_BITS_PER_SAMPLE, N_CHANNEL, true, false);
        
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if( !AudioSystem.isLineSupported(info) ){
            System.out.println("Error : Line is not supported.");
            System.exit(0);
        }
        
        try{
            String dirSaveTarget = "dataset/";
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            AudioInputStream audioSys = new AudioInputStream(line);
            AudioSystem.write(audioSys, AudioFileFormat.Type.WAVE, new File(dirSaveTarget + this.audioFileName));
        } catch(LineUnavailableException | IOException ex) {
            System.out.println("Error : Line is unavailable.");
        }
    }
    
    public void stopRecord(){
        stopped = true;
        line.stop();
        line.close();
    }
    
    public void getAudio(String fileName){
        
        this.setNumBytesPerSample(NUM_BITS_PER_SAMPLE);

        this.setAudioFileName(fileName);
        this.setSampleRate(SAMPLE_RATE);
        this.setNChannel(N_CHANNEL);

        Thread recordHandler = new Thread(() -> {
            try {
                Thread.sleep(DURATION);
            } catch (InterruptedException ex) {
                System.out.println("Error : Unexpected interrupt.");
            }
            this.stopRecord();
        });
        recordHandler.start();
        this.startRecord();
    }    
}
