import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import servers.CampusServerInterface;

public class StudentClient {
    public static void main(String args[]) {

        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);

        String sStudentId, sChoice, sBookingId, sServerName = null,
                sDate = null, sTimeSlot = null, sStatus = null, sRoomNo;

        BufferedWriter output = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date d1 = new Date();

        try {
            System.out.println("Please enter your student ID");
            sStudentId = br.readLine();
            FileWriter file = new FileWriter("./Logs/StudentClient_" + sStudentId + ".txt", true);
            output = new BufferedWriter(file);

            output.append("Student ID:" + sStudentId + System.lineSeparator());

            if (sStudentId.length() != 8) {
                sStatus = "Invalid Student ID";
            }

            try {
                int i = Integer.parseInt(sStudentId.substring(4));
            } catch (Exception e) {
                sStatus = "Invalid Student ID";
            }

            QName qname;
            Service service;
            String wsdlPath, serverImpl, serverImplPort, nameSpaceURI = "http://server/";

            if (sStudentId.substring(0, 4).equals("DVLS")) {
                sServerName = "Dorval";
                wsdlPath = "http://localhost:9897/CampusServerInterface?wsdl";
                serverImpl = "DorvalServerImplService";
                serverImplPort = "DorvalServerImplPort";
            } else if (sStudentId.substring(0, 4).equals("KKLS")) {
                wsdlPath = "http://localhost:9898/CampusServerInterface?wsdl";
                serverImpl = "KirklandServerImplService";
                serverImplPort = "KirklandServerImplPort";

            } else if (sStudentId.substring(0, 4).equals("WSTS")) {
                wsdlPath = "http://localhost:9899/CampusServerInterface?wsdl";
                serverImpl = "WestmountServerImplService";
                serverImplPort = "WestmountServerImplPort";
            } else {
                output.append(dateFormat.format(d1) + "Invalid Admin ID" + System.lineSeparator());
                System.out.println("Invalid Admin ID");
                output.append(System.lineSeparator());
                output.close();
                return;
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

            System.out.println("what operation do you want to perform");
            System.out.println("Press 1-To book the room");
            System.out.println("Press 2-To get the information about available time slot");
            System.out.println("Press 3-To cancel the booking");
            System.out.println("Press 4-To change Booking");

            sChoice = br.readLine();

            if (sChoice.equals("1")) {
                System.out.println("Please Enter Server Name");
                sServerName = br.readLine();
                System.out.println("Please Enter Room Number");
                sRoomNo = br.readLine();
                System.out.println("Please Enter Date(DD-MON-YYYY Format)");
                sDate = br.readLine();
                System.out.println("Please Enter Time Slot(HH:MM-HH:MM format)");
                sTimeSlot = br.readLine();

                output.append(dateFormat.format(d1) + " Book Room" + System.lineSeparator());
                output.append("Server Name:" + sServerName + System.lineSeparator());
                output.append("Booking Room:" + sRoomNo + System.lineSeparator());
                output.append("Booking Date:" + sDate + System.lineSeparator());
                output.append("Booking Time:" + sTimeSlot + System.lineSeparator());

                sStatus = cs.bookRoom(sStudentId, sServerName, sRoomNo, sDate, sTimeSlot);
            } else if (sChoice.equals("2")) //to know about available Time Slots
            {
                System.out.println("Please Enter the Date(DD-MON-YYYY Format)");
                sDate = br.readLine();
                output.append(dateFormat.format(d1) + " Total Time Slots" + System.lineSeparator());
                output.append("Date:" + sDate + System.lineSeparator());

                sStatus = cs.getAvailableTimeSlot(sServerName, sDate);

            } else if (sChoice.equals("3"))  //To Cancel a booking
            {
                System.out.println("Please Enter the Booking Id");
                sBookingId = br.readLine();

                output.append(dateFormat.format(d1) + " Cancel Booking" + System.lineSeparator());
                output.append("Server Name:" + sServerName + System.lineSeparator());
                output.append("Booking ID:" + sBookingId + System.lineSeparator());

                sStatus = cs.cancelBooking(sBookingId, sStudentId);

            } else if (sChoice.equals("4"))  //To change a booking
            {
                System.out.println("Please Enter the Booking Id");
                sBookingId = br.readLine();
                System.out.println("Please Enter New Server Name");
                sServerName = br.readLine();
                System.out.println("Please Enter New Room Number");
                sRoomNo = br.readLine();
                System.out.println("Please Enter New Date(DD-MON-YYYY Format)");
                sDate = br.readLine();
                System.out.println("Please Enter New Time Slot(HH:MM-HH:MM format)");
                sTimeSlot = br.readLine();

                output.append(dateFormat.format(d1) + " Change Booking" + System.lineSeparator());
                output.append("Old Booking ID:" + sBookingId + System.lineSeparator());
                output.append("New Server Name:" + sServerName + System.lineSeparator());
                output.append("New Booking Room:" + sRoomNo + System.lineSeparator());
                output.append("New Booking Date:" + sDate + System.lineSeparator());
                output.append("new Booking Time:" + sTimeSlot + System.lineSeparator());

                sStatus = cs.changeReservation(sBookingId, sServerName, sDate, sRoomNo, sTimeSlot, sStudentId);

            }

            System.out.println(sStatus);
            output.append(sStatus + System.lineSeparator());
            output.append(System.lineSeparator());
            output.close();

        } // end try
        catch (Exception e) {
            System.out.println("Exception in StudentClient: " + e);
        }
    }
}