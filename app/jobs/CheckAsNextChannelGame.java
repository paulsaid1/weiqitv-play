package jobs;

import java.util.List;

import models.Channel;
import models.Criteria;
import models.Game;
import play.Logger;
import play.jobs.Job;

/**
 * match a game with each criteria and update related channels
 * 
 * TODO use an upcoming game list per channel
 * 
 * @author dagnu
 * 
 */
public class CheckAsNextChannelGame extends Job {

	private final Game game;

	public CheckAsNextChannelGame(Game game) {
		this.game = game;
	}

	@Override
	public void doJob() throws Exception {
		List<Criteria> criteriaList = Criteria.all().fetch();
		for (Criteria criteria : criteriaList) {
			Logger.debug("%s matches? %s", criteria.name, game);
			if (criteria.matches(game)) {
				Logger.debug("%s matches!", criteria.name);
				List<Channel> channels = Channel.findByCriteria(criteria);
				for (Channel channel : channels) {
					if (game.isBetterThan(channel.next)) {
						channel.next = game;
						Logger.info("%s is the next game on channel %s", game, channel.title);
						channel.save();
					}
				}
			}
		}
	}
}
