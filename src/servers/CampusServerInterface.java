package servers;

import java.util.ArrayList;
import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface CampusServerInterface{

	@WebMethod
	String createRoom(String date, String room_Number, ArrayList<String> TimeSlots);
	
	@WebMethod
	String deleteRoom(String date, String room_Number, ArrayList<String> TimeSlots);
    
	@WebMethod
	String bookRoom(String sStudentId, String campusName, String roomNumber, String date, String timeslot);
    
	@WebMethod
	String getAvailableTimeSlot(String sServer, String date);

	@WebMethod
	String cancelBooking(String sBookingID, String sStudentId);

    @WebMethod
	String changeReservation(String studentId, String bookingId, String newCampusName, String newRoomNo, String bookingDate, String newTimeSlot);

}