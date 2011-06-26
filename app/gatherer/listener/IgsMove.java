package gatherer.listener;

import static gatherer.IgsConstants.*;
import gatherer.TelnetOutputListener;
import gatherer.WeiqiStorage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.BlackOrWhite;
import play.Logger;

// 15 Game 43 I: ryoken (14 359 4) vs rokujidoo (15 598 24)
// 15 Game 530 I: k4152 (16 533 7) vs kindol (2 531 19)
// 15 269(W): J9 K9
// 2
// 1 8
public class IgsMove implements TelnetOutputListener {

	private String server;

	private boolean retrieveMoveList;

	private GameStatus status;

	private static final Pattern GAME_STATUS = Pattern.compile("15 Game +" //
			+ "([0-9]+) " // game nr
			+ "I: " // ONLY IGS GO GAMES, CHESS IS BORING
			// white player: name, prisoners, seconds, byo-yomi period
			+ "([^ ]+) " + "\\(([0-9]+) +([0-9]+) +([0-9-]+)\\)" + " vs "
			// black player: ...
			+ "([^ ]+) " + "\\(([0-9]+) +([0-9]+) +([0-9-]+)\\)" + ".*");

	private static final Pattern MOVE_PATTERN = Pattern.compile("15 +" //
			+ "([0-9]+)\\((W|B)\\): " // move nr and player
			+ "([A-HJ-T0-9 ]+)"); // coordinates

	private final WeiqiStorage storage;

	public IgsMove(String server, WeiqiStorage storage) {
		this.server = server;
		this.storage = storage;
	}

	@Override
	public boolean notify(String line) {

		if (retrieveMoveList) {
			if (line.equals(OBSERVED)) { // ignore flag output for observed move
				Logger.debug("end of observed");
				return true;
			} else if (line.equals(OK) //
					|| line.equals(MOVE_LIST_OK)) { // all moves readed
				Logger.debug("move list of %s retrieved", status.getId());
				retrieveMoveList = false;
				return false;
			}
		}

		// Game 13 I: SHIGE08 (2 546 17) vs YMT123 (3 462 1)
		Matcher gsm = GAME_STATUS.matcher(line);
		if (gsm.matches()) {
			status = getGameStatus(gsm);
			Logger.debug("game status: " + line);
			retrieveMoveList = true;
			return true;
		}

		// 15 269(W): J9 K9
		Matcher mpm = MOVE_PATTERN.matcher(line);
		if (mpm.matches()) {

			int number = Integer.parseInt(mpm.group(1));
			BlackOrWhite player = BlackOrWhite.get(mpm.group(2));

			List<String> list = Arrays.asList(mpm.group(3).split(" "));
			LinkedList<String> stones = new LinkedList<String>(list);
			String coordinate = stones.remove();
			List<String> prisoners = stones;

			GamePlayerStatus gps;
			if (player == BlackOrWhite.WHITE) {
				gps = status.getWhite();
			} else {
				gps = status.getBlack();
			}

			String game = status.getId();
			int seconds = gps.getSeconds();
			int byo = gps.getByo();

			String id = storage.addMove(server, game, number, player, //
					coordinate, seconds, byo, prisoners);
			Logger.debug("game %s move %s", id, line);
			return true;
		}

		return false;
	}

	private GameStatus getGameStatus(Matcher gsm) {
		String id = gsm.group(1);

		GamePlayerStatus white = new GamePlayerStatus();
		white.setPrisoners(Integer.parseInt(gsm.group(3)));
		white.setSeconds(Integer.parseInt(gsm.group(4)));
		white.setByo(Integer.parseInt(gsm.group(5)));

		GamePlayerStatus black = new GamePlayerStatus();
		black.setPrisoners(Integer.parseInt(gsm.group(7)));
		black.setSeconds(Integer.parseInt(gsm.group(8)));
		black.setByo(Integer.parseInt(gsm.group(9)));

		return new GameStatus(id, white, black);
	}
}
