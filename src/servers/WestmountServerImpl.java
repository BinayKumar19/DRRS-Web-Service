package servers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.jws.WebService;
//@author Binay

@WebService(endpointInterface = "servers.CampusServerInterface")
public class WestmountServerImpl implements CampusServerInterface {

    //Contains Date as key and hRoom as value
    static HashMap<String, HashMap<String, HashMap<String, String>>> hDate =
            new HashMap<String, HashMap<String, HashMap<String, String>>>();
    //Contains Room No. as key and hTimeStudent as value
    static HashMap<String, HashMap<String, String>> hRoom = new HashMap<String, HashMap<String, String>>();
    //contains Timeslot as Key and StudentID+Booking ID as Value
    static HashMap<String, String> hTimeStudent = new HashMap<String, String>();

    static int iBookingIdNo = 1;
    static String[] sStudentIdArray = new String[100];
    static int iStudentBookingCount[] = new int[100];
    static long iStudentBookingTime[] = new long[100];

    @Override
    public String createRoom(String date, String roomNumber, ArrayList<String> TimeSlots) {
        BufferedWriter output = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d1 = new Date();
        String sStatus, sTimeSlot;
        Iterator itr;

        synchronized (this) {

            try {
                FileWriter file = new FileWriter("./Logs/WestmountServer.txt", true);
                output = new BufferedWriter(file);
                output.append(dateFormat.format(d1) + " Create Room" + System.lineSeparator());
                output.append("Date:" + date + System.lineSeparator());

                if (hDate.containsKey(date))  //Date present in System
                {
                    if (hDate.get(date).containsKey(roomNumber)) //Room present in System
                    {
                        output.append("Room Number:" + roomNumber + " Already Exists" + System.lineSeparator());
                        itr = TimeSlots.iterator();
                        while (itr.hasNext()) {
                            sTimeSlot = (String) itr.next();
                            if ((hDate.get(date).get(roomNumber).containsKey(sTimeSlot)))
                                output.append("Time Slot:" + sTimeSlot + " Already Present" + System.lineSeparator());
                            else {
                                (hDate.get(date).get(roomNumber)).put(sTimeSlot, null);
                                output.append("Time Slot:" + sTimeSlot + " Created" + System.lineSeparator());
                            }
                        }
                    } else //Room No. doesn't exist in System
                    {
                        output.append("Room Number:" + roomNumber + " Created" + System.lineSeparator());
                        itr = TimeSlots.iterator();
                        while (itr.hasNext()) {
                            sTimeSlot = (String) itr.next();
                            hTimeStudent.put(sTimeSlot, null);
                            output.append("Time Slot:" + sTimeSlot + " Created" + System.lineSeparator());
                        }
                        (hDate.get(date)).put(roomNumber, hTimeStudent);
                    }
                } else //Date doesn't exist in System
                {
                    output.append("Date Created" + System.lineSeparator());
                    output.append("Room Number:" + roomNumber + " Created" + System.lineSeparator());

                    itr = TimeSlots.iterator();
                    while (itr.hasNext()) {
                        sTimeSlot = (String) itr.next();
                        hTimeStudent.put(sTimeSlot, null);
                        output.append("Time Slot:" + sTimeSlot + " Created" + System.lineSeparator());
                    }
                    hRoom.put(roomNumber, hTimeStudent);
                    hDate.put(date, hRoom);
                }

                sStatus = "Room Created";
                output.append(sStatus + System.lineSeparator());
                output.append(System.lineSeparator());
                output.close();
            } catch (IOException e) {
                sStatus = "Room Not Created";
                e.printStackTrace();
            }
        }
        return sStatus;
    }

