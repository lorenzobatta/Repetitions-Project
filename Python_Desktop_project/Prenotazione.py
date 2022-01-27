class PrenotazioneNoUser:
    def __init__(self, corso, docente, giorno, slot):
        self.corso = corso
        self.docente = docente
        self.giorno = giorno
        self.slot = slot

    def __str__(self):
        return self.corso.title + " " +  self.docente.__str__() + " " + self.giorno + " " + self.slot
