package com.digitalpersona.onetouch.sampleapp;

import com.digitalpersona.onetouch.*;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.DPFPCapturePriority;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPDataListener;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.readers.DPFPReaderDescription;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

//import libreria de json
import java.io.FileWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonReader;

import java.sql.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.OutputStream;




/**
 * Implementation of UserInterface.Factory interface that creates a console-based user interface
 */
public class ConsoleUserInterfaceFactory implements UserInterface.Factory {

    /**
     * Creates an object implementing UserInterface interface
     *
     * @param userDatabase user database to be used with the ui
     * @return created instance
     */
    public UserInterface createUI(UserDatabase userDatabase) {
        return new ConsoleUserInterface(userDatabase);
    }

    /**
     * Console-based UserInterface
     */
    private static class ConsoleUserInterface
            implements UserInterface
    {

        /**
         * keeps a user database
         */
        private UserDatabase userDatabase;

        /**
         * Constructs an instance
         *
         * @param userDatabase user database to be used with the ui
         */
        public ConsoleUserInterface(UserDatabase userDatabase) {
            this.userDatabase = userDatabase;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
            System.out.println("\n*** Registro de huellas ***"); //sks
            String activeReader = null;
            boolean readerSelected = true;//Activa la tarjeta de huella automaticamente sks
            addUser();//para cargar el usuario automaticamente sks
            register(activeReader);//para cargar la huella automaticamente sks
            int res;

            while ((res = MenuShow(mainMenu, MENU_WITH_EXIT)) != exitItem.getValue()) try {
                switch (res) {
                    /*case MAIN_MENU_ADD:
                        addUser();
                        break;*/ /////// Se comenta para cargar el usuario en la base datos automaticamente     sks

                    /*case MAIN_MENU_ENROLL:
                        if (readerSelected)
                            register(activeReader);
                        else
                            System.out.println("No reader selected");
                        break;
                      */

                    case MAIN_MENU_VERIFY:
                        if (readerSelected)
                            verify(activeReader);
                        else
                            System.out.println("No reader selected");
                        break;
                    case MAIN_MENU_ENUMERATE:
                        listReaders();
                        break;
                    case MAIN_MENU_SELECT:
                        try {
                            activeReader = selectReader(activeReader);
                            readerSelected = true;
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("El lector de huella no disponible");//sks
                        }
                        break;
                }
            } catch (Exception e) { }
        }

        /**
         * Console menu item
         */
        private static class MenuItem
        {
            private String text;
            private int value;

            /**
             * Creates a menu item
             *
             * @param text  item text
             * @param value value to return if item is chosen
             */
            public MenuItem(String text, int value) {
                this.text = text;
                this.value = value;
            }

            /**
             * Returns the menu item's text
             *
             * @return menu item text
             */
            public String getText() {
                return text;
            }

            /**
             * Returns the menu item's associated value
             *
             * @return associated value
             */
            public int getValue() {
                return value;
            }
        }

        /**
         * Specifies that menu should be appended with "Back" item
         */
        private static final int MENU_WITH_BACK = 2;

        /**
         * Specifies that menu should be appended with "Exit" item
         */
        private static final int MENU_WITH_EXIT = 1;

        /**
         * "Exit" menu item
         */
        private static final MenuItem exitItem = new MenuItem("Salir de la applicacion", -1);

        /**
         * "Back" menu item
         */
        private static final MenuItem backItem = new MenuItem("Regresar al menu previo", -2);

        private static final int MAIN_MENU_ENUMERATE = 101;
        private static final int MAIN_MENU_SELECT = 102;
        private static final int MAIN_MENU_ADD = 103;
        private static final int MAIN_MENU_ENROLL = 104;
        private static final int MAIN_MENU_VERIFY = 105;

        private static final Vector<MenuItem> mainMenu;
        static {
            mainMenu = new Vector<MenuItem>();
            // mainMenu.add(new MenuItem("Lista de tarjetas disponible", MAIN_MENU_ENUMERATE));
            //mainMenu.add(new MenuItem("Seleccione una tarjeta", MAIN_MENU_SELECT));
            //mainMenu.add(new MenuItem("Agragar una persona a la db ", MAIN_MENU_ADD));

            //mainMenu.add(new MenuItem("Registrar huella (enrollment)", MAIN_MENU_ENROLL));
            //mainMenu.add(new MenuItem("Verificacion", MAIN_MENU_VERIFY));
        }

        private static final EnumMap<DPFPFingerIndex, String> fingerNames;
        static {
            fingerNames = new EnumMap<DPFPFingerIndex, String>(DPFPFingerIndex.class);
            //fingerNames.put(DPFPFingerIndex.RIGHT_INDEX,  "Indice derecho");
            //fingerNames.put(DPFPFingerIndex.LEFT_PINKY,	  "Menique izquierdo");
            /*fingerNames.put(DPFPFingerIndex.LEFT_RING,    "Anillo izquierdo");
            fingerNames.put(DPFPFingerIndex.LEFT_MIDDLE,  "Izquierdo medio");
            fingerNames.put(DPFPFingerIndex.LEFT_INDEX,   "Indice izquierdo");
            fingerNames.put(DPFPFingerIndex.LEFT_THUMB,   "Pulgar izquierdo");
            fingerNames.put(DPFPFingerIndex.RIGHT_PINKY,  "Menique derecho");
            fingerNames.put(DPFPFingerIndex.RIGHT_RING,   "Anillo derecho");
            fingerNames.put(DPFPFingerIndex.RIGHT_MIDDLE, "Medio derecho");
            fingerNames.put(DPFPFingerIndex.RIGHT_INDEX,  "Indice derecho");
            fingerNames.put(DPFPFingerIndex.RIGHT_THUMB,  "Pulgar derecho");
*/
        }

        private int MenuShow(Vector<MenuItem> menu, int nMenuFlags) {
            int choice = 0;

            if (menu == null)
                return choice;

            while (true) {
                System.out.println();
                for (int i = 0; i < menu.size(); ++i)
                    System.out.printf("%d: %s\n", i + 1, menu.elementAt(i).getText());

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Elige una opccion (1 - %d", menu.size()));
                if ((nMenuFlags & MENU_WITH_BACK) != 0) {
                    sb.append(", R");
                }
                if ((nMenuFlags & MENU_WITH_EXIT) != 0) {
                    sb.append(", E");
                }
                sb.append("): ");

                String userInput ="E";// ShowDialog(sb.toString());



                if ((nMenuFlags & MENU_WITH_EXIT) != 0 && userInput.equalsIgnoreCase("E")) {
                    choice = exitItem.getValue();
                    break;
                }
                if ((nMenuFlags & MENU_WITH_BACK) != 0 && userInput.equalsIgnoreCase("R")) {
                    choice = backItem.getValue();
                    break;
                }

                int nInput;

                try {
                    nInput = Integer.parseInt(userInput);
                } catch (NumberFormatException e) {
                    System.out.printf("\nInvalida entrada(input): \"%s\"\n", userInput);
                    continue;
                }

                if (nInput < 1 || nInput > menu.size()) {
                    System.out.printf("\nIncorrecta opcion: \"%s\"\n", userInput);
                    continue;
                }

                choice = menu.elementAt(nInput - 1).getValue();
                break;
            }
            System.out.println();

            //System.out.println(choice+"<<<<<< opccion elegioda\n");
            return choice;
        }

        /**
         * Adds user to the database
         */
        private void addUser() {

            //System.out.printf("Adding person to the database...\n");
            String username = "sks";//ShowDialog("Enter a name: ");
            if (userDatabase.addUser(username) != null) {
                assert true;
            } else {
                assert true;

            }

        }


        private void jsonStatus( ) {
            //https://www.mkyong.com/java/gson-streaming-to-read-and-write-json/
            Gson var1 = (new GsonBuilder()).create();
            JsonWriter writer;
            try {
                writer = new JsonWriter(new FileWriter("T.json"));
                writer.beginObject();
                writer.name("status").value(1);
                writer.name("msg").value("Captura de huella exitosa!"); // "messages" :
                writer.endObject();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private void jsonStatusF() {
        //https://www.mkyong.com/java/gson-streaming-to-read-and-write-json/
        Gson var1 = (new GsonBuilder()).create();
        JsonWriter writer;
        try {
            writer = new JsonWriter(new FileWriter("E.json"));
            writer.beginObject();
            writer.name("status").value(0);
            writer.name("msg").value("Fallo a la captura de huella (ERROR) !!!"); // "messages" :
            writer.endObject();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }













 
    
   
  

 

 







        /**
         * register() looks for the user in the database, invokes CreateRegistrationTemplate(),
         * and stores the template in the database.
         *
         * @param activeReader reader to use for fingerprint acquisition
         */
        private void register(String activeReader) {
            System.out.printf("Realizar la inscripcion de huellas dactilares... \n");
            String username ="sks";// ShowDialog("Enter the name of the person to be enrolled: ");//sks
            UserDatabase.User user = userDatabase.getUser(username);

            if (user == null) {
                assert true;
            } else {
                StringBuilder sb = new StringBuilder();
                for (DPFPFingerIndex finger : DPFPFingerIndex.values()) {

                    if (user.getTemplate(finger) != null)
                        sb.append("    ").append(fingerName(finger)).append("");
                }
                String fingers = sb.toString();

                if (fingers.length() > 0) {
                    assert true;
                } else {
                    assert true;
                }

                Vector<MenuItem> menu = new Vector<MenuItem>();
                for (DPFPFingerIndex finger : DPFPFingerIndex.values())
                    menu.add(new MenuItem(fingerName(finger), finger.ordinal()));
                int nFinger =0;// MenuShow(menu, MENU_WITH_BACK);
                //System.out.printf(nFinger+" valor del nfinger 1 \n");
                if (nFinger == backItem.getValue()) {
                    System.out.printf("Enrollment canceled.\n");
                } else {
                    try {
                   ///     System.out.printf(nFinger+"valor del nfinger 2 \n");
                        DPFPFingerIndex finger = DPFPFingerIndex.values()[nFinger];
                        DPFPFeatureExtraction featureExtractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
                        DPFPEnrollment enrollment = DPFPGlobal.getEnrollmentFactory().createEnrollment();
                        while (enrollment.getFeaturesNeeded() > 0)
                        {
                            DPFPSample sample = getSample(activeReader,
                                    String.format("Capturando huella (%d restante)...\n", enrollment.getFeaturesNeeded()));
                            if (sample == null)
                                continue;
                            DPFPFeatureSet featureSet;

                            try {
                                featureSet = featureExtractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);

                            } catch (DPFPImageQualityException e) {
                                System.out.printf("Mala calidad de imagen: \"%s\".Intentalo de nuevo. \n", e.getCaptureFeedback().toString());
                                continue;
                            }

                            enrollment.addFeatures(featureSet);
                        }

                        DPFPTemplate template = enrollment.getTemplate();

                        //----------------------------//----------------------------//----------------------------
                        //                                      Se crea el archivo
                        //----------------------------//----------------------------//----------------------------
                        //                                         Conexion MYSQL
                        //----------------------------//----------------------------//----------------------------
                        try {
                            JsonReader reader = new JsonReader(new FileReader("verificar.json"));
                            reader.beginObject();
                            while (reader.hasNext()) {
                                String name = reader.nextName();
                                if (name.equals("id")) {
                                    int id=reader.nextInt();
                                        try{
                                     
                                            Class.forName("com.mysql.jdbc.Driver");
                                            //driver --> //https://dev.mysql.com/downloads/file/?id=480091
                                            Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/cocai","root","");
                                            String SQL_SERIALIZE_OBJECT = "update usuario set huella = ? where id = 1";
                                            PreparedStatement pstmt = conn.prepareStatement(SQL_SERIALIZE_OBJECT);
                                            pstmt.setObject(1,enrollment.getTemplate().serialize());
                                            pstmt.executeUpdate();
                                            pstmt.close();
                                            conn.close();                                   
                                
                                                /* try {
                                                        FileOutputStream test = new FileOutputStream("borrar.fpt");
                                                            System.out.printf(enrollment.getTemplate().toString());
                                                            test.write(enrollment.getTemplate().serialize());//setHuella
                                                            test.close();
                                                        }catch (Exception ex){
                                                                    System.out.printf("\n Error al crear archivo"+ex.getLocalizedMessage());
                                                        }
                                                        Gson var1 = (new GsonBuilder()).create();
                                                        JsonWriter writer;*/
                                        }catch(Exception e){System.out.println(e);
                                        
                                        }
                                              System.out.printf("\nUsuario Id: ["+id+"] inscrito correctamente \n");
                                    } else {
                                    reader.skipValue();
                                }
                            }
                            reader.endObject();
                            reader.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();}
                        //----------------------------//----------------------------//----------------------------


                        user.setTemplate(finger, template);
                        System.out.printf("La huella se a inscrito correctamente (enrolled) ");

                        //jsonStatus();


                    } catch (DPFPImageQualityException e) {
                        System.out.printf("Fallo al  captura de huella (Error) .\n");
                        jsonStatusF();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        /**
         * Acquires fingerprint from the sensor and matches it with the registration templates
         * stored in the database.
         *
         * @param activeReader fingerprint reader to use
         */
        private void verify(String activeReader) {
            System.out.printf("Performing fingerprint verification...\n");
            String username = "sks";//ShowDialog("Enter the name of the person to be verified: ");
            UserDatabase.User user = userDatabase.getUser(username);
            

            if (user == null) {
                System.out.printf("\"%s\" was not found in the database.\n", username);
            } else {
                if (user.isEmpty()) {
                    System.out.printf("No fingers for \"%s\" have been enrolled.\n", username);
                } else {
                    try {
                        DPFPSample sample = getSample(activeReader, "Incerte dedo en el dispositivo\n");
                        if (sample == null)
                            throw new Exception();

                        DPFPFeatureExtraction featureExtractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
                        DPFPFeatureSet featureSet = featureExtractor.createFeatureSet(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
                        DPFPVerification matcher = DPFPGlobal.getVerificationFactory().createVerification();
                        matcher.setFARRequested(DPFPVerification.MEDIUM_SECURITY_FAR);
                       
                        for (DPFPFingerIndex finger : DPFPFingerIndex.values()) {
                            JsonReader reader = new JsonReader(new FileReader("verificar.json"));
                            reader.beginObject();
                            reader.hasNext();
                            String name = reader.nextName();
                            name.equals("id");
                            int id=reader.nextInt();                                            
                            
                            Class.forName("com.mysql.jdbc.Driver");
                            //driver --> //https://dev.mysql.com/downloads/file/?id=480091
                            ResultSet rs = null;
                            Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/cocai","root","");
                            PreparedStatement pstmt = null;
                            String query = "SELECT huella FROM usuario WHERE id = ?";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setInt(1, id);
                            rs = pstmt.executeQuery();
                            rs.next();
                            Blob templateF = rs.getBlob("huella");
                            byte[] templateBuffer= rs.getBytes("huella");

                            
                            Blob test=rs.getBlob("huella");
                            InputStream x=test.getBinaryStream();
                            int size=x.available();
                            OutputStream out=new FileOutputStream("verificar.fpt");
                            byte b[]= new byte[size];
                            x.read(b);
                            out.write(b);
                                                                    
                            FileInputStream stream = new FileInputStream("verificar.fpt");
                            byte[] data = new byte[stream.available()];
                            stream.read(data);
                            stream.close();
                            DPFPTemplate templater = DPFPGlobal.getTemplateFactory().createTemplate();
                            templater.deserialize(data);

                            rs.close();
                            pstmt.close();
                            conn.close();
                                
                            reader.endObject();
                            reader.close();
                           
                            //----------------------------//----------------------------//----------------------------
                
                           
                           
                           
                            if (templater != null) {
                                DPFPVerificationResult result = matcher.verify(featureSet, templater);
                                
                                if (result.isVerified()) {
                                    
                         
        
        
                                    System.out.printf("Verificacion exitosa!!! \n PROBABILITY_ONE far achieved:  %g.\n",
                                             (double)result.getFalseAcceptRate()/DPFPVerification.PROBABILITY_ONE);
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.printf("Falla en la verification.");
                    }
                    System.out.printf("Error en la verificacion\n");
                }
            }
        }

        /**
         * Prints information on all available readers
         */
        private void listReaders() {

            DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();

            if (readers == null || readers.size() == 0) {
                System.out.printf("El lector de huella no disponible.\n");
                return;
            }

            System.out.printf("Available readers:");
            for (DPFPReaderDescription readerDescription : readers)
                System.out.println(readerDescription.getSerialNumber());
        }

        /**
         * selectReader() stores chosen reader in *pActiveReader
         * @param activeReader currently selected reader
         * @return reader selected
         * @throws IndexOutOfBoundsException if no reader available
         */
            String selectReader(String activeReader) throws IndexOutOfBoundsException {
            DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();

            if (readers == null || readers.size() == 0)
                throw new IndexOutOfBoundsException("El lector de huella no disponible");
            Vector<MenuItem> menu = new Vector<MenuItem>();

            for (DPFPReaderDescription readerDescription : readers)
                menu.add(new MenuItem(readerDescription.getSerialNumber(), menu.size()));
            menu.add(new MenuItem("Any available readers", menu.size()));

            int res = 0;// MenuShow(menu, MENU_WITH_BACK);monta la terjeta automaticamente

            if (res == backItem.getValue()) {
                return activeReader;
            } else if (res == readers.size()) {

                return null;
            } else {

                return readers.get(res).getSerialNumber();
            }
        }

        /**
         * Acquires a fingerprint sample from the specified fingerprint reader
         *
         * @param activeReader Fingerprint reader to use for acquisition
         * @return sample acquired
         * @throws InterruptedException if thread is interrupted
         */
        private DPFPSample getSample(String activeReader, String prompt)
                throws InterruptedException
        {
            final LinkedBlockingQueue<DPFPSample> samples = new LinkedBlockingQueue<DPFPSample>();
            DPFPCapture capture = DPFPGlobal.getCaptureFactory().createCapture();
            capture.setReaderSerialNumber(activeReader);
            capture.setPriority(DPFPCapturePriority.CAPTURE_PRIORITY_LOW);
            capture.addDataListener(new DPFPDataListener()
            {
                public void dataAcquired(DPFPDataEvent e) {
                    if (e != null && e.getSample() != null) {
                        try {
                            samples.put(e.getSample());
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
            capture.addReaderStatusListener(new DPFPReaderStatusAdapter()
            {
                int lastStatus = DPFPReaderStatusEvent.READER_CONNECTED;
                public void readerConnected(DPFPReaderStatusEvent e) {
                    if (lastStatus != e.getReaderStatus())
                        System.out.println("Reader is connected");
                    lastStatus = e.getReaderStatus();
                }
                public void readerDisconnected(DPFPReaderStatusEvent e) {
                    
                    if (lastStatus != e.getReaderStatus())
                        System.out.println("Reader is disconnected");
                    lastStatus = e.getReaderStatus();
                }

            });
            try {
                capture.startCapture();
                System.out.print(prompt);
                return samples.take();
            } catch (RuntimeException e) {
                System.out.printf("Failed to start capture. Check that reader is not used by another application.\n");
                throw e;
            } finally {
                capture.stopCapture();
            }
        }

        private String ShowDialog(String prompt) {
            System.out.printf(prompt);

            try {
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                return stdin.readLine();
            } catch (IOException e) {
                return "";
            }
        }

        private String fingerName(DPFPFingerIndex finger) {
            return fingerNames.get(finger);
        }
        private String fingerprintName(DPFPFingerIndex finger) {
            return fingerNames.get(finger) + " fingerprint";
        }
    }
}
