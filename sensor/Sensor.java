package sensor;
/*
 * Updated on Feb 2023
 */

import common.MessageInfo;

import java.io.IOException;
import java.net.*;
import java.util.Random;

 /* You can add/change/delete class attributes if you think it would be
  * appropriate.
  *
  * You can also add helper methods and change the implementation of those
  * provided if you think it would be appropriate, as long as you DO NOT
  * CHANGE the provided interface.
  */

public class Sensor implements ISensor {
    private float measurement;

    private final static int max_measure = 50;
    private final static int min_measure = 10;

    private DatagramSocket s;
    private byte[] buffer;

    protected int totMsg;
    protected int port;
    protected String address;

    InetAddress inetAddress;

    /* Note: Could you discuss in one line of comment what do you think can be
     * an appropriate size for buffsize?
     * (Which is used to init DatagramPacket?)
     */
    /* buffsize should be 2048 as UDP datagrams are typically less than 1500 bytes to avoid
    fragmentation */
    private static final int buffsize = 2048;

    public Sensor(String address, int port, int totMsg) {
        /* TODO: Build Sensor Object */
        this.port = port;
        this.address = address;
        this.totMsg = totMsg;

        try {
            s = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(
                "Socket could not be opened or "
                    + "failed to bind to port " + port + ". \n");
        }
    }

    @Override
    public void run (int N) throws InterruptedException {
        /* TODO: Send N measurements */
        totMsg = N;
        /* Hint: You can pick ONE measurement by calling */
         float measurement = this.getMeasurement();

         /* TODO: Call sendMessage() to send the msg to destination */
        MessageInfo msg;
        try {
            msg = new MessageInfo(totMsg, 0, measurement);
        } catch (Exception e) {
            // Exit as the message cannot be initialised
            throw new RuntimeException(
                "Message initialisation failed, check input parameters.\n");
        }

        // Send N messages
        for (int i = 1; i <= N; i++) {
            measurement = getMeasurement();
            int nextMsgNum = msg.getMessageNum() + 1;
            msg.setMessageNum(nextMsgNum);
            msg.setMessage(measurement);
            sendMessage(address, port, msg);
        }
    }

    public static void main (String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: ./sensor.sh field_unit_address port number_of_measures");
            return;
        }

        /* Parse input arguments */
        String address = args[0];
        int port = Integer.parseInt(args[1]);
        int totMsg = Integer.parseInt(args[2]);

        /* TODO: Call constructor of sensor to build Sensor object*/
        UDPSensor udpSensor = new Sensor(address, port, totMsg);

        /* TODO: Use Run to send the messages */
        udpSensor.run(2000);

        udpSensor.closeSocket(); 
    }

    @Override
    public void sendMessage (String address, int port, MessageInfo msg) {
        String toSend = msg.toString();

        /* TODO: Build destination address object */
        try {
            inetAddress = InetAddress.getByName(dstAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException("IP address could not be found. \n");
        }
        /* TODO: Build datagram packet to send */
        String outgoingDataStr = msg.toString();
        buffer = outgoingDataStr.getBytes();
        Integer intPort = Integer.parseInt(port);

        DatagramPacket outgoingMsg
            = new DatagramPacket(buffer, buffer.length, inetAddress, intPort);

        Integer msgNum = msg.getMessageNum();
        Float msgInfo = msg.getMessage();
        System.out.printf(
            "[Sensor] Sending message " + msgNum + " out of " + totMsg
                + ". Measure = " + msgInfo + "\n");

        /* TODO: Send packet */
        try {
            s.send(outgoingMsg);
        } catch (IOException e) {
            throw new RuntimeException("UDP sensor unable to send "
                + "datagram packets, exiting. \n");
        }

    }

    @Override
    public float getMeasurement () {
        Random r = new Random();
        measurement = r.nextFloat() * (max_measure - min_measure) + min_measure;

        return measurement;
    }

    public void closeSocket() {
        udpSocket.close();
    }
}
