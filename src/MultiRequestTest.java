import java.util.ArrayList;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import servers.CampusServerInterface;

public class MultiRequestTest extends Thread {
    int iRunNo;

    MultiRequestTest(int iRunNo) {
        this.iRunNo = iRunNo;
    }

    public void run() {
        try {
            QName qname;
            Service service;
            URL url = new URL("http://localhost:9897/CampusServerInterface?wsdl");
            qname = new QName("http://server/", "DorvalServerImplService");
            service = Service.create(url, qname);
            qname = new QName("http://server/", "DorvalServerImplPort");
            CampusServerInterface cs = service.getPort(qname, CampusServerInterface.class);
            ArrayList<String> sTimeSlots = new ArrayList<String>();//Creating arraylist
            sTimeSlots.add(0, "07-08");
            sTimeSlots.add(0, "08-09");
            String sDate = "01-JAN-2017";
            String sRoomNo = "1";

            String sStatus = cs.createRoom(sDate, sRoomNo, sTimeSlots);
            System.out.println(sStatus);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String args[]) {
        MultiRequestTest s1 = new MultiRequestTest(1);
        MultiRequestTest s2 = new MultiRequestTest(2);

        s1.start();
        s2.start();
    }
}