package servers;
//import server.DorvalServerImpl;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.xml.ws.Endpoint;

//@author Binay

public class DorvalCampusServer extends Thread {
    private Thread t;

    public void run() {
        String sStatus = "Action couldn't carried out", sStudentId,
                sTimeSlot, sDate, sServer, sRoomNumber, sBookingId;

        String bindingURI = "http://localhost:9897/CampusServerInterface";
        DorvalServerImpl DorvalObj = new DorvalServerImpl();
        Endpoint.publish(bindingURI, DorvalObj);
        System.out.println("Server started at: " + bindingURI);

        try {
            DatagramSocket serverSocket = new DatagramSocket(9878);

            while (true) {
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
                String[] parts;
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sReceivedData = new String(receivePacket.getData());

                if (sReceivedData.startsWith("1")) {
                    parts = sReceivedData.split("_");
                    sDate = parts[1];
                    sRoomNumber = parts[2];
                    //  String[] sTimeSlotList =new String[parts.length-3];
                    ArrayList<String> sTimeSlots = new ArrayList<String>();//Creating arraylist
                    //System.out.println("Please Enter "+iNoTimeSlots +" Time Slots(HH:MM-HH:MM format):");

                    for (int i = 3; i < parts.length; i++) {
                        sTimeSlots.add(i - 3, parts[i].trim());
                    }

                    sStatus = DorvalObj.createRoom(sDate, sRoomNumber, sTimeSlots);
                } else if (sReceivedData.startsWith("2")) {
                    parts = sReceivedData.split("_");
                    sRoomNumber = parts[1];
                    sDate = parts[2];
                    //  String[] sTimeSlotList =new String[parts.length-3];
                    ArrayList<String> sTimeSlots = new ArrayList<String>();//Creating arraylist
                    //System.out.println("Please Enter "+iNoTimeSlots +" Time Slots(HH:MM-HH:MM format):");

                    for (int i = 3; i < parts.length; i++) {
                        sTimeSlots.add(i - 3, parts[i].trim());
                    }

                    sStatus = DorvalObj.deleteRoom(sRoomNumber, sDate, sTimeSlots);
                } else if (sReceivedData.startsWith("3")) {
                    parts = sReceivedData.split("_");
                    sStudentId = parts[1];
                    sServer = parts[2];
                    sRoomNumber = parts[3];
                    sDate = parts[4];
                    sTimeSlot = parts[5].trim();
                    sStatus = DorvalObj.bookRoom(sStudentId, sServer, sRoomNumber, sDate, sTimeSlot);
                } else if (sReceivedData.startsWith("5"))//get available time slot
                {
                    parts = sReceivedData.split("_");
                    sServer = parts[1];
                    sDate = parts[2].trim();
                    sStatus = DorvalObj.getAvailableTimeSlot(sServer, sDate);
                } else if (sReceivedData.startsWith("7"))//cancel booking
                {
                    parts = sReceivedData.split("_");
                    sBookingId = parts[1];
                    sStudentId = parts[2].trim();
                    sStatus = DorvalObj.cancelBooking(sBookingId, sStudentId);
                } else if (sReceivedData.startsWith("9"))//Change reservation
                {
                    parts = sReceivedData.split("_");
                    sBookingId = parts[1];
                    sServer = parts[2];
                    sDate = parts[3];
                    sRoomNumber = parts[4];
                    sTimeSlot = parts[5];
                    sStudentId = parts[6].trim();
                    sStatus = DorvalObj.getAvailableTimeSlot(sServer, sDate);
                } else if (sReceivedData.startsWith("11"))//Decrease Booking Count
                {
                    parts = sReceivedData.split("_");
                    for (int i = 0; i < parts.length; i++) {
                        sStudentId = parts[i].trim();
                        DorvalObj.AdjustBookingCount(sStudentId);
                    }
                }

                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                sendData = sStatus.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }

        } catch (Exception e) {
            System.out.println("Thread interrupted." + e);
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }


    public static void main(String str[]) {
        DorvalCampusServer T1 = new DorvalCampusServer();
        T1.start();
    }

}
