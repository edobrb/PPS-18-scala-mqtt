package mqtt.parser

import mqtt.model.{Packet, QoS}
import mqtt.model.Packet._
import mqtt.parser.BitParsers._
import mqtt.parser.Monad._
import mqtt.parser.MqttFragmentsParsers._
import mqtt.parser.Parsers.{Parser, first, ifConditionFails, many, many1, fail}
import mqtt.utils.BitImplicits._

import scala.concurrent.duration._

/**
 * A container of MQTT 3.1.1 packets parsers.
 */
object MqttPacketsParsers {
  
  /**
   * Parser of all MQTT 3.1.1 packets.
   *
   * @return the MQTT 3.1.1 packets parser
   */
  def mqtt(): Parser[Packet] = first (
    connect(), connack(), disconnect(),
    publish(), puback(), pubrec(), pubrel(), pubcomp(),
    subscribe(), suback(),
    unsubscribe(), unsuback()
  )
  
  def connect(): Parser[Packet] = for {
    _ <- packetType(ConnectMask)
    _ <- reserved()
    _ <- variableLength()
    _ <- protocolName()
    version <- protocolLevel()
    flags <- connectFlags()
    keepAlive <- keepAlive()
    clientId <- utf8()
    willMessage <- willPayload(flags.willFlags)
    credentials <- credentials(flags.credentials)
  } yield Connect(Protocol("MQTT", version), flags cleanSession, keepAlive seconds, clientId, credentials, willMessage)
  
  def connack(): Parser[Packet] = for {
    _ <- packetType(ConnackMask)
    _ <- reserved()
    _ <- variableLength()
    session <- sessionPresent()
    code <- connectReturnCode()
  } yield Connack(session, code)
  
  def disconnect(): Parser[Packet] = for {
    _ <- disconnectPacketType()
    _ <- reserved()
    _ <- variableLength()
  } yield Disconnect()
  
  def publish(): Parser[Packet] = for {
    _ <- packetType(PublishMask)
    dup <- bit()
    qos <- qos(); _ <- fail(!dup && qos != QoS(0))
    retain <- bit()
    _ <- variableLength()
    topic <- utf8()
    id <- ifConditionFails(0, packetIdentifier())(qos != QoS(0))
    payload <- many(byte())
  } yield Publish(dup, id, ApplicationMessage(retain, qos, topic, payload))
  
  def puback(): Parser[Packet] = for {
    _ <- packetType(PubackMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Puback(id)
  
  def pubrec(): Parser[Packet] = for {
    _ <- packetType(PubrecMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Pubrec(id)
  
  def pubrel(): Parser[Packet] = for {
    _ <- packetType(PubrelMask)
    _ <- reserved2()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Pubrel(id)
  
  def pubcomp(): Parser[Packet] = for {
    _ <- packetType(PubcompMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Pubcomp(id)
  
  def subscribe(): Parser[Packet] = for {
    _ <- packetType(SubscribeMask)
    _ <- reserved2()
    _ <- variableLength()
    id <- packetIdentifier()
    subs <- many1(subscription())
  } yield Subscribe(id, subs)
  
  def suback(): Parser[Packet] = for {
    _ <- packetType(SubackMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
    subs <- many1(subscriptionGrantedQoS())
  } yield Suback(id, subs)
  
  def unsubscribe(): Parser[Packet] = for {
    _ <- packetType(UnsubscribeMask)
    _ <- reserved2()
    _ <- variableLength()
    id <- packetIdentifier()
    unsubs <- many1(unsubscription())
  } yield Unsubscribe(id, unsubs)
  
  def unsuback(): Parser[Packet] = for {
    _ <- packetType(UnsubackMask)
    _ <- reserved()
    _ <- variableLength()
    id <- packetIdentifier()
  } yield Unsuback(id)
}
