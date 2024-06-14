import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChargerCode {
    /**
     * methode qui permet de modifier les <code></code> d'une page html
     * @param docHTML document sous forme de chaine de caracteres a inspecter
     * @return doucment sous forme de chaine de caracteres modifie
     * @throws IOException
     */
    public static String interpreterCode(String docHTML) throws IOException {

        //tant qu'il existe une ligne avec "<code interpreteur="
        while (docHTML.indexOf("<code interpreteur=") != -1) {

            //on determine le bloc de code a prendre
            int debut = docHTML.indexOf("<code interpreteur=");
            int fin = docHTML.indexOf("</code>");

            //on le recupere
            String recupere = docHTML.substring(debut, fin);

            //on separe le bloc en plusieurs parties afin de ne garder que ce qui nous interesse (le chemin et le code)
            String[] tabCode = recupere.split("=");
            tabCode = tabCode[1].split(">");
            //on recupere le chemin
            String chemin = tabCode[0];
            chemin = chemin.substring(1,chemin.length()-1);
            //on recupere le code
            String code = tabCode[1];

            //on appelle le code en utilisant le chemin
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(chemin,"-c", code);
            Process process = processBuilder.start();
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getInputStream()));

            //on garde le resultat renvoye par la/les methodes appelle(es)
            String resultat = reader2.readLine();

            //on remplace la balise "<code></code>" par le resultat obtenu
            docHTML = docHTML.substring(0,debut) + resultat + docHTML.substring(fin+7, docHTML.indexOf("</html>")+7);
            //les +7 servent reciproquement a supprimer la balise </code> et garder la balise </html>
            //(car ils font une taille de 7 caracteres)

        }
        //on renvoie le document html modifie
        return docHTML;
    }
}
