import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class Main {
    public static String interpreterCode(String docHTML) {
        int i = 0;
        boolean stop = false;
        while ((docHTML.indexOf("<code interpreteur=") != -1)&&(!stop)) {
            int debut = docHTML.indexOf("<code interpreteur=");
            int fin = docHTML.indexOf("</code>");
            String recupere = docHTML.substring(debut, fin);
            String[] tabCode = recupere.split("=");
            tabCode = tabCode[1].split(">");
            String chemin = tabCode[0];
            String code = tabCode[1];
            //appeler la methode grace au chemin
            //garder le resultat dans une variable resultat
            String resultat = "booh";
            //remplacer tout le bazar par le resultat
            docHTML = docHTML.substring(0,debut) + resultat + docHTML.substring(fin+7, docHTML.indexOf("</html>")+7);

        }
        return docHTML;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        String a = "<html>\n" +
                "<body>\n" +
                "<h1> Exemple avec la date </h1>\n" +
                "<h2>en bash</h2>\n" +
                "La date est <code interpreteur=«/bin/bash»>date</code>\n" +
                "<h2>En python</h2>\n" +
                "La date est <code interpreteur=«/usr/bin/python»>\n" +
                "    import time;\n" +
                "    print(time.time())\n" +
                "</code>\n" +
                "</body>\n" +
                "</html>";
        System.out.println(interpreterCode(a));
    }
}
