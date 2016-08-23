package speakerverification_plp_svm;

import java.io.File;
import java.util.Arrays;

public class PLP {

    public int sampleRate = 10000; //sample rate 10kHz
    public double overlap = 0.5; //overlapping setengah dari frame
    public double pframe = 200; //frame 20ms
    public int durasi; //panjang data asli 2s
    public int jumlahFrame;
    public int panjangFrame;

    public Double[][] power;
    public Complex[][] temp;

    public Audio audio;

    public PLP(File file) {
        audio = new Audio(file);
        audio.getAudioData();
        this.durasi = audio.amplitudeData.length;
        this.setJumlahFrame(jumlahFrame);
        this.setPanjangFrame(panjangFrame);
    }

    public void setJumlahFrame(int jumlahFrame) {
        this.jumlahFrame = (int) ((int) 10000 / pframe)*2-1;
    }

    public void setPanjangFrame(int panjangFrame) {
        this.panjangFrame = (int) ((int) (sampleRate) / (10000/pframe));
    }

    public double[][] framming() {

        double[][] frame = new double[this.jumlahFrame][this.panjangFrame];

        int idxData = 0;
        int idxFrame = 0;
        int i = 0;

        while (idxFrame != this.jumlahFrame) {
            frame[idxFrame][idxData] = audio.amplitudeData[i];
            if (idxData == panjangFrame - 1) {
                idxFrame++;
                i = (int) (i - (panjangFrame * overlap) + 1);
                idxData = 0;
            } else {
                i++;
                idxData++;
            }
        }
        return frame;
    }

    public double[][] hammingWindow() {

        double[][] windowedFrame = new double[this.jumlahFrame][this.panjangFrame];
        double[][] x = this.framming();
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                windowedFrame[i][j] = (0.54 - 0.46 * Math.cos((2 * Math.PI * j) / (x[i].length - 1))) * x[i][j];
            }
        }
        return windowedFrame;
    }

    public Complex[] fft(Complex[] x) {
        int N = x.length;

        // base case
        if (N == 1) {
            return new Complex[]{x[0]};
        }

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0) {
            throw new RuntimeException("N is not a power of 2");
        }

        // fft of even terms
        Complex[] even = new Complex[N / 2];
        for (int k = 0; k < N / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd = even;  // reuse the array
        for (int k = 0; k < N / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].Add(wk.Mult(r[k]));
            y[k + N / 2] = q[k].Sub(wk.Mult(r[k]));
        }
        return y;
    }

    public void runFFT() {
        double[][] w = this.hammingWindow();
        int harusnyaNambahSegini = 0;

        Complex[] x;

        double hasilLog2 = Math.log(w[0].length) / Math.log(2);
        double selisih = hasilLog2 - Math.round(hasilLog2);

        if (selisih != 0) {
            harusnyaNambahSegini = (int) (Math.pow(2, Math.ceil(hasilLog2)) - w[0].length);
            x = new Complex[w[0].length + harusnyaNambahSegini];
            this.temp = new Complex[w.length][w[0].length + harusnyaNambahSegini];            
        } else {
            x = new Complex[w[0].length];
            this.temp = new Complex[w.length][w[0].length];
        }

        for (int i = 0; i < w.length; i++) {

            for (int j = 0; j < w[i].length + harusnyaNambahSegini; j++) {
                if (j < w[i].length) {
                    x[j] = new Complex(w[i][j], 0.0);
                } else {
                    x[j] = new Complex(0.0, 0.0);
                }
            }
            Complex[] p = this.fft(x);            
            for (int j = 0; j < p.length; j++) {
                this.temp[i][j] = p[j];
            }
        }
        this.getPowerSpectrum(temp);
    }

    public void getPowerSpectrum(Complex[][] data) {
        this.power = new Double[data.length][data[0].length];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {                
                this.power[i][j] = data[i][j].dReal * data[i][j].dReal + data[i][j].dImaginary * data[i][j].dImaginary;     
            }
        }       
    }
}
