package audio;

import connection.Connect;
import static connection.Connect.saveAudio;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.ParseException;
import java.util.Date;
import javax.sound.sampled.*;
import model.DocFile;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AxxonAudio class. 
 *
 * @author jguerra
 */
public class AxxonAudio extends JFrame{

        private boolean stopCapture = false;
        private ByteArrayOutputStream byteArrayOutputStream;
        private AudioFormat audioFormat;
        private TargetDataLine targetDataLine;
        private AudioInputStream audioInputStream;
        private SourceDataLine sourceDataLine;
        private final Timestamp dateTime;
        
        public static void main(String args[]) throws IOException, UnsupportedAudioFileException, LineUnavailableException, ParseException{
            AxxonAudio axxonAudio = new AxxonAudio();
        }//end main

        // Constructor
        public AxxonAudio() { 
            
            final JButton captureBtn    = new JButton("Grabar");
            final JButton stopBtn       = new JButton("Detener");
            final JButton playBtn       = new JButton("Escuchar");
            final JButton saveBtn       = new JButton("Guardar y Cerrar");
            
            dateTime = new Timestamp(new Date().getTime());
                    
            captureBtn.setEnabled(true);
            stopBtn.setEnabled(false);
            playBtn.setEnabled(false);
            saveBtn.setEnabled(false);

            //Register anonymous listeners
            captureBtn.addActionListener((ActionEvent e) -> {
                captureBtn.setEnabled(false);
                stopBtn.setEnabled(true);
                playBtn.setEnabled(false);
                saveBtn.setEnabled(false);
                //Capture input data from the
                // microphone until the Stop button is
                // clicked.
                captureAudio();
            } //end actionPerformed
            //end ActionListener
            );//end addActionListener()
            getContentPane().add(captureBtn);

            stopBtn.addActionListener((ActionEvent e) -> {
                captureBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                playBtn.setEnabled(true);
                saveBtn.setEnabled(true);
                //Terminate the capturing of input data
                // from the microphone.
                stopCapture = true;
            } //end actionPerformed
            //end ActionListener
            );//end addActionListener()
            getContentPane().add(stopBtn);

            playBtn.addActionListener((ActionEvent e) -> {
                //Play back all of the data that was
                // saved during capture.
                playAudio();
            } //end actionPerformed
            //end ActionListener
            );//end addActionListener()
            getContentPane().add(playBtn);

            saveBtn.addActionListener((ActionEvent e) -> {
                // TODO: Crear action listener una vez todo funcione
                // saved and write the register on the db
                //Connect connect = new Connect();
                saveAndExit();

            } //end actionPerformed
            //end ActionListener
            );//end addActionListener()
            getContentPane().add(saveBtn);
            
            getContentPane().setLayout(new FlowLayout());
            setTitle("Dictáfono");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(300,200);
            setLocation(1000, 520);
            setVisible(true);
            setAlwaysOnTop(true);
    }//end constructor

    //This method captures audio input from a
    // microphone and saves it in a
    // ByteArrayOutputStream object.
    private void captureAudio() {

        try{
            //Get and display a list of
            // available mixers.
            Mixer.Info[] mixerInfo = 
                            AudioSystem.getMixerInfo();
            
            // Available mixers
            //System.out.println("Available mixers:");
            //for (Mixer.Info mixerInfo1 : mixerInfo) {
            //    System.out.println(mixerInfo1.getName());
            //} //end for loop

            //Get everything set up for capture
            audioFormat = getAudioFormat();

            DataLine.Info dataLineInfo =
                                  new DataLine.Info(
                                  TargetDataLine.class,
                                  audioFormat);

            //Select one of the available
            // mixers.
            Mixer mixer = AudioSystem.
                                getMixer(mixerInfo[3]);

            //Get a TargetDataLine on the selected
            // mixer.
            targetDataLine = (TargetDataLine)
                           mixer.getLine(dataLineInfo);
            //Prepare the line for use.
            targetDataLine.open(audioFormat);
            targetDataLine.start();

            //Create a thread to capture the microphone
            // data and start it running.  It will run
            // until the Stop button is clicked.
            Thread captureThread = new CaptureThread();
            captureThread.start();

        } catch (LineUnavailableException e) {
            System.out.println(e);
            System.exit(0);
        }//end catch    
    }//end captureAudio method

    //This method plays back the audio data that
    // has been saved in the ByteArrayOutputStream
    private void playAudio() {
        try{
          //Get everything set up for playback.
          //Get the previously-saved data into a byte
          // array object.
          byte audioData[] = byteArrayOutputStream.
                                       toByteArray();
          //Get an input stream on the byte array
          // containing the data
          InputStream byteArrayInputStream =
                 new ByteArrayInputStream(audioData);
          AudioFormat audioFormat_resp = getAudioFormat();
          audioInputStream = new AudioInputStream(
                        byteArrayInputStream,
                        audioFormat_resp,
                        audioData.length/audioFormat_resp.
                                     getFrameSize());
          DataLine.Info dataLineInfo = 
                                new DataLine.Info(
                                SourceDataLine.class,
                                audioFormat_resp);
          sourceDataLine = (SourceDataLine)
                   AudioSystem.getLine(dataLineInfo);
          sourceDataLine.open(audioFormat_resp);
          sourceDataLine.start();

          //Create a thread to play back the data and
          // start it  running.  It will run until
          // all the data has been played back.
          Thread playThread = new PlayThread();
          playThread.start();

        } catch (LineUnavailableException e) {
          System.out.println(e);
          System.exit(0);
        }//end catch
    }//end playAudio

