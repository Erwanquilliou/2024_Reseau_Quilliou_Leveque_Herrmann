import org.w3c.dom.Document;
import java.net.Socket;

public class AdresseMachine {
    String adresseM;
    public AdresseMachine(Document doc, Socket clientSocket) {
        this.adresseM = clientSocket.getInetAddress().getHostAddress();
    }

    public String[] getChaineAdresseM() {
        String[] s =  this.adresseM.replace("."," ").split(" ");
        return s;
    }
}