    @Override
    public String deleteRoom(String date, String roomNumber, ArrayList<String> TimeSlots) {
        BufferedWriter output = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d1 = new Date();
        String sStatus, sKirklandSentence = "11", sDorvalSentence = "11", sStudentId;
        String sTimeSlot;
        Iterator itr;
        synchronized (this) {

            try {
                FileWriter file = new FileWriter("./Logs/WestmountServer.txt", true);
                output = new BufferedWriter(file);
                output.append(dateFormat.format(d1) + " Delete Room" + System.lineSeparator());

                itr = TimeSlots.iterator();
                while (itr.hasNext()) {
                    sTimeSlot = (String) itr.next();
                    if (hDate.get(date).get(roomNumber).get(sTimeSlot) == null)
                        hDate.get(date).get(roomNumber).remove(sTimeSlot);
                    else {
                        sStudentId = hDate.get(date).get(roomNumber).get(sTimeSlot).substring(0, 7);
                        if (sStudentId.substring(0, 4).equals("WSTS"))
                            AdjustBookingCount(sStudentId);
                        else if (sStudentId.substring(0, 4).equals("KKLS"))
                            sKirklandSentence = sKirklandSentence + "_" + sStudentId;
                        else if (sStudentId.substring(0, 4).equals("DVLS"))
                            sDorvalSentence = sDorvalSentence + "_" + sStudentId;

                        hDate.get(date).get(roomNumber).remove(sTimeSlot);
                    }
                }

                String sBookingCountStatus;
                if (sKirklandSentence.length() > 2)
                    sBookingCountStatus = otherServerUDP(9876, sKirklandSentence);
                if (sDorvalSentence.length() > 2)
                    sBookingCountStatus = otherServerUDP(9878, sDorvalSentence);

                sStatus = "Room Deleted";

                output.append(sStatus + System.lineSeparator());
                output.append(System.lineSeparator());
                output.close();

            } catch (Exception e) {
                sStatus = "Room Not Deleted";
                e.printStackTrace();
            }
        }
        return sStatus;
    }