    //This method creates and returns an
    // AudioFormat object for a given set of format
    // parameters.  If these parameters don't work
    // well for you, try some of the other
    // allowable parameter values, which are shown
    // in comments following the declartions.
    private AudioFormat getAudioFormat(){
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(
                          sampleRate,
                          sampleSizeInBits,
                          channels,
                          signed,
                          bigEndian);
    }//end getAudioFormat

    private void saveAndExit() {

        DocFile docFile = new DocFile();  

        try{
            // convert byteArrayOutputStream to audioInputStream
            byte audioData[] = byteArrayOutputStream.toByteArray();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
            audioFormat = getAudioFormat();
            audioInputStream = new AudioInputStream(
                          bis,
                          audioFormat,
                          audioData.length/audioFormat.
                                       getFrameSize());

            // TODO: colocar los datos a insertar en la tabla deben pasar como argumentos a la llamada del main
            // Fill class with data
            docFile.setRepId(5);
            docFile.setDociuId(""); // campo vacio en la BD
            docFile.setDoceuId(""); // campo vacio en la BD
            docFile.setDocDesc("D_EXAMENES"); // de donde sale este dato ???   como parametro del main ???
            docFile.setDocPath("c:\\Temp\\"); // ../../dms/data/SND + fecha (año + mes + dia)
            docFile.setDocFileName("D_EXAMENES.5.2018.07.19.0.SND.wav"); // D_EXAMENES.5.2018.07.19.0.SND.mp3 - cambiar formato de wav a mp3
            docFile.setDocStatus(0);
            docFile.setDocType("SND");
            docFile.setCreation(dateTime);
            docFile.setModification(""); // campo vacio en la BD
            docFile.setUserId(5);
            docFile.setPlaceId(1);
            docFile.setUniqueId("5b8841a6633ad_6"); // Este campo que es ???

            // Write file
            AudioSystem.write(
                    audioInputStream
                   ,AudioFileFormat.Type.WAVE
                   ,new File(docFile.getDocPath() + docFile.getDocFileName()));

            // Write the register on the db
            Connect connect = new Connect();
            saveAudio(connect.connectDb(), docFile);

            // TODO: eliminar nombre y extension hardcode
            // Convert wav to mp3
            FileEncoder fileEncoder = new FileEncoder(docFile.getDocPath() + docFile.getDocFileName(), docFile.getDocPath() + "listo.mp3");
            if(fileEncoder == null){
                throw new Exception("No se logró convertir el archivo WAV a MP3, por favor comuniquese con Mediprocesos");
            }
            
            // Exit application
            System.exit(0);
            
          }catch (IOException e) {
            System.out.println(e);
            System.exit(0);
          } catch (Exception ex) {
                Logger.getLogger(AxxonAudio.class.getName()).log(Level.SEVERE, null, ex);
            }//end catch
    }

    public static File convertWAVtoMP3(File wav){
        File temp = null;
        
        return temp;
    }

    //Inner class to capture data from microphone
    class CaptureThread extends Thread{
        //An arbitrary-size temporary holding buffer
        byte tempBuffer[] = new byte[10000];

        @Override
        public void run(){

          byteArrayOutputStream =
                           new ByteArrayOutputStream();
          stopCapture = false;
          //Loop until stopCapture is set by
          // another thread that services the Stop
          // button.
          while(!stopCapture){
              //Read data from the internal buffer of
              // the data line.
              int cnt = targetDataLine.read(tempBuffer,
                      0,
                      tempBuffer.length);
              if(cnt > 0){
                  //Save data in output stream object.
                  byteArrayOutputStream.write(tempBuffer,
                          0,
                          cnt);
              }//end if
          }//end while
          
          // Close the line for new use (if this lines not presents take a LineUnavailableException)
          targetDataLine.stop();
          targetDataLine.close();
          
        }//end run
    }//end inner class CaptureThread
    
    //===================================//
    //Inner class to play back the data
    // that was saved.
    class PlayThread extends Thread{
        byte tempBuffer[] = new byte[10000];

        @Override
        public void run(){
          try{
            int cnt;
            //Keep looping until the input read method
            // returns -1 for empty stream.
            while((cnt = audioInputStream.read(
                            tempBuffer, 0,
                            tempBuffer.length)) != -1){
              if(cnt > 0){
                //Write data to the internal buffer of
                // the data line where it will be
                // delivered to the speaker.
                sourceDataLine.write(tempBuffer,0,cnt);
              }//end if
            }//end while
            //Block and wait for internal buffer of the
            // data line to empty.
            sourceDataLine.drain();
            sourceDataLine.close();
          }catch (IOException e) {
            System.out.println(e);
            System.exit(0);
          }//end catch
        }//end run
    }//end inner class PlayThread

}//end outer class AudioCapture.java