import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ServeurWeb {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        int port = 8080; // Port par défaut

        // Vérifier si un port est fourni en argument
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Le port doit être un entier. Utilisation du port par défaut 8080.");
            }
        }
        Document doc = creerDocument("src/config.xml");
        String cheminWeb = doc.getElementsByTagName("root").item(0).getTextContent();


        BufferedReader reader = new BufferedReader(new FileReader( "var/www/index.html"));
        String s1 = reader.readLine();
        String content = "";
        while (s1 != null){
            content += s1 + "\n";
            s1 = reader.readLine();
        }

        String codeDate = ChargerCode.interpreterCode(content);
        System.out.println(codeDate);
        BufferedWriter index = new BufferedWriter(new FileWriter("var/www/index.html"));
        index.write(codeDate);
        index.flush();



        if (doc.getElementsByTagName("port").item(0)!=null && doc.getElementsByTagName("port").item(0).getTextContent()!= ""){
            port = Integer.parseInt(doc.getElementsByTagName("port").item(0).getTextContent());
        }
        BufferedWriter err = new BufferedWriter( new FileWriter(doc.getElementsByTagName("errorlog").item(0).getTextContent()));
        BufferedWriter access = new BufferedWriter(new FileWriter(doc.getElementsByTagName("accesslog").item(0).getTextContent()));

            try (ServerSocket serverSocket = new ServerSocket(port) ){


                System.out.println("Serveur web démarré sur le port " + port);

                while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     OutputStream out = clientSocket.getOutputStream()) {

                    AdresseReseau adrR = new AdresseReseau(doc,"reject");
                    AdresseReseau adrA = new AdresseReseau(doc,"accept");
                    AdresseMachine adrC = new AdresseMachine(clientSocket);

                    if (doc.getElementsByTagName("reject").item(0)!=null && doc.getElementsByTagName("reject").item(0).getTextContent()!= "") {

                        String[] aRC = adrC.getChaineAdresseM();
                        String[] aRF = adrR.getChaineAdresseR();
                        String[] aRA = adrA.getChaineAdresseR();
 
                        // si l'adresse réseau du client correspond à l'adresse réseau refusée
                        if((aRC[0].compareTo(aRF[0]) == 0) && (aRC[1].compareTo(aRF[1]) == 0) && (aRC[2].compareTo(aRF[2]) == 0)){
                            //on coupe la connexion
                            access.write("connexion refusée de " + clientSocket.getInetAddress());
                            access.newLine();
                            access.flush();
                            clientSocket.close();
                            continue;
                        }
                        // si l'adresse réseau du client ne correspond pas non plus à une adresse réseau acceptée
                        else if(!(aRC[0].compareTo(aRA[0]) == 0) && (aRC[1].compareTo(aRA[1]) == 0) && (aRC[2].compareTo(aRA[2]) == 0)){
                            //on coupe aussi la connexion
                            access.write("connexion inconnue refusée de " + clientSocket.getInetAddress());
                            access.newLine();
                            access.flush();
                            clientSocket.close();

                            continue;
                        }
                        //sinon cela signifique que l'adresse réseau est acceptée, et donc que l'on ne coupe pas la connection

                    }
                    access.write("connexion de " + clientSocket.getInetAddress());
                    access.newLine();
                    String inputLine;
                    String requestedFile = null;

                    // Lire la requête du client
                    while ((inputLine = in.readLine()) != null) {
                        String[] sep = inputLine.split(" ");
                        System.out.println(inputLine);
                        if (inputLine.startsWith("GET")) {
                            requestedFile = cheminWeb + sep[1]; // /var/www/index.html
                        }
                        if (inputLine.isEmpty()) {
                            break; // Fin des en-têtes HTTP
                        }
                        if (requestedFile == cheminWeb || requestedFile.equals(cheminWeb +"/")) {
                            requestedFile = "/var/www/index.html";
                        }
                    }
                    BufferedWriter status = new BufferedWriter(new FileWriter("status.log"));
                    Runtime runtime = Runtime.getRuntime();

                    File fileProj = new File("/");

                    Stream<ProcessHandle> allProcesses = ProcessHandle.allProcesses();


                    status.write("Mémoire disponible : "+String.valueOf(runtime.freeMemory()));
                    status.newLine();
                    status.write("Espace disque disponible : " + fileProj.getFreeSpace());
                    status.newLine();
                    status.write("Nombre de processus en cours  : " + allProcesses.count());
                    status.newLine();
                    status.flush();



                    // Enlever le premier caractère '/' du chemin
                    requestedFile = requestedFile.substring(1);

                    // Lire et envoyer le fichier
                    File file = new File(requestedFile);
                    if (file.exists() && !file.isDirectory()) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] fileContent = new byte[(int) file.length()];
                        fileInputStream.read(fileContent);
                        fileInputStream.close();
                        String s = null;
                        if (!requestedFile.endsWith("html")) {
                            s = Base64.getEncoder().encodeToString(fileContent);
                        }


                        String httpResponse = "HTTP/1.1 200 OK\r\n";
                        // Détecter le type de fichier et définir le Content-Type approprié
                        if (requestedFile.endsWith(".html")) {
                            httpResponse += "Content-Type: text/html\r\n";
                        } else if (requestedFile.endsWith(".xml")) {
                            httpResponse += "Content-Type: application/xml\r\n\r\n";
                        }else if(requestedFile.endsWith(".jpg")){
                            httpResponse += "Content-Type: image/jpg\r\n";
                        }
                        else {
                            httpResponse += "Content-Type: image/png\r\n";
                        }

                        if (!requestedFile.endsWith(".html")) {
                            String base64Content = Base64.getEncoder().encodeToString(fileContent);
                            System.out.println(base64Content);
                            httpResponse += "Content-Encoding: base64\r\n";
                            httpResponse += "Content-Length: " + base64Content.length() + "\r\n";
                            httpResponse += "\r\n" + base64Content;
                            out.write(httpResponse.getBytes("UTF-8"));
                        } else {
                            httpResponse += "Content-Length: " + fileContent.length + "\r\n";
                            httpResponse += "\r\n";
                            System.out.println(httpResponse);
                            out.write(httpResponse.getBytes("UTF-8"));
                            out.write(fileContent);
                        }
                    } else {
                        String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\nFile Not Found ";
                        out.write(httpResponse.getBytes("UTF-8"));
                    }

                    access.flush();

                } catch (Exception e) {
                    err.write("l'erreur suivante est survenue : " + e.getMessage());
                    err.newLine();
                }
                err.flush();
            }

        } catch (Exception e) {
            err.write("l'erreur suivante est survenue : " + e.getMessage());
            err.newLine();
        }
    }

    public static Document creerDocument(String s) throws ParserConfigurationException, IOException, SAXException {
        File inputFile = new File(s);

        // Créer une fabrique de constructeurs de documents
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        // Créer un constructeur de documents
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Analyser le fichier XML et obtenir un objet Document
        Document doc;
        doc = dBuilder.parse(inputFile);

        // Normaliser le document XML
        doc.getDocumentElement().normalize();
        return doc;
    }

    private static boolean isBinaryFile(String fileName) {
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                fileName.endsWith(".gif") || fileName.endsWith(".mp3") || fileName.endsWith(".wav") ||
                fileName.endsWith(".mp4");
    }


}