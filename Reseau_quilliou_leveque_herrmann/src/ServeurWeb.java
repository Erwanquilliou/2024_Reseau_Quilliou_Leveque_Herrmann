import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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

        File inputFile = new File("src/config.xml");

        // Créer une fabrique de constructeurs de documents
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        // Créer un constructeur de documents
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        // Analyser le fichier XML et obtenir un objet Document
        Document doc = dBuilder.parse(inputFile);

        // Normaliser le document XML
        doc.getDocumentElement().normalize();
        System.out.println(doc.getElementsByTagName("port").item(0).getTextContent());
        if (doc.getElementsByTagName("port").item(0)!=null && doc.getElementsByTagName("port").item(0).getTextContent()!= ""){
            port = Integer.parseInt(doc.getElementsByTagName("port").item(0).getTextContent());
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur web démarré sur le port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     OutputStream out = clientSocket.getOutputStream()) {
                    if (doc.getElementsByTagName("reject").item(0)!=null && doc.getElementsByTagName("reject").item(0).getTextContent()!= "") {
                        if(clientSocket.getInetAddress().getHostAddress().equals(doc.getElementsByTagName("reject").item(0).getTextContent())){
                            clientSocket.close();
                            continue;
                        }
                    }
                    System.out.println("Connexion acceptée de " + clientSocket.getInetAddress());

                    String inputLine;
                    String requestedFile = null;

                    // Lire la requête du client
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println(inputLine);
                        if (inputLine.startsWith("GET")) {
                            requestedFile = inputLine.split(" ")[1];
                        }
                        if (inputLine.isEmpty()) {
                            break; // Fin des en-têtes HTTP
                        }
                    }

                    // Si aucun fichier n'est spécifié, on sert index.html par défaut
                    if (requestedFile == null || requestedFile.equals("/")) {
                        requestedFile = "/index.html";
                    }

                    // Enlever le premier caractère '/' du chemin
                    requestedFile = requestedFile.substring(1);

                    // Lire et envoyer le fichier
                    File file = new File(requestedFile);
                    if (file.exists() && !file.isDirectory()) {
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] fileContent = new byte[(int) file.length()];
                        fileInputStream.read(fileContent);
                        fileInputStream.close();

                        String httpResponse = "HTTP/1.1 200 OK\r\n";
                        // Détecter le type de fichier et définir le Content-Type approprié
                        if (requestedFile.endsWith(".html")) {
                            httpResponse += "Content-Type: text/html\r\n\r\n";
                        } else if (requestedFile.endsWith(".xml")) {
                            httpResponse += "Content-Type: application/xml\r\n\r\n";
                        } else {
                            httpResponse += "\r\n";
                        }

                        out.write(httpResponse.getBytes("UTF-8"));
                        out.write(fileContent);
                    } else {
                        String httpResponse = "HTTP/1.1 404 Not Found\r\n\r\nFile Not Found";
                        out.write(httpResponse.getBytes("UTF-8"));
                    }

                } catch (Exception e) {
                    System.err.println("Erreur lors de la communication avec le client : " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage du serveur sur le port " + port + " : " + e.getMessage());
        }
    }
}