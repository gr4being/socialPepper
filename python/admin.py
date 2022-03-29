import os
import json
import pprint

question_fp = "questions.txt"

print("############ Willkommen zur Admin-Anwendung! #############\n")

action = "f"

with open(question_fp, "r") as file:
    parsed = json.load(file)

while not action == "v":
    print("Datei Aktuell:")
    pprint.pprint(parsed)
    action = input("Frage Hinzufügen?(h) Frage Entfernen?(e) Frage Bearbeiten?(b) Anwendung Verlassen?(v) ")
    if action == "f":
        q = input("\nFragentext eingeben: ")
        a = input("\nAntworttext eingeben: ")
        avg_c = int(sum([q["count"] for q in parsed["questions"]])/(len(parsed["questions"])))
        parsed["questions"].append({"count": max(avg_c,1), "question": q, "answer": a, "keyweights": []})

        print("\nWähle die relevanten Keywords für die Frage (y/n):")
        for kw in parsed["keywords"]:
            if input("\t"+kw+": ") == "y":
                parsed["questions"][-1]["keyweights"].append(1)
            else:
                parsed["questions"][-1]["keyweights"].append(0)
                
        if input("\nNeue Keywords zur Frage hinzufügen? (y/n)") == "y":
            
            k = input("\nKeyword (Enter zum Abschließen): ")
            while not k == "":
                parsed["keywords"].append(k)
                parsed["questions"][-1]["keyweights"].append(1)
                for q in parsed["questions"][:-1]:
                    q["keyweights"].append(0)
                k = input("Keyword (Enter zum Abschließen): ")

    elif action == "e":
        text = input("Füge den Text der zu löschenden Frage ein: ")
        try:
            q = [q for q in parsed["questions"] if q["question"] == text][0]
            parsed["questions"].pop(parsed["questions"].index(q))
            print("Frage erfolgreich entfernt!")
        except:
            print("Frage nicht gefunden!")
    
    elif action == b:
        text = input("Füge den Text der zu bearbeitenden Frage ein: ")
        try:
            q = [q for q in parsed["questions"] if q["question"] == text][0]
            idx = parsed["questions"].index(q)
        except:
            q = None
            print("Frage nicht gefunden!")
            
        if q is not None:
            print("Alter Fragentext: "+q["question"])
            q = input("\nNeuen Fragentext eingeben (Enter für Beibehalten): ")
            print("Alter Antworttext: "+q["answer"])
            a = input("\nNeuen Antworttext eingeben (Enter für Beibehalten): ")

            print("Alte Keyweights: "+q["keyweights"])

                
            
        with open(question_fp, "w") as file:
            json.dump(parsed, file)

        print("\nFrage erfolgreich hinzugefügt!\n\n")
