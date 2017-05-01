package pkgPoker.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import netgame.common.Hub;
import pkgPokerBLL.Action;
import pkgPokerBLL.Card;
import pkgPokerBLL.CardDraw;
import pkgPokerBLL.Deck;
import pkgPokerBLL.GamePlay;
import pkgPokerBLL.GamePlayPlayerHand;
import pkgPokerBLL.Player;
import pkgPokerBLL.Rule;
import pkgPokerBLL.Table;

import pkgPokerEnum.eAction;
import pkgPokerEnum.eCardDestination;
import pkgPokerEnum.eDrawCount;
import pkgPokerEnum.eGame;
import pkgPokerEnum.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private int iDealNbr = 0;

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {

		if (playerID == 2) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			Player actPlayer = (Player) ((Action) message).getPlayer();
			Action act = (Action) message;
			switch (act.getAction()) {
			case Sit:
				HubPokerTable.AddPlayerToTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case Leave:
				HubPokerTable.RemovePlayerFromTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case StartGame:
				// Get the rule from the Action object.
				Rule rle = new Rule(act.geteGame());

				// TODO Lab #5 - If neither player has 'the button', pick a
				// random player
				// and assign the button.
				HubGamePlay = new GamePlay(rle, act.getPlayer().getPlayerID());
				Iterator it = HubPokerTable.getHmPlayer().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					Player p = (Player) pair.getValue();
					HubGamePlay.addPlayerHandToGame(p);
				}
				HubGamePlay.setiActOrder(HubGamePlay.GetOrder(act.getPlayer().getiPlayerPosition()));
				int pos = HubGamePlay.NextPosition(act.getPlayer().getiPlayerPosition(), HubGamePlay.getiActOrder());
				HubGamePlay.setPlayerNextToAct(HubGamePlay.getPlayerByPosition(pos));
				HubGamePlay.setGameDeck(new Deck(rle.GetNumberOfJokers(), rle.GetWildCards()));

			case Draw:
				int c = HubGamePlay.geteDrawCountLast().getDrawNo() + 1;
				CardDraw cd = (CardDraw) HubGamePlay.getRule().getHmCardDraw().get(c);
				Player cPlayer = HubGamePlay.getPlayerNextToAct();
				if (cd.getCardDestination() == eCardDestination.Community) {

					for (int i = 0; i < cd.getCardCount().getCardCount(); i++) {
						HubGamePlay.drawCard(cPlayer, cd.getCardDestination());
					}
				} else {
					Iterator it2 = HubPokerTable.getHmPlayer().entrySet().iterator();
					while (it2.hasNext()) {
						Map.Entry pair = (Map.Entry) it2.next();
						Player p = (Player) pair.getValue();
						for (int i = 0; i < cd.getCardCount().getCardCount(); i++) {
							HubGamePlay.drawCard(p, cd.getCardDestination());
						}
					}

				}
				HubGamePlay.setPlayerNextToAct(HubGamePlay.getPlayerByPosition(cPlayer.getiPlayerPosition()));
				HubGamePlay.seteDrawCountLast(eDrawCount.geteDrawCount(c));

				
				HubGamePlay.isGameOver();

				resetOutput();
				sendToAll(HubGamePlay);
				break;
			case ScoreGame:
				resetOutput();
				sendToAll(HubGamePlay);
				break;
			}
		}
	}
}