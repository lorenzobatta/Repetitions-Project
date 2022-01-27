import tkinter as tk
from tkinter import filedialog, Text, END, font
import requests
import json
from Corso import Corso
from Docente import Docente
from Prenotazione import PrenotazioneNoUser

from Utente import Utente

# ------VARIABILI GLOBALI -----------
# currentFrame: corsi, docenti, login, Le mie prenotazioni, Le mie prenotazioni disdette,
# Le mie ripetizioni effettuate, Ripetizioni Disponibili
#

logged = False
listItems = []
currentUtente = Utente(-1, "", "", "")
currentCorso = Corso(-1, "")
currentDocente = Docente(-1, "", "")
currentPrenotazione = None


# ------FINE VARIABILI GLOBALI ------


def open_login_frame():
    global currentFrame
    currentFrame.set("login")
    login_frame.place(relwidth=1, relheight=1)
    login_button.configure(text="Login", command=doLogin)
    open_button.pack_forget()
    indietro_button.configure(command=indietro_button_clicked)


def doLogin():
    password = password_text_area.get(1.0, 'end-1c') # end-1c perchè la end considera un carattere di spazio bianco e con -1c lo togliamo
    email = account_text_area.get(1.0, 'end-1c')
    parameters = {"username": email, "password": password, "action": "login"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        user = (json.loads(response.text)["loginControl"])
        print(user)
        global currentUtente
        currentUtente = Utente(user['utente']['id'], user['utente']['account'], user['utente']['password'], user['utente']['role'], )
        lista.place(relwidth=1, relheight=0.8, relx=0, rely=0.1)
        frame.place(relwidth=1, relheight=1)
        login_frame.place_forget()
        login_button.configure(text="Logout", command=doLogout)
        global currentFrame
        currentFrame.set("docenti")
        global logged
        logged = True
        my_prenotations_button.pack(side=tk.LEFT, padx=3)
        prenotations_disdette_button.pack(side=tk.LEFT, padx=3)
        ripetizioni_effettuate_button.pack(side=tk.LEFT, padx=3)
        open_button.pack(side=tk.LEFT, padx=3)
        indietro_button_clicked()

def doLogout():
    global currentUtente
    global currentFrame
    currentUtente = Utente(-1, "", "", "")
    global logged
    logged = False
    login_button.configure(text="Apri schermata Login", command=open_login_frame)
    prenota_button.pack_forget()
    my_prenotations_button.pack_forget()
    ripetizioni_effettuate_button.pack_forget()
    prenotations_disdette_button.pack_forget()
    segna_prenotazione_disdetta_button.pack_forget()
    segna_ripetizione_effettuata_button.pack_forget()
    currentFrame.set("docenti")
    indietro_button_clicked()


def indietro_button_clicked():
    global currentFrame
    if currentFrame.get().__eq__("login") | currentFrame.get().__eq__("docenti"):
        if currentFrame.get().__eq__("login"):
            login_frame.place_forget()
            login_button.configure(text="Apri schermata Login", command=open_login_frame)
            open_button.pack(side=tk.LEFT, padx=3)
        open_button.pack(side=tk.LEFT, padx=3)

        parameters = {"action": "corsi"}
        response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
        category = (json.loads(response.text)["data"])
        print("Indietro clicked")
        print(category)
        lista.delete(0, lista.size())
        listItems.clear()
        for c in category:
            corso = Corso(c['id'], c['title'])
            lista.insert(END, corso.title)
            listItems.append(corso)
        open_button.configure(text="Apri", command=open_course_clicked)
        currentFrame.set("corsi")

    elif currentFrame.get().__eq__("Ripetizioni Disponibili"):
        parameters = {"corso": currentCorso, "action": "insegnanti"}
        response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
        if (json.loads(response.text)["status"]) == 200:
            category = (json.loads(response.text)["data"])
            print(category)
            lista.delete(0, lista.size())
            listItems.clear()
            for c in category:
                doc = Docente(c['id'], c['name'], c['surname'])
                lista.insert(END, doc)
                listItems.append(doc)
            open_button.pack(side=tk.LEFT, padx=3)
            open_button.configure(text="Apri", command=show_ripetizioni_disponibili)
            prenota_button.pack_forget()
            currentFrame.set("docenti")


    elif currentFrame.get().__eq__("Le mie prenotazioni") | currentFrame.get().__eq__("Le mie prenotazioni disdette") | currentFrame.get().__eq__("Le mie ripetizioni effettuate"):
        currentFrame.set("docenti")
        segna_prenotazione_disdetta_button.pack_forget()
        segna_ripetizione_effettuata_button.pack_forget()
        indietro_button_clicked()


def open_course_clicked():
    course_position = lista.curselection()
    global currentCorso
    currentCorso = listItems.__getitem__(lista.index(course_position))
    parameters = {"corso": currentCorso, "action": "insegnanti"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        category = (json.loads(response.text)["data"])
        print(category)
        lista.delete(0, lista.size())
        listItems.clear()
        for c in category:
            doc = Docente(c['id'], c['name'], c['surname'])
            lista.insert(END, doc)
            listItems.append(doc)
        open_button.configure(text="Apri", command=show_ripetizioni_disponibili)
        global currentFrame
        currentFrame.set("docenti")


def show_ripetizioni_disponibili():
    docente_position = lista.curselection()
    global currentDocente
    currentDocente = listItems.__getitem__(lista.index(docente_position))
    parameters = {"corso": currentCorso, "docente": currentDocente.get_surname(), "action": "getRipetizioniDisponibili"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        category = (json.loads(response.text)["data"])
        lista.delete(0, lista.size())
        listItems.clear()
        for c in category:
            corsoTemp = Corso(c['corso']['id'],c['corso']['title'])
            docenteTemp = Docente(c['docente']['id'], c['docente']['name'], c['docente']['surname'])
            prenotazione = PrenotazioneNoUser(corsoTemp, docenteTemp, c['giorno'], c['slot'])
            lista.insert(END, prenotazione)
            listItems.append(prenotazione)

        global currentFrame
        currentFrame.set("Ripetizioni Disponibili")
        global logged
        if logged:
            prenota_button.pack(side=tk.LEFT, padx=3)
        else:
            prenota_button.pack_forget()

        open_button.pack_forget()


def prenota_ripetizione():
    global currentCorso
    global currentDocente
    global currentUtente
    global currentPrenotazione
    prenotazione_position = lista.curselection()
    currentPrenotazione = listItems.__getitem__(lista.index(prenotazione_position))

    parameters = {"corso": currentCorso.id, "docente": currentDocente.id,"utente": currentUtente.id,"giorno": currentPrenotazione.giorno,"slot": currentPrenotazione.slot,"action": "prenotaRipetizioneForAndroid"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        lista.delete(lista.index(prenotazione_position))
        listItems.pop(lista.index(prenotazione_position))


def show_my_prenotations():
    parameters = {"userId": currentUtente.id, "action": "getMyActivePrenotationForAndroid"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        category = (json.loads(response.text)["data"])
        lista.delete(0, lista.size())
        listItems.clear()
        for c in category:
            corsoTemp = Corso(c['corso']['id'], c['corso']['title'])
            docenteTemp = Docente(c['docente']['id'], c['docente']['name'], c['docente']['surname'])
            prenotazione = PrenotazioneNoUser(corsoTemp, docenteTemp, c['giorno'], c['slot'])
            lista.insert(END, prenotazione)
            listItems.append(prenotazione)
        global currentFrame
        currentFrame.set("Le mie prenotazioni")
        prenota_button.pack_forget()
        segna_prenotazione_disdetta_button.pack(side=tk.LEFT, padx=3)
        segna_ripetizione_effettuata_button.pack(side=tk.LEFT, padx=3)
        open_button.pack_forget()


def show_prenotazioni_disdette():
    parameters = {"userId": currentUtente.id, "action": "getMyPrenotazioniDisdetteForAndroid"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        category = (json.loads(response.text)["data"])
        lista.delete(0, lista.size())
        listItems.clear()
        for c in category:
            corsoTemp = Corso(c['corso']['id'],c['corso']['title'])
            docenteTemp = Docente(c['docente']['id'], c['docente']['name'], c['docente']['surname'])
            prenotazione = PrenotazioneNoUser(corsoTemp, docenteTemp, c['giorno'], c['slot'])
            lista.insert(END, prenotazione)
            listItems.append(prenotazione)
        global currentFrame
        currentFrame.set("Le mie prenotazioni disdette")
        prenota_button.pack_forget()
        segna_prenotazione_disdetta_button.pack_forget()
        segna_ripetizione_effettuata_button.pack_forget()
        open_button.pack_forget()

def show_prenotazioni_effettuate():
    parameters = {"userId": currentUtente.id, "action": "getMyPrenotazioniEffettuateForAndroid"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        category = (json.loads(response.text)["data"])
        lista.delete(0, lista.size())
        listItems.clear()
        for c in category:
            corsoTemp = Corso(c['corso']['id'],c['corso']['title'])
            docenteTemp = Docente(c['docente']['id'], c['docente']['name'], c['docente']['surname'])
            prenotazione = PrenotazioneNoUser(corsoTemp, docenteTemp, c['giorno'], c['slot'])
            lista.insert(END, prenotazione)
            listItems.append(prenotazione)
        global currentFrame
        currentFrame.set("Le mie ripetizioni effettuate")
        prenota_button.pack_forget()
        segna_prenotazione_disdetta_button.pack_forget()
        segna_ripetizione_effettuata_button.pack_forget()
        open_button.pack_forget()


def segna_ripetizione_effettuata():
    global currentCorso
    global currentDocente
    global currentUtente
    global currentPrenotazione
    prenotazione_position = lista.curselection()
    currentPrenotazione = listItems.__getitem__(lista.index(prenotazione_position))

    parameters = {"corso": currentCorso.id, "docente": currentDocente.id,"utente": currentUtente.id,
                  "giorno": currentPrenotazione.giorno,"slot": currentPrenotazione.slot,
                  "action": "segnaPrenotazioneEffettuataForAndroid"}

    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        lista.delete(lista.index(prenotazione_position))
        listItems.pop(lista.index(prenotazione_position))


def segna_ripetizione_disdetta():
    global currentCorso
    global currentDocente
    global currentUtente
    global currentPrenotazione
    prenotazione_position = lista.curselection()
    currentPrenotazione = listItems.__getitem__(lista.index(prenotazione_position))

    parameters = {"corso": currentCorso.id, "docente": currentDocente.id, "utente": currentUtente.id,
                  "giorno": currentPrenotazione.giorno, "slot": currentPrenotazione.slot,
                  "action": "disdiciPrenotazioneForAndroid"}
    response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
    if (json.loads(response.text)["status"]) == 200:
        lista.delete(lista.index(prenotazione_position))
        listItems.pop(lista.index(prenotazione_position))


root = tk.Tk()
root.title("Ripetizioni")


canvas = tk.Canvas(root, height=350, width=900, bg="#263D42")
canvas.pack()

frame = tk.Frame(canvas, bg="#1798bf")
frame.place(relwidth=1, relheight=1)

currentFrame = tk.StringVar()
currentFrame.set("corsi")



label_page = tk.Label(frame, textvariable=currentFrame, font=font.Font(size=12))
label_page.pack(pady=3, side=tk.TOP)

login_frame = tk.Frame(canvas, bg="#1798bf")
login_frame.place(relwidth=1, relheight=1)
login_frame.place_forget()


account_label = tk.Label(login_frame, text="Account")
account_label.pack(pady=3)
account_text_area = tk.Text(login_frame, height="1", width="15")
account_text_area.pack(pady=3)

password_label = tk.Label(login_frame,text="Password")
password_label.pack(pady=3)
password_text_area = tk.Text(login_frame, height="1", width="15")
password_text_area.pack(pady=3)

# ---- INIZIO BOTTONI ----

button_frame = tk.Frame(root)
button_frame.pack(side=tk.BOTTOM,pady=2)

indietro_button = tk.Button(button_frame, text="Indietro", command=indietro_button_clicked)
indietro_button.pack(side=tk.LEFT, padx=3)

login_button = tk.Button(button_frame,text="Apri schermata Login",command=open_login_frame)
login_button.pack(side=tk.LEFT, padx=3)

open_button = tk.Button(button_frame, text="Apri Corso", command=open_course_clicked)
open_button.pack(side=tk.LEFT, padx=3)

prenota_button = tk.Button(button_frame,text="Prenota",command=prenota_ripetizione)
prenota_button.pack(side=tk.LEFT, padx=3)
prenota_button.pack_forget()

my_prenotations_button = tk.Button(button_frame,text="Le mie Prenotazioni", command=show_my_prenotations)
my_prenotations_button.pack(side=tk.LEFT, padx=3)
my_prenotations_button.pack_forget()

prenotations_disdette_button = tk.Button(button_frame,text="Prenotazioni Disdette", command=show_prenotazioni_disdette)
prenotations_disdette_button.pack(side=tk.LEFT, padx=3)
prenotations_disdette_button.pack_forget()

ripetizioni_effettuate_button = tk.Button(button_frame, text="Ripetizioni Effettuate", command=show_prenotazioni_effettuate)
ripetizioni_effettuate_button.pack(side=tk.LEFT, padx=3)
ripetizioni_effettuate_button.pack_forget()

segna_ripetizione_effettuata_button = tk.Button(button_frame,text="Segna Ripetizione Effettuata", command=segna_ripetizione_effettuata)
segna_ripetizione_effettuata_button.pack(side=tk.LEFT, padx=3)
segna_ripetizione_effettuata_button.pack_forget()

segna_prenotazione_disdetta_button = tk.Button(button_frame,text="Segna Prenotazione Disdetta", command=segna_ripetizione_disdetta)
segna_prenotazione_disdetta_button.pack(side=tk.LEFT, padx=3)
segna_prenotazione_disdetta_button.pack_forget()



# ---- FINE BOTTONI ----
font_size = font.Font(size=15)
lista = tk.Listbox(frame, bg="#bcc6cc",font=font_size)
lista.place(relwidth=1, relheight=0.8, relx=0, rely=0.1)

parameters = {"action": "corsi"}
response = requests.get('http://localhost:8080/Ripetizioni/ServletRipetizioni', params=parameters)
category = (json.loads(response.text)["data"])
print(category)
for c in category:
    corso = Corso(c['id'], c['title'])
    listItems.append(corso)
    lista.insert(END, corso)



root.mainloop()