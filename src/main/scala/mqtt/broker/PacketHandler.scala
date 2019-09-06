package mqtt.broker

import mqtt.model.Packet

/**
 * Represents the logic to modify the server state at the moment of a reception of a particular kind of packet.
 *
 * @tparam T the kind of the packet.
 */
trait PacketHandler[T <: Packet] {
  /**
   * Handles a packet of kind T received on a socket and modifies the server state properly.
   *
   * @param state  the old state of the server.
   * @param packet the packet of kind T to be processed.
   * @param socket the socket on which the packet has been received
   * @return the new state of the server.
   */
  def handle(state: State, packet: T, socket: Socket): State
}
