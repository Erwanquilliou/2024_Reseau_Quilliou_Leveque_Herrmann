import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Un Serveur Http
 */
public class HttpServer {

    /**
     * La méthode principale.
     * @param args les arguments en ligne de commande
     */
    public static void main(String[] args) {
        // Port par défaut, 80
        int serverPort = 80;

        // Vérifie si un port est donné dans la ligne de commande
        if (args.length == 1) {
            try {
                serverPort = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("Numéro de port invalide");
            }
        } else {
            System.out.println("Numéro de port non fourni");
        }

        // Lit le fichier de configuration
        Properties properties = new Properties();
        String iniFilePath = "ressources/myweb.ini";
        File iniFile = new File(iniFilePath);
        if (iniFile.exists() && !iniFile.isDirectory()) {
            try {
                properties.load(new FileInputStream(iniFilePath));
                String configFilePath = properties.getProperty("cfgfile");
                System.out.println("Fichier de configuration : " + configFilePath);

                // Extraction du port depuis fichier de configuration
                String configContent = new String(Files.readAllBytes(Paths.get(configFilePath)));
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(configContent)));
                XPath xpath = XPathFactory.newInstance().newXPath();
                String portString = (String) xpath.evaluate("/myweb/port", doc, XPathConstants.STRING);
                serverPort = Integer.parseInt(portString);
            } catch (Exception e) {
                System.out.println("Erreur lors de la lecture du fichier de configuration, utilisation du port par défaut : 80");

            }
        } else {
            System.out.println("Le fichier " + iniFilePath + " n'existe pas. Utilisation du port par défaut : 80");

        }

        // Démarre le serveur
        System.out.println("Démarrage du serveur sur le port " + serverPort);
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (Exception e) {
                    System.out.println("Erreur lors de la gestion de la connexion : " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur du serveur : " + e.getMessage());
        }
    }

    /**
     * Gère la connexion client.
     * @param clientSocket le socket client
     */
    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);

            // Lit la première ligne de la requête
            String requestLine = in.readLine();

            // Extrait l'URL de la requête
            String filePath = getFilePath(requestLine);

            // Lit le contenu du fichier
            byte[] content = Files.readAllBytes(Paths.get(filePath));

            // Détermine le type de contenu selon l'extension du fichier
            String contentType = getContentType(filePath);

            // Envoie la réponse au client
            printWriter.println("HTTP/1.1 200 OK");
            printWriter.println("Content-Type: " + contentType);
            printWriter.println("Connection: close");
            printWriter.println();
            outputStream.write(content);
            outputStream.flush();

            System.out.println(filePath + " a été demandé par le client " + clientSocket.getInetAddress());
        } catch (Exception e) {
            System.out.println("Erreur lors de la gestion de la connexion client : " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.out.println("Erreur lors de la fermeture du socket client : " + e.getMessage());
            }
        }
    }

    private static String getFilePath(String requestLine) {
        String url = requestLine.split(" ")[1];

        if (url.equals("/")) {
            url = "/index.html";
        }

        // Localisation du fichier associé à l'URL
        String filePathBase = "ressources/html";
        String filePath = filePathBase + url;
        File file = new File(filePath);

        // Si le fichier n'existe pas, renvoie une erreur 404
        if (!file.exists()) {
            filePath = filePathBase + "/404.html";
        }
        return filePath;
    }

    /**
     * Renvoie le type de contenu en fonction de l'extension du fichier.
     * @param filePath le chemin d'accès au fichier
     * @return le type de contenu
     */
    private static String getContentType(String filePath) {
        String extension = "";

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i+1);
        }

        return switch (extension) {
            case "html", "htm" -> "text/html";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "png" -> "image/png";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            default -> "application/octet-stream";
        };
    }
}
