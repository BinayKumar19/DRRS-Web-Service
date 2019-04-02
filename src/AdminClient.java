import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import servers.CampusServerInterface;


public class AdminClient {

    InputStreamReader is;
    BufferedReader br;
    LogWriter lw;

    AdminClient() {
        is = new InputStreamReader(System.in);
        br = new BufferedReader(is);
    }

    private String readLine(String messageToDisplay) {
        String line = null;

        System.out.println(messageToDisplay);
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    private String readLine() {
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static void main(String args[]) {

        String adminId, status;
        AdminClient client = new AdminClient();

        adminId = client.readLine("Please enter your Admin ID");
        client.lw = new LogWriter("AdminClient_" + adminId + ".txt", true);
        client.lw.writeToLog("Admin ID:" + adminId + System.lineSeparator());

        if (adminId.length() != 8) {
            status = "Invalid Admin ID";
            System.out.println(status);
            return;
        }

        try {
            int i = Integer.parseInt(adminId.substring(4));
        } catch (Exception e) {
            status = "Invalid Admin ID";
            System.out.println(status);
            return;
        }

        status = client.performOperation(adminId);
        System.out.println(status);
        client.lw.writeToLog(status + System.lineSeparator() + System.lineSeparator());
        client.lw.closeLogWriter();

    }

    private String performOperation(String adminId) {
        String operationChoice, status = null, registryURL = null, date = null;
        int noTimeSlots;
        String roomNo;
        ArrayList<String> timeSlotsArray = new ArrayList<>();

        CampusServerInterface cs = getServerName(adminId);

        System.out.println("what operation do you want to perform");
        System.out.println("Press 1-To create a room");
        System.out.println("2-to delete the room");
        operationChoice = readLine();

        roomNo = readLine("Please Enter Room Number:");
        date = readLine("Please Enter Date(DD-MON-YYYY Format):");
        noTimeSlots = Integer.parseInt(readLine("How many Time slot do you want to enter?"));

        lw.writeToLog("Server:" + registryURL + System.lineSeparator());
        lw.writeToLog("Date:" + date + System.lineSeparator());
        lw.writeToLog("Room:" + roomNo + System.lineSeparator());

        System.out.println("Please Enter Time Slot(HH:MM-HH:MM format):");
        for (int i = 0; i < noTimeSlots; i++) {
            timeSlotsArray.add(readLine(i + ": "));
            lw.writeToLog("TimeSlot:" + timeSlotsArray.get(i) + System.lineSeparator());
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d1 = new Date();

        if (operationChoice.equals("1"))  //To Create the Room
        {
            lw.writeToLog(dateFormat.format(d1) + " Create Room" + System.lineSeparator());
            status = createRoom(cs, roomNo, date, timeSlotsArray);
        } else if (operationChoice.equals("2")) //To Delete the Room
        {
            lw.writeToLog(dateFormat.format(d1) + " Delete Room" + System.lineSeparator());
            status = deleteRoom(cs, roomNo, date, timeSlotsArray);
        }

        return status;
    }

    private String createRoom(CampusServerInterface cs, String roomNo, String date, ArrayList<String> timeSlots) {
        String status = null;

        // invoke the remote methods
        try {
            status = cs.createRoom(date, roomNo, timeSlots);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    private String deleteRoom(CampusServerInterface cs, String roomNo, String date, ArrayList<String> timeSlots) {
        String status = null;

        try {
            status = cs.deleteRoom(date, roomNo, timeSlots);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }

    private CampusServerInterface getServerName(String adminId) {
        QName qname;
        Service service;
        String wsdlPath, serverImpl, serverImplPort, nameSpaceURI = "http://server/";

        if (adminId.substring(0, 4).equals("DVLA")) {
            wsdlPath = "http://localhost:9897/CampusServerInterface?wsdl";
            serverImpl = "DorvalServerImplService";
            serverImplPort = "DorvalServerImplPort";
        } else if (adminId.substring(0, 4).equals("KKLA")) {
            wsdlPath = "http://localhost:9898/CampusServerInterface?wsdl";
            serverImpl = "KirklandServerImplService";
            serverImplPort = "KirklandServerImplPort";

        } else if (adminId.substring(0, 4).equals("WSTA")) {
            wsdlPath = "http://localhost:9899/CampusServerInterface?wsdl";
            serverImpl = "WestmountServerImplService";
            serverImplPort = "WestmountServerImplPort";
        } else {
            lw.writeToLog("Admin Id Incorrect" + System.lineSeparator());
            lw.writeToLog(System.lineSeparator());
            lw.closeLogWriter();
            System.out.println("Admin Id Incorrect");
            return null;
        }

        URL url = null;
        try {
            url = new URL(wsdlPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        qname = new QName(nameSpaceURI, serverImpl);
        service = Service.create(url, qname);
        qname = new QName(nameSpaceURI, serverImplPort);

        CampusServerInterface cs = service.getPort(qname, CampusServerInterface.class);
        return cs;
    }

}