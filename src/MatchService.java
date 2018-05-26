import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A thread object which the server pull 2 clients into the connect4 game.
 * 
 * @author B
 *
 */
public class MatchService implements Runnable {
	private Player p1;
	private Player p2;
	private ObjectOutputStream out1;
	private ObjectOutputStream out2;
	private ObjectInputStream in1;
	private ObjectInputStream in2;
	private LobbyGUI theLobby;

	public MatchService(Player player1, Player player2, ObjectOutputStream output1, ObjectOutputStream output2,
			ObjectInputStream input1, ObjectInputStream input2, LobbyGUI lobbyGUI) {
		p1 = player1;
		p2 = player2;
		out1 = output1;
		out2 = output2;
		in1 = input1;
		in2 = input2;
		theLobby = lobbyGUI;
	}

	@Override
	public void run() {
		boolean cont = true;
		try {
			System.out.println("Sending signal to start game ...");
			out1.writeObject("Startgame");
			out2.writeObject("Startgame");
			System.out.println("Signal sent");
			System.out.println("Sending player information ...");
			out1.writeObject("1");
			out2.writeObject("2");
			System.out.println("Information sent");
			String int1 = "";
			String int2 = "";

			int winningPlayer = 0;
			do {
				/* transimit the update signal from p1 to p2 */
				if (!int1.equals("***CLOSE***")) {
					int1 = (String) in1.readObject();
					System.out.println("1>2: " + int1);
					if (!int1.equals("***CLOSE***")) {
						out2.writeObject(int1);
					} else {
						out1.writeObject(int1);
						winningPlayer = (int) in1.readObject();
					}
				}

				// transimit the update signal from p2 to p1
				if (!int2.equals("***CLOSE***")) {
					int2 = (String) in2.readObject();
					System.out.println("2>1: " + int2);
					if (!int2.equals("***CLOSE***")) {
						out1.writeObject(int2);
					} else {
						out2.writeObject(int2);
						winningPlayer = (int) in2.readObject();
					}
				}

				if (int1.equals("***CLOSE***") && int2.equals("***CLOSE***")) {
					if (winningPlayer == 1) {
						System.out.println("Winner is player " + winningPlayer);
						theLobby.playerLinkedList.addFirst(p1);
						theLobby.playerLinkedList.addLast(p2);
						System.out.println("Winner is player " + winningPlayer);
					} else if (winningPlayer == 2) {
						theLobby.playerLinkedList.addFirst(p2);
						theLobby.playerLinkedList.addLast(p1);
					} else if (winningPlayer == 0) {
						System.out.println("No winner");
						theLobby.playerLinkedList.addFirst(p1);
						theLobby.playerLinkedList.addFirst(p2);
					}
					theLobby.updateLocalAndNetworkPlayerListLabels();
					cont = false;
					LobbyGUI.gameInSession = false;
				}
			} while (cont);
		} catch (IOException | ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Error: ServerService thread closed!");
		}
	}
}
