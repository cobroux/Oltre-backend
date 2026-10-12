package io.oltre_backend.appointment;

// MANUAL : créé/édité par l'utilisateur dans le calendrier.
// GARMIN : généré automatiquement à partir d'une activité Garmin synchronisée.
// COACH_PLAN : généré automatiquement à partir du plan hebdomadaire du Coach IA.
//
// Un rendez-vous GARMIN/COACH_PLAN modifié par l'utilisateur repasse en MANUAL
// (voir appointmentService.update) : une fois édité, il devient "le sien" et
// les prochaines synchronisations ne l'écrasent plus.
public enum AppointmentSource {
    MANUAL, GARMIN, COACH_PLAN
}
