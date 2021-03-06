package org.poker


import com.typesafe.scalalogging.slf4j.StrictLogging
import org.poker.handler._
import org.pircbotx.cap.TLSCapHandler
import org.pircbotx.hooks.Listener
import org.pircbotx.{UtilSSLSocketFactory, Configuration, PircBotX}
import org.poker.poller.{DotaPoller, UntappdPoller, SceneAccessPoller, CoinMarketCaps}

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class BotRunner(pc: ProgramConfiguration) extends StrictLogging {
  val coinMarketCaps = new CoinMarketCaps(pc)
  val ircBotConfig = this.getIrcBotConfiguration()
  val ircBot = new PircBotX(ircBotConfig)
  lazy val sceneAccessPoller = new SceneAccessPoller(pc, ircBot)
  lazy val untappdPoller = new UntappdPoller(pc, ircBot)
  lazy val dotaPoller = new DotaPoller(pc, ircBot)

  def run(): Unit = {
    startPollers()
    logger.debug("connecting to '{}'", pc.serverHostname)
    ircBot.startBot()
  }

  def startPollers() {
    coinMarketCaps.start()
    if (pc.sceneAccessPassword.isDefined && pc.sceneAccessUserName.isDefined) {
      sceneAccessPoller.start()
    }
    if (untappdEnabled && !pc.testMode) {
      untappdPoller.start()
    }
    if (dotaEnabled) {
      dotaPoller.start()
    }
  }

  lazy val dotaEnabled =  {
    pc.steamApiKey.isDefined
  }

  lazy val untappdEnabled = {
    pc.untappdClientId.isDefined && pc.untappdClientSecret.isDefined && pc.untappdAccessToken.isDefined
  }

  lazy val weatherEnabled = {
    pc.yahooConsumerKey.isDefined && pc.yahooConsumerSecret.isDefined
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
    listener.addHandler(new CryptoCoinMessageEventHandler(pc, coinMarketCaps))
    listener.addHandler(new InfoMessageEventHandler)
    listener.addHandler(new WorldCupMessageEventHandler)
    if (untappdEnabled) {
      listener.addHandler(new BeerMessageEventHandler(pc.untappdClientId.get, pc.untappdClientSecret.get, pc.untappdAccessToken.get))
    }
    if (weatherEnabled) {
      listener.addHandler(new WeatherMessageHandler(pc))
    }
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
      .setAutoSplitMessage(true)
      .setAutoNickChange(true)
      .setShutdownHookEnabled(true)
      .setEncoding(StandardCharsets.UTF_8)
      .setServerHostname(pc.serverHostname)
    for (c <- pc.channels) {
      logger.debug("adding autojoin channel: '{}'", c)
      builder.addAutoJoinChannel(c)
    }
    builder.buildConfiguration()
  }
}