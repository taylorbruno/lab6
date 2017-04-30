package pkgPokerBLL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Table implements Serializable {

	private UUID TableID;

	private HashMap<UUID, Player> hmPlayer = new HashMap<UUID, Player>();

	public Table() {
		super();
		TableID = UUID.randomUUID();
	}

	public Table AddPlayerToTable(Player p) {
		hmPlayer.put(p.getPlayerID(), p);
		return this;
	}

	public Table RemovePlayerFromTable(Player p) {
		hmPlayer.remove(p.getPlayerID());
		return this;
	}

	public HashMap<UUID, Player> getHmPlayer() {
		return hmPlayer;
	}
	
	
}