class Docente:
    def __init__(self, id, name, surname):
        self.id = id
        self.name = name
        self.surname = surname

    def __str__(self):
        return self.surname + " " + self.name

    def get_surname(self):
        return self.surname
