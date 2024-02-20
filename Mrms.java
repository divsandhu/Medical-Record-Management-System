package mrms;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;

public class Mrms extends JFrame implements ActionListener {
    private static final String url = "jdbc:mysql://localhost:3306/hospital";
    private static final String username = "root";
    private static final String password = "root";

    private JButton addPatientButton, viewPatientsButton, viewDoctorsButton, bookAppointmentButton, viewAppointmentsButton, exitButton;
    private Patient patient;
    private Doctor doctor;

    public Mrms() {
        // Set up the JFrame properties
        setTitle("MEDICAL RECORD MANAGEMENT SYSTEM");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize instances
        patient = new Patient(getConnection(), new Scanner(System.in));
        doctor = new Doctor(getConnection());

        // Create the components
        addPatientButton = new JButton("Add Patient");
        viewPatientsButton = new JButton("View Patients");
        viewDoctorsButton = new JButton("View Doctors");
        bookAppointmentButton = new JButton("Book Appointment");
        viewAppointmentsButton = new JButton("View Appointments");
        exitButton = new JButton("Exit");

        // Add action listeners for the buttons
        addPatientButton.addActionListener(this);
        viewPatientsButton.addActionListener(this);
        viewDoctorsButton.addActionListener(this);
        bookAppointmentButton.addActionListener(this);
        viewAppointmentsButton.addActionListener(this);
        exitButton.addActionListener(this);

        // Set the layout to GridLayout
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Add the components to the JFrame
        add(addPatientButton);
        add(viewPatientsButton);
        add(viewDoctorsButton);
        add(bookAppointmentButton);
        add(viewAppointmentsButton);
        add(exitButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addPatientButton) {
            // Add Patient
            patient.addPatient();
        } else if (e.getSource() == viewPatientsButton) {
            // View Patient
            patient.viewPatients();
        } else if (e.getSource() == viewDoctorsButton) {
            // View Doctors
            doctor.viewDoctors();
        } else if (e.getSource() == bookAppointmentButton) {
            // Book Appointment
            bookAppointment(patient, doctor, getConnection(), new Scanner(System.in));
        } else if (e.getSource() == viewAppointmentsButton) {
            // View Appointments
            viewAppointments(patient, doctor, getConnection());
        } else if (e.getSource() == exitButton) {
            System.out.println("THANK YOU! FOR USING MEDICAL RECORD MANAGEMENT SYSTEM!!");
            System.exit(0);
        }
    }
    public static void viewAppointments(Patient patient, Doctor doctor, Connection connection)
    {
        try{
            String checkquery = "select * from appointments";
            PreparedStatement preparedStatement1 = connection.prepareStatement(checkquery);
            ResultSet resultSet = preparedStatement1.executeQuery();
            System.out.println("Appointments : ");
            System.out.println("+----------------+--------------+-----------+------------------+");
            System.out.println("| Appointment No | patient Id   | Doctor Id | Appointment Date |");
            System.out.println("+----------------+--------------+-----------+------------------+");
            while(resultSet.next()){
                int id = resultSet.getInt("id");
                String patient_id = resultSet.getString("patient_id");
                String doctor_id = resultSet.getString("doctor_id");
                String appointment_date = resultSet.getString("appointment_date");
                System.out.printf("| %-17s | %-13s | %-11s | %-17s |\n", id, patient_id, doctor_id,appointment_date);
                System.out.println("+-----------+---------------+-----------+------------------+");
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
    public static void bookAppointment(Patient patient, Doctor doctor, Connection connection, Scanner scanner){
        System.out.print("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        System.out.print("Enter Doctor Id: ");
        int doctorId = scanner.nextInt();
        System.out.print("Enter appointment date (YYYY-MM-DD): ");
        String appointmentDate = scanner.next();
        if(patient.getPatientById(patientId) && doctor.getDoctorById(doctorId)){
            if(checkDoctorAvailability(doctorId, appointmentDate, connection)){
                String appointmentQuery = "INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)";
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(appointmentQuery);
                    preparedStatement.setInt(1, patientId);
                    preparedStatement.setInt(2, doctorId);
                    preparedStatement.setString(3, appointmentDate);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if(rowsAffected>0){
                        System.out.println("Appointment Booked!");
                    }else{
                        System.out.println("Failed to Book Appointment!");
                    }
                }
                catch (SQLException e){
                    e.printStackTrace();
                }
            }else{
                System.out.println("Doctor not available on this date!!");
            }
        }else{
            System.out.println("Either doctor or patient doesn't exist!!!");
        }
    }
    public static boolean checkDoctorAvailability(int doctorId, String appointmentDate, Connection connection){
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                int count = resultSet.getInt(1);
                if(count==0){
                    return true;
                }else{
                    return false;
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Mrms gui = new Mrms();
        gui.setVisible(true);
    }
}
