package jobs;

import models.Game;
import models.GamePlayer;
import models.GameServer;
import models.Handicap;
import models.Player;
import models.Rank;
import models.Size;
import play.Logger;
import play.jobs.Job;

public class CreateGame extends Job {

	private final String server;
	private final String onlineId;
	private final String white;
	private final String wRank;
	private final String black;
	private final String bRank;
	private final int turn;
	private final int size;
	private final int handicap;
	private final float komi;
	private final int byo;
	private final int observer;

	public CreateGame(String server, String onlineId, String white, String wRank, String black,
			String bRank, int turn, int size, int handicap, float komi, int byoYomi, int observer) {
		this.server = server;
		this.onlineId = onlineId;
		this.white = white;
		this.wRank = wRank;
		this.black = black;
		this.bRank = bRank;
		this.turn = turn;
		this.size = size;
		this.handicap = handicap;
		this.komi = komi;
		this.byo = byoYomi;
		this.observer = observer;
	}

	@Override
	public void doJob() throws Exception {
		GameServer gameServer = GameServer.findByHost(server);
		Game actual = Game.findByServerAndOnlineId(gameServer, onlineId);
		if (actual != null) {
			Logger.error("game %s %s exists", actual.server, actual.onlineId);
			return;
		}

		updateNextChannelGames(createGame(gameServer));
	}

	private Game createGame(GameServer gameServer) {
		Game game = new Game();
		game.server = gameServer;
		game.onlineId = onlineId;

		GamePlayer w = new GamePlayer();
		w.player = Player.getPlayer(white);
		w.rank = Rank.getRank(wRank);
		game.white = w;

		GamePlayer b = new GamePlayer();
		b.player = Player.getPlayer(black);
		b.rank = Rank.getRank(bRank);
		game.black = b;

		game.turn = turn;
		game.size = Size.get(size);
		game.handicap = Handicap.getHandicap(handicap);
		game.komi = komi;
		game.byo = byo;
		game.observer = observer;

		Logger.debug("save game %s", game);
		return game.save();
	}

	private void updateNextChannelGames(Game game) {
		Logger.info("%s created", game);
		new CheckAsNextChannelGame(game).now();
	}
}
