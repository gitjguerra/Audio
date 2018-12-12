/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileEncoder extends Thread {

	private File inputFile;
	private File outputFile;
	private EncodingAttributes attrs;
	private Encoder encoder;
	
	public FileEncoder(String inFileName, String outFileName)
	{
            try {
                encoder = new Encoder();
                AudioAttributes audio = new AudioAttributes();
                audio.setCodec("libmp3lame");
                audio.setBitRate(320000);
                audio.setChannels(2);
                audio.setSamplingRate(44100);
                
                attrs = new EncodingAttributes();
                attrs.setFormat("mp3");
                attrs.setAudioAttributes(audio);
                
                inputFile = new File(inFileName);
                outputFile = new File(outFileName);
                
                encoder.encode(inputFile, outputFile, attrs);
                System.out.println("Audio saved to file: " + outputFile.getName());
                inputFile.delete();
                
            } catch (IllegalArgumentException | EncoderException ex) {
                Logger.getLogger(FileEncoder.class.getName()).log(Level.SEVERE, null, ex);
            }
	}
}