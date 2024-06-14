import org.w3c.dom.Document;

public class AdresseReseau {
    String adresseR;
    public AdresseReseau(Document doc, String balise) {
        this.adresseR = doc.getElementsByTagName(balise).item(0).getTextContent();
    }

    public String[] getChaineAdresseM() {
        String[] s =  this.adresseR.replace("."," ").split(" ");
        return s;
    }
}
