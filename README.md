# DRRS-Web-Service
Distributed Room Reservation system using Java Soap Web service

Distributed System assignment

Distributed Room Reservation System (DRRS) using Java RMI

A distributed room reservation system (DRRS) for a university with multiple campuses: a distributed system used by administrators who manage the availability information about the university’s rooms and students who can reserve the rooms across the university’s different campuses.

Consider three campus locations: Dorval-Campus (DVL), Kirkland-Campus (KKL) and Westmount-Campus (WST) for your implementation. The server for each campus must maintain a number of RoomRecords. A RoomRecord can be identified by a unique RecordID starting with RR (for RoomRecord) and ending with a 5 digits number (e.g. RR10000 for a RoomRecord).

A RoomRecord contains the following fields: • Date. • Room number. • List of available time (applies every day). • Booked_by (studentID if it’s booked by student, null otherwise).

Steps to run:
1) open project in an IDE (IntelliJ preferred)
2) Run all servers DorvalCampusServer.java, WestmountCampusServer and KirklandCampusServer
3) Run AdminClient.java to perform creation of rooms with booking time slots and other admin operations
4) Run StudentClient.java to perform Student operations

