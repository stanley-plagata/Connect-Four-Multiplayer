import java.net.Socket;
import java.net.UnknownHostException;

public class Player {
    private String name;
    private Socket theSocket;
    
    public Player(String name, Socket socket) throws UnknownHostException {
        this.name = name;
        this.setSocket(socket);
    }
    

    /**
     * @return Name of the player
     */
    public String getName() {
        return name;
    }

	public Socket getSocket() {
		return theSocket;
	}

	public void setSocket(Socket theSocket) {
		this.theSocket = theSocket;
	}
}