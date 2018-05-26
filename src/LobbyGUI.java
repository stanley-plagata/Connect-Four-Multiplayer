import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class LobbyGUI implements Runnable {
	private boolean isHostOfLobby;
	private String IPAddressToConnectTo;

	private JFrame lobbyFrame;
	private JLabel playerListLabel;
	private final String PLAYER_LIST_PROMPT = "Players in game: ";
	private static final int PORT = 9090;
	private Socket mySocket = null;

	/* variables only the host (server) will use */
	public LinkedList<Player> playerLinkedList;
	public HashMap<Player, ObjectInputStream> inputStreamMap;
	public HashMap<Player, ObjectOutputStream> outputStreamMap;
	private LobbyGUI thisLobby;
	private boolean exit;
	private ServerSocket serverSocket = null;
	public static boolean gameInSession = false;;

	public LobbyGUI(boolean isHostOfLobby, String IPAddressToConnectTo) {
		this.isHostOfLobby = isHostOfLobby;
		this.IPAddressToConnectTo = IPAddressToConnectTo;
		createLobbyGUI();
	}

	private void createLobbyGUI() {
		/* initializes the frame object and the two panel objects */
		if (isHostOfLobby == true) {
			lobbyFrame = new JFrame("Server");
		} else if (isHostOfLobby == false) {
			lobbyFrame = new JFrame("Lobby");
		}
		JPanel playerListPanel = new JPanel();
		JPanel buttonPanel = new JPanel();

		/* initializes the label object */
		playerListLabel = new JLabel(PLAYER_LIST_PROMPT);

		/* initializes the "Start Game" button object */
		JButton startGameButton = new JButton("Start Game");
		startGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (gameInSession) {
					String prompt = "Game in session";
					JOptionPane.showMessageDialog(lobbyFrame, "Please wait until the current game is over", prompt,
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					try {
						for (Player p : playerLinkedList) {
							try {
								outputStreamMap.get(p).writeObject("Areyouconnected");
								inputStreamMap.get(p).readObject().equals("Imconnected");
							} catch (ClassNotFoundException | IOException e1) {
								removePlayerFromServer(p);
							}
						}
					} catch (ConcurrentModificationException e1) {
						// intentionally empty
					}

					if (playerLinkedList.size() >= 2) {
						Player p1 = playerLinkedList.removeFirst();
						Player p2 = playerLinkedList.removeFirst();

						MatchService service = new MatchService(p1, p2, outputStreamMap.get(p1),
								outputStreamMap.get(p2), inputStreamMap.get(p1), inputStreamMap.get(p2), thisLobby);
						Thread t = new Thread(service);
						t.start();
						gameInSession = true;
					} else {
						String prompt = "Not enough players";
						JOptionPane.showMessageDialog(lobbyFrame,
								"Need 2 or more players to start (Currently " + playerLinkedList.size() + ")", prompt,
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});

		/* disables "Start Game" button if user is not the host of the lobby */
		if (!isHostOfLobby) {
			startGameButton.setEnabled(false);
		}

		/* initializes the "Quit Lobby" button object */
		JButton quitLobbyButton = new JButton("Quit Lobby");
		quitLobbyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				/* closes server socket if host of lobby */
				try {
					if (isHostOfLobby) {
						exit = true;
						if (serverSocket != null) {
							serverSocket.close();
						}
					}

					if (mySocket != null) {
						mySocket.close();
					}
				} catch (IOException e1) {
					System.out.println("[Server] Unable to close server socket");
					System.exit(1);
				} finally {
					new MainMenuGUI();
					lobbyFrame.dispose();
				}
			}

		});

		/*
		 * adds the playerListLabel object (and the spacing) to the playerListPanel
		 * object
		 */
		playerListPanel.add(playerListLabel);
		playerListPanel.add(Box.createRigidArea(new Dimension(0, 5)));

		/* sets the buttonPanel object's layout */
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

		/*
		 * adds all the buttons (and the spacing inbetween each button) to the
		 * buttonPanel object
		 */
		buttonPanel.add(Box.createRigidArea(new Dimension(50, 0)));
		buttonPanel.add(startGameButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(70, 0)));
		buttonPanel.add(quitLobbyButton);

		/* adds the playerListPanel and buttonPanel objects to the frame object */
		lobbyFrame.add(playerListPanel, BorderLayout.NORTH);
		lobbyFrame.add(buttonPanel, BorderLayout.CENTER);

		/* sets the size, visibility, and behavior of the frame object */
		lobbyFrame.setSize(400, 200);
		lobbyFrame.setVisible(true);
		lobbyFrame.setLocationRelativeTo(null);
		lobbyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setLocalPlayerListLabel(String stringForLabel) {
		playerListLabel.setText(stringForLabel);
	}

	public void updateLocalAndNetworkPlayerListLabels() {
		String stringForLabel = PLAYER_LIST_PROMPT;
		for (Player p : playerLinkedList) {
			stringForLabel = stringForLabel + " " + p.getName();
		}

		for (ObjectOutputStream o : outputStreamMap.values()) {
			try {
				o.writeObject(stringForLabel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		setLocalPlayerListLabel(stringForLabel);
	}

	private void addPlayerToServer(Player p, ObjectOutputStream out, ObjectInputStream in) {
		playerLinkedList.add(p);
		outputStreamMap.put(p, out);
		inputStreamMap.put(p, in);
		updateLocalAndNetworkPlayerListLabels();
	}

	private void removePlayerFromServer(Player p) {
		playerLinkedList.removeFirstOccurrence(p);
		inputStreamMap.remove(p);
		outputStreamMap.remove(p);
		updateLocalAndNetworkPlayerListLabels();
	}

	@Override
	public void run() {
		/* if this is a thread of a host */
		if (isHostOfLobby) {
			playerLinkedList = new LinkedList<>();
			outputStreamMap = new HashMap<Player, ObjectOutputStream>();
			inputStreamMap = new HashMap<Player, ObjectInputStream>();
			thisLobby = this;
			exit = false;

			try {
				/* initializes the socket objects */
				serverSocket = new ServerSocket(PORT);
				System.out.println("[Server] Listening for connections ...");

				/* loops the serverSocket to listen for incoming connections */
				while (!exit) {
					Socket client = serverSocket.accept();
					String playerName = client.getInetAddress().getHostName();
					Player player = new Player(playerName, client);

					/* makes a LinkedList of the player names */
					LinkedList<String> nameLinkedList = new LinkedList<String>();
					for (Player p : playerLinkedList) {
						nameLinkedList.add(p.getName());
					}

					/* looks through LinkedList of names to see if there are any duplicates */
					if (nameLinkedList.contains(player.getName())) {
						int duplicateCount = 1;
						do {
							String duplicateIndicator = "(" + duplicateCount + ")";
							String adjustedPlayerName = playerName + duplicateIndicator;
							player = new Player(adjustedPlayerName, client);
							duplicateCount++;
						} while (nameLinkedList.contains(player.getName()));
					}

					ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
					ObjectInputStream input = new ObjectInputStream(client.getInputStream());
					addPlayerToServer(player, output, input);
				}
			} catch (IOException e) {
				System.out.println("[Server] Server closed");
			}
		}

		/* if this is a thread of a client */
		else if (!isHostOfLobby) {
			boolean socketIsValid = true;
			try {
				/* initialize the connection with the server */
				mySocket = new Socket(IPAddressToConnectTo, PORT);

				/* checks if the created socket is valid */
				PrintStream printStreamTest = new PrintStream(mySocket.getOutputStream());
				if (printStreamTest.checkError()) {
					socketIsValid = false;
				}
			} catch (IOException e2) {
				socketIsValid = false;
			}

			/* if the socket is invalid, return to main menu */
			if (!socketIsValid) {
				if (lobbyFrame.isActive()) {
					JOptionPane.showMessageDialog(lobbyFrame, "Address not valid");
					new MainMenuGUI();
					lobbyFrame.dispose();
				}
			}
			/* else if the socket is valid, continue */
			else {
				try {
					System.out.println("[Client] Connection established successfully");

					/* establish the client output and input */
					ObjectOutputStream output = new ObjectOutputStream(mySocket.getOutputStream());
					ObjectInputStream input = new ObjectInputStream(mySocket.getInputStream());

					/* loops the client to listen for messages from the server */
					while (true) {
						System.out.println("In Lobby ...");

						/* gets input from server */
						Object incoming = input.readObject();
						String message = "";
						if (incoming.getClass() == String.class) {
							message = (String) incoming;
						}

						/* if message contains playerListPrompt, update the playerLinkedList */
						if (message.length() >= 17) {
							if (message.substring(0, 17).equals(PLAYER_LIST_PROMPT)) {
								setLocalPlayerListLabel(message);
							}
						}

						if (message.equals("Areyouconnected")) {
							output.writeObject("Imconnected");
						}

						/* if message contains the startgame message, launch matchGUI */
						if (message.equals("Startgame")) {
							System.out.println("Game is starting ...");

							incoming = input.readObject();
							message = (String) incoming;
							System.out.println("Launching match for player " + message);
							MatchGUI match = new MatchGUI(output, Integer.parseInt(message));
							System.out.println("Match loaded successfully");

							/* loops to listen for game inputs from opponent */
							boolean cont = true;
							while (cont) {
								System.out.println("Waiting for board input ...");
								String update = (String) input.readObject();
								try {
									Integer temp = Integer.parseInt(update);
									match.setGameBoard(temp);
									System.out.println("Board updated at column " + update);
								} catch (NumberFormatException e) {
									if (update.equals("***CLOSE***")) {
										System.out.println("Match is ending ...");
										cont = false;
									}
								}
							}
						}
					}
				} catch (IOException | ClassNotFoundException e) {
					System.out.println("[Client] Connection closed");
				}
			}
		}
	}
}
