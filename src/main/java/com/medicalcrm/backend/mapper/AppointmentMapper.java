package com.medicalcrm.backend.mapper;

import com.medicalcrm.backend.dto.request.CreateAppointmentRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentNotesRequest;
import com.medicalcrm.backend.dto.request.UpdateAppointmentScheduleRequest;
import com.medicalcrm.backend.dto.response.AppointmentResponse;
import com.medicalcrm.backend.model.*;

public class AppointmentMapper {

    public static AppointmentResponse toResponse(Appointment appointment) {

        AppointmentResponse response = new AppointmentResponse();

        response.setId(appointment.getId());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        response.setNotes(appointment.getNotes());

        response.setPatientId(appointment.getPatient().getId());
        response.setDoctorId(appointment.getDoctor().getId());
        response.setServiceId(appointment.getService().getId());
        response.setPatientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        response.setServiceName(appointment.getService().getName());

        return response;
    }


    public static Appointment toEntity(CreateAppointmentRequest request,
                                       Patient patient,
                                       Doctor doctor,
                                       MedicalService service) {

        Appointment appointment = new Appointment();

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setService(service);

        return appointment;
    }

    public static void updateNotes(Appointment appointment,
                                   UpdateAppointmentNotesRequest request) {

        appointment.setNotes(request.getNotes());
    }

    public static void updateSchedule(Appointment appointment,
                                      UpdateAppointmentScheduleRequest request) {

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
    }

}
