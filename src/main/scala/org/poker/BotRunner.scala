package org.poker


import com.typesafe.scalalogging.slf4j.StrictLogging
import org.poker.handler._
import org.pircbotx.cap.TLSCapHandler
import org.pircbotx.hooks.Listener
import org.pircbotx.{UtilSSLSocketFactory, Configuration, PircBotX}
import org.poker.poller.CoinMarketCaps

class BotRunner(pc: ProgramConfiguration) extends StrictLogging {
  val coinMarketCaps = new CoinMarketCaps(pc);

  def run(): Unit = {
    startPollers()
    val ircBotConfig = this.getIrcBotConfiguration()
    val ircBot = new PircBotX(ircBotConfig)
    logger.debug("connecting to '{}'", pc.serverHostname)
    ircBot.startBot()
  }

  def startPollers() {
    coinMarketCaps.start()
  }

  def getListener(): Listener[PircBotX] = {
    val listener = new BotListener()
    listener.addHandler(new UptimeMessageEventHandler)
    listener.addHandler(new GoogleMessageEventHandler(pc))
    listener.addHandler(new UrlMessageEventHandler(pc))
    listener.addHandler(new StreamsMessageEventHandler(pc))
    listener.addHandler(new BitcoinMessageEventHandler(pc, coinMarketCaps))
    listener.addHandler(new BitcoinAddressMessageEventHandler(pc))
    listener.addHandler(new StockMessageEventHandler)
    listener.addHandler(new DotaMessageEventHandler(pc))
    listener.addHandler(new RottenTomatoesMessageEventHandler(pc))
    listener.addHandler(new SceneAccessMessageEventHandler(pc))
    listener.addHandler(new DogecoinMessageEventHandler(pc, coinMarketCaps))
    listener
  }

  def getIrcBotConfiguration(): Configuration[PircBotX] = {
    val listener = getListener()
    val builder = new Configuration.Builder()
      .setName(pc.nick)
      .setFinger(pc.finger)
      .setRealName(pc.realName)
      .setCapEnabled(true)
      .addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true))
      .setAutoReconnect(true)
      .addListener(listener)
      .setLogin(pc.nick)
      .setShutdownHookEnabled(true)
      .setServerHostname(pc.serverHostname)
    for (c <- pc.channels) {
      logger.debug("adding autojoin channel {}", c)
      builder.addAutoJoinChannel(c)
    }
    builder.buildConfiguration()
  }
}