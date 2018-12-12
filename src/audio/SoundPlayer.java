/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

//Example 17-3. SoundPlayer.java


import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;
/**
* @author rwitmer
*/


// TODO: agregar a AxxonAaudio la barra de progreso ?

/**
*
* This class is a Swing component that can load and play a sound clip,
* displaying progress and controls. The main( ) method is a test program.
* This component can play sampled audio or MIDI files, but handles them 
* differently. For sampled audio, time is reported in microseconds, tracked in
* milliseconds and displayed in seconds and tenths of seconds. This program 
* does no transcoding, so it can only play sound files that use the PCM encoding.
*/
public class SoundPlayer extends JComponent {
Clip clip; // Contents of a sampled audio file
boolean playing = false; // whether the sound is currently playing

// Length and position of the sound are measured in milliseconds for 
// sampled sounds and MIDI "ticks" for MIDI sounds
int audioLength; // Length of the sound. 
int audioPosition = 0; // Current position within the sound

// The following fields are for the GUI
JButton play; // The Play button
JButton stop; // The Stop button 
JSlider progress; // Shows and sets current position in sound
JLabel time; // Displays audioPosition as a number
Timer timer; // Updates slider every 100 milliseconds

File filename;
SourceDataLine line = null;
AudioInputStream audioInputStream = null;
PlayerThread pt;

// Create a SoundPlayer component for the specified file.
public SoundPlayer(File f)
throws IOException,
UnsupportedAudioFileException,
LineUnavailableException

{

// Now create the basic GUI
play = new JButton("Play"); // Play button
stop = new JButton("Stop"); // Stop button 
play.setEnabled(false);
stop.setEnabled(false);

// When clicked, start playing the sound
play.addActionListener(new ActionListener( ) {
public void actionPerformed(ActionEvent e) {
if (!playing){
pt = new PlayerThread();
pt.start();
}
}
});

// When clicked, stop playing the sound
stop.addActionListener(new ActionListener( ) {
public void actionPerformed(ActionEvent e) {
if (playing) stop( );
}
});

// put those controls in a row
Box row = Box.createHorizontalBox( );
row.add(play);
row.add(stop);

// And add them to this component.
setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
this.add(row);

}

public void setFile(File f) {

filename = f;

}

class PlayerThread extends Thread {
public void run() {
try
{
audioInputStream = AudioSystem.getAudioInputStream(filename);
}
catch (Exception e)
{ 
e.printStackTrace();
}

AudioFormat audioFormat = audioInputStream.getFormat();
DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);

try
{
line = (SourceDataLine) AudioSystem.getLine(info);
line.open(audioFormat);
}
catch (LineUnavailableException e)
{
e.printStackTrace();
System.exit(1);
}
catch (Exception e)
{
e.printStackTrace();
}

playing = true;

line.start();
int nBytesRead = 0;
byte[] abData = new byte[128000];
while (nBytesRead != -1)
{
try
{
nBytesRead = audioInputStream.read(abData, 0, abData.length);
int temp [] = new int[abData.length];
}
catch (IOException e)
{
e.printStackTrace();
}
if (nBytesRead >= 0)
{
int nBytesWritten = line.write(abData, 0, nBytesRead);
}
}

line.drain();
line.close();

}
}

/** Stop playing the sound*/
public void stop( ) {
line.stop();
line.flush();
line.close();
pt.interrupt();
pt = null;

playing = false;
}

}