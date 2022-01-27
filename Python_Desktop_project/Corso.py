class Corso:
    def __init__(self, id, title):
        self.id = id
        self.title = title

    def __str__(self):
        return self.title

    def get_id(self):
        return self.id
