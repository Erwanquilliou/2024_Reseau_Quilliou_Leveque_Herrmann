import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static String interpreterCode(String docHTML) throws IOException {
        int i = 0;
        boolean stop = false;
        while ((docHTML.indexOf("<code interpreteur=") != -1)&&(!stop)) {
            int debut = docHTML.indexOf("<code interpreteur=");
            int fin = docHTML.indexOf("</code>");
            String recupere = docHTML.substring(debut, fin);
            String[] tabCode = recupere.split("=");
            tabCode = tabCode[1].split(">");
            String chemin = tabCode[0];
            chemin = chemin.substring(1,chemin.length()-1);
            String code = tabCode[1];
            //appeler la methode grace au chemin
            //garder le resultat dans une variable resultat
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(chemin,"-c", code);
            Process process = processBuilder.start();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader2.readLine();

            String resultat = line;
            //remplacer tout le bazar par le resultat
            docHTML = docHTML.substring(0,debut) + resultat + docHTML.substring(fin+7, docHTML.indexOf("</html>")+7);

        }
        return docHTML;
    }


}
