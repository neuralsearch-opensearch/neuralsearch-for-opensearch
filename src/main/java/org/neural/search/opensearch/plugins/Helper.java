package org.neural.search.opensearch.plugins;

import java.security.AccessController;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import java.util.ArrayList;
import java.util.List;

import org.neural.search.opensearch.plugins.services.NeuralAnswer;
import org.neural.search.opensearch.plugins.services.NeuralServices;
import org.neural.search.opensearch.plugins.services.SentenceScore;
import org.opensearch.SpecialPermission;
import org.opensearch.common.settings.Settings;

public class Helper {

    public static String msg;

    public static List<String> toChunks(String text, int chunkSize)
    {
        List<String> chunks = new ArrayList<String>();
        String[] sents = text.split("(\\. |\\r\\n|\\n)|\n");
        String current = "";
        int tokenSize = 0;
        for(String s:sents)
        {
            if(s.trim().length()>0)
            {
                current = current+" "+s+".";
                tokenSize+=s.split(" ").length;
                if(tokenSize>=chunkSize)
                {
                    chunks.add(current.trim());
                    current = "";
                    tokenSize = 0;
                }
            }
            
            
        }

        return chunks;
    }

    public static float[] getEmbedding(String text, String modelName) throws Exception 
    {
        Settings neuralSettings = NeuralSearchSettings.state().getClient().settings();
        String apiUrl = neuralSettings.get(NeuralSearchSettings.NEURAL_SERVICES_URL);

        float[] result = null;
        result = ClientSecurityManager.doPrivilegedException(() -> {
                return NeuralServices.getEmbedding(apiUrl, text, modelName);
            });

        return result;
    }

    public static NeuralAnswer answer(String question, String context, String modelName)
    {
        Settings neuralSettings = NeuralSearchSettings.state().getClient().settings();
        String apiUrl = neuralSettings.get(NeuralSearchSettings.NEURAL_SERVICES_URL);

        NeuralAnswer result = null;
        try {
            result = ClientSecurityManager.doPrivilegedException(()->{return NeuralServices.answer(apiUrl, question, context, modelName);});
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public static List<SentenceScore> scoreSentences(String text, String context, String modelName)
    {
        Settings neuralSettings = NeuralSearchSettings.state().getClient().settings();
        String apiUrl = neuralSettings.get(NeuralSearchSettings.NEURAL_SERVICES_URL);

        List<SentenceScore> result = null;
        try {
            result = ClientSecurityManager.doPrivilegedException(()->{return NeuralServices.scoreSentences(apiUrl, text, context, modelName);});
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    /*public static List getClassesForPackage_(String pckgname) throws ClassNotFoundException {
        // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
        ArrayList<File> directories = new ArrayList<File>();
        String packageToPath = pckgname.replace('.', '/');
        try {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }
  
            // Ask for all resources for the packageToPath
            Enumeration<URL> resources = cld.getResources(packageToPath);
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath(), "UTF-8")));
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        } catch (UnsupportedEncodingException encex) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        } catch (IOException ioex) {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }
  
        ArrayList classes = new ArrayList();
        // For every directoryFile identified capture all the .class files
        while (!directories.isEmpty()){
            File directoryFile  = directories.remove(0);             
            if (directoryFile.exists()) {
                // Get the list of the files contained in the package
                File[] files = directoryFile.listFiles();
  
                for (File file : files) {
                    // we are only interested in .class files
                    if ((file.getName().endsWith(".class")) && (!file.getName().contains("$"))) {
                        // removes the .class extension
                        int index = directoryFile.getPath().indexOf(packageToPath);
                        String packagePrefix = directoryFile.getPath().substring(index).replace('/', '.');;                          
                      try {                  
                        String className = packagePrefix + '.' + file.getName().substring(0, file.getName().length() - 6);                            
                        classes.add(Class.forName(className));                                
                      } catch (NoClassDefFoundError e)
                      {
                        // do nothing. this class hasn't been found by the loader, and we don't care.
                      }
                    } else if (file.isDirectory()){ // If we got to a subdirectory
                        directories.add(new File(file.getPath()));                          
                    }
                }
            } else {
                throw new ClassNotFoundException(pckgname + " (" + directoryFile.getPath() + ") does not appear to be a valid package");
            }
        }
        return classes;
    }*/  



    static class ClientSecurityManager {
        public static final SpecialPermission INSTANCE = new SpecialPermission();
    
        public static <T> T doPrivilegedException(PrivilegedExceptionAction<T> operation) throws Exception {
            check();
            try {
                return AccessController.doPrivileged(operation);
            } catch (PrivilegedActionException e) {
                throw (Exception) e.getCause();
            }
        }
    
        // Stolen from the SpecialPermission class (ES v6.x)
        public static void check() {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(INSTANCE);
            }
        }

    }
}