    private String bookRoomWestmount(String sStudentId, String roomNumber, String date, String timeslot) {
        String sBookingId;
        String sBookingStatus = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d1 = new Date();
        BufferedWriter output = null;
        synchronized (this) {
            try {

                FileWriter file = new FileWriter("./Logs/WestmountServer.txt", true);
                output = new BufferedWriter(file);
                output.append(dateFormat.format(d1) + " Book Room" + System.lineSeparator());
                output.append("Booking Room:" + roomNumber + System.lineSeparator());
                output.append("Booking Date:" + date + System.lineSeparator());
                output.append("Booking Time:" + timeslot + System.lineSeparator());

                if (hDate.containsKey(date)) {
                    if (hDate.get(date).containsKey(roomNumber)) {
                        if (hDate.get(date).get(roomNumber).containsKey(timeslot)) {
                            if (hDate.get(date).get(roomNumber).get(timeslot) == null) {
                                sBookingId = "W" + (iBookingIdNo++);
                                (hDate.get(date).get(roomNumber)).put(timeslot, sStudentId + sBookingId);
                                sBookingStatus = "Room Booked. Booking Id: " + sBookingId;
                            } else {
                                sBookingStatus = "Time Slot Already Taken by Someone";
                            }
                        } else {
                            sBookingStatus = "Booking Time not Present in System";
                        }
                    } else {
                        sBookingStatus = "Room not present in System";
                    }
                } else {
                    sBookingStatus = "Date not present in System";
                }
                output.append(sBookingStatus + System.lineSeparator());
                output.append(System.lineSeparator());
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sBookingStatus;
    }

    @Override
    public String bookRoom(String sStudentId, String campusName, String roomNumber, String date, String timeslot) {
        String sBookingStatus = null;

        for (int i = 0; i < sStudentIdArray.length; i++) {
            if (sStudentIdArray[i] != null)
                if ((sStudentIdArray[i].equals(sStudentId) &&
                        (iStudentBookingCount[i] == 3 &&
                                (System.currentTimeMillis() / 3600000 - iStudentBookingTime[i]) < 168))) {
                    sBookingStatus = "Booking Count Reached to Maximum for the Student";
                    return sBookingStatus;
                }
        }


        if (campusName.equals("Westmount"))
            sBookingStatus = bookRoomWestmount(sStudentId, roomNumber, date, timeslot);
        else if (campusName.equals("Dorval")) {
            String sMessage = "3_" + sStudentId + "_" + campusName + "_" + roomNumber + "_" + date + "_" + timeslot;
            sBookingStatus = otherServerUDP(9878, sMessage);
        } else if (campusName.equals("Kirkland")) {
            String sMessage = "3_" + sStudentId + "_" + campusName + "_" + roomNumber + "_" + date + "_" + timeslot;
            sBookingStatus = otherServerUDP(9876, sMessage);
        } else
            sBookingStatus = "Server Name is Incorrect";

        if (sBookingStatus.contains("Room Booked. Booking Id:"))
            AdjustBookingCount(sStudentId);
        return sBookingStatus;
    }

    public void AdjustBookingCount(String sStudentId) {
        for (int i = 0; i < sStudentIdArray.length; i++) {
            if (sStudentIdArray[i] != null && (sStudentIdArray[i].equals(sStudentId))) {
                if (System.currentTimeMillis() / 3600000 - iStudentBookingTime[i] >= 168) {
                    iStudentBookingCount[i] = 1;
                    iStudentBookingTime[i] = System.currentTimeMillis() / 3600000;
                } else
                    iStudentBookingCount[i]++;
                break;
            } else if (sStudentIdArray[i] == null) {
                sStudentIdArray[i] = sStudentId;
                iStudentBookingCount[i] = 1;
                iStudentBookingTime[i] = System.currentTimeMillis() / 3600000;
                break;
            }
        }
    }

    private String getAvailableTimeSlotWestmount(String date) {
        String sBookingStatus = "Westmount:";
        int iTimeSlotCount = 0;

        for (String keyTimeSlot : hTimeStudent.keySet()) {
            if (hTimeStudent != null && hTimeStudent.get(keyTimeSlot) == null) {
                iTimeSlotCount++;
            }
        }
        return (sBookingStatus + iTimeSlotCount);
    }

    @Override
    public String getAvailableTimeSlot(String sServer, String date) {
        String sTimeSlotCount = getAvailableTimeSlotWestmount(date);
        if (sServer.equals("Westmount")) {
            try {
                DatagramSocket clientSocket = new DatagramSocket();
                InetAddress IPAddress = InetAddress.getByName("localhost");
                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];
                String sentence = "5_" + sServer + "_" + date;
                sendData = sentence.getBytes();

                //To get TimeSlot from Kirkland Server
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                sTimeSlotCount = (sTimeSlotCount + (new String(receivePacket.getData()))).trim();
                clientSocket.close();

                //Resetting variables
                receiveData = new byte[1024];

                //To get TimeSlot from Dorval Server
                clientSocket = new DatagramSocket();
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9878);
                clientSocket.send(sendPacket);
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                sTimeSlotCount = (sTimeSlotCount + (new String(receivePacket.getData()))).trim();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return sTimeSlotCount;
    }

    private String cancelBookingWestmount(String sBookingID) {
        BufferedWriter output = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d1 = new Date();
        String sBookingStatus = "Wrong Booking ID";
        try {
            FileWriter file = new FileWriter("./Logs/WestmountServer.txt", true);
            output = new BufferedWriter(file);
            output.append(dateFormat.format(d1) + " Cancel Booking" + System.lineSeparator());
            output.append("Booking ID:" + sBookingID.substring(8) + System.lineSeparator());
            for (String keyTimeSlot : hTimeStudent.keySet()) {
                if (hTimeStudent.get(keyTimeSlot) != null
                        && hTimeStudent.get(keyTimeSlot).equals(sBookingID)) {
                    hTimeStudent.put(keyTimeSlot, null);
                    sBookingStatus = "Booking Cancelled";
                    break;
                }
            }
            output.append(sBookingStatus + System.lineSeparator());
            output.append(System.lineSeparator());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sBookingStatus;
    }

    @Override
    public String cancelBooking(String sBookingID, String sStudentId) {
        String sBookingStatus = "Wrong Booking ID";

        if (sBookingID.charAt(0) == 'W') {
            sBookingStatus = cancelBookingWestmount(sStudentId + sBookingID);
        } else if (sBookingID.charAt(0) == 'K') {
            String sMessage = "7_" + sBookingID + "_" + sStudentId;
            sBookingStatus = otherServerUDP(9876, sMessage);
        } else if (sBookingID.charAt(0) == 'D') {
            String sMessage = "7_" + sBookingID + "_" + sStudentId;
            sBookingStatus = otherServerUDP(9878, sMessage);
        }

        if (sBookingStatus.contains("Booking Cancelled")) {
            for (int i = 0; i < sStudentIdArray.length; i++) {
                if (sStudentIdArray[i] != null && (sStudentIdArray[i].equals(sStudentId))) {
                    iStudentBookingCount[i]--;
                    break;
                }
            }
        }

        return sBookingStatus;
    }

    private String otherServerUDP(int iPortNo, String sUDPMessage) {
        String sStatus = "Not Successful";
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("localhost");
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            sendData = sUDPMessage.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, iPortNo);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            sStatus = new String(receivePacket.getData()).trim();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sStatus;
    }

    @Override
    public String changeReservation(String bookingId, String newCampusName, String bookingDate, String newRoomNo, String newTimeSlot, String studentId) {
        String sBookingStatus = null, sBookingCancelStatus = null, sStatus = "Not able to perform Action";

        if (newCampusName.equals("Westmount"))
            sBookingStatus = bookRoomWestmount(studentId, newRoomNo, bookingDate, newTimeSlot);
        else if (newCampusName.equals("Kirkland")) {
            String sMessage = "3_" + studentId + "_" + newCampusName + "_" + newRoomNo + "_" + bookingDate + "_" + newTimeSlot;
            sBookingStatus = otherServerUDP(9876, sMessage);
        } else if (newCampusName.equals("Dorval")) {
            String sMessage = "3_" + studentId + "_" + newCampusName + "_" + newRoomNo + "_" + bookingDate + "_" + newTimeSlot;
            sBookingStatus = otherServerUDP(9878, sMessage);
        }

        if (bookingId.charAt(0) == 'W' && sBookingStatus != null && sBookingStatus.contains("Room Booked."))
            sBookingCancelStatus = cancelBookingWestmount(studentId + bookingId);
        else if (sBookingStatus.contains("Room Booked.")) {
            String sMessage = "7_" + bookingId + "_" + studentId;
            if (bookingId.charAt(0) == 'K')
                sBookingCancelStatus = otherServerUDP(9876, sMessage);
            else if (bookingId.charAt(0) == 'D')
                sBookingCancelStatus = otherServerUDP(9878, sMessage);
        }

        if (sBookingCancelStatus != null && sBookingCancelStatus.equals("Booking Cancelled"))
            sStatus = "Booking Slot Transfer Successful" + sBookingStatus.substring(sBookingStatus.indexOf(':'));
        else if (sBookingCancelStatus != null) {
            sStatus = "Booking Slot Transfer Un-Successful";
            String sNewBookingId = sBookingStatus.substring(sBookingStatus.indexOf(':') + 2);
            if (sNewBookingId.charAt(0) == 'W')
                sBookingCancelStatus = cancelBookingWestmount(studentId + sNewBookingId);
            else {
                String sMessage = "7_" + sNewBookingId + "_" + studentId;
                if (sNewBookingId.charAt(0) == 'K')
                    sBookingCancelStatus = otherServerUDP(9876, sMessage);
                else if (sNewBookingId.charAt(0) == 'D')
                    sBookingCancelStatus = otherServerUDP(9878, sMessage);
            }
        } else
            sStatus = "Booking Slot Transfer Un-Successful";
        ;

        return sStatus;
    }
}
