DROP TABLE Users;
CREATE TABLE Users(
    email VARCHAR(50) PRIMARY KEY,
    nom VARCHAR(30) NOT NULL,
    prenom VARCHAR(30) NOT NULL,
    adresse VARCHAR(30) NOT NULL
);

INSERT INTO Users(email, nom, prenom) VALUES ('add1@mail.fr', 'Couston', 'Leila');
INSERT INTO Users(email, nom, prenom) VALUES ('add2@mail.fr', 'Leconte', 'Nathan');
INSERT INTO Users(email, nom, prenom) VALUES ('add3@mail.fr', 'Maabout', 'Lou');
INSERT INTO Users(email, nom, prenom) VALUES ('add4@mail.fr', 'Amelot', 'Victor');
INSERT INTO Users(email, nom, prenom) VALUES ('add5@mail.fr', 'Zeroual', 'Yasmine');

DROP TABLE Produits;

CREATE TABLE Produits(
    idProduit NUMBER PRIMARY KEY,
    nom VARCHAR(30) NOT NULL,
    prixRevient NUMBER NOT NULL,
    stock NUMBER NOT NULL,
    categorie VARCHAR(30) NOT NULL,
    DISPONIBLE NUMBER(1, 0) NOT NULL
);

INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (1, 'Chaussure', 10, 17, 'Soulier');
INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (2, 'Tong', 5, 45, 'Souliers');
INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (3, 'Crocs', 1000, 1; 'Souliers');
INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (4, 'Espadrille', 10, 10, 'Souliers');
INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (5, 'Botte', 30, 26, 'Souliers');

INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (6, 'Chapeau', 20, 123, 'Couvre-chef');
INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (7, 'Casquette', 18, 9, 'Couvre-chef');
INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (8, 'Bonnet', 12, 27, 'Couvre-chef');
INSERT INTO Produits(idProduit, nom, prixRevient, stock, categorie) VALUES (9, 'Chapka', 56, 3, 'Couvre-chef');

CREATE TABLE SalledeVente(
    idSalle NUMBER PRIMARY KEY,
    categorie VARCHAR(30),
    Duree NUMBER,
    Croissance NUMBER,
    Revocable NUMBER
);

CREATE TABLE CARACTERISTIQUES(
    nomCaracteristique VARCHAR(30) NOT NULL,
    idProduit NUMBER NOT NULL,
    valeurCaracteristique NUMBER,
    PRIMARY KEY(idProduit, nomCaracteristique),
    FOREIGN KEY (idProduit) REFERENCES Produits(idProduit)
);


CREATE TABLE VENTE(
    idVente INT NOT NULL PRIMARY KEY,
    idProduit INT NOT NULL,
    prixDepart INT,
    idSalle INT NOT NULL,
    FOREIGN KEY (idSalle) REFERENCES SalledeVente(idSalle),
    FOREIGN KEY (idProduit) REFERENCES Produits(idProduit)
);

CREATE TABLE OFFRE(
    DateOffre NUMBER(22) NOT NULL,
    Email VARCHAR(50) NOT NULL, 
    IdVente NUMBER NOT NULL, 
    Prix NUMBER,
    Quantite NUMBER NOT NULL, 
    "TIME" NUMBER(22),
    FOREIGN KEY (Email) REFERENCES Users(Email),
    FOREIGN KEY (idVente) REFERENCES VENTE(idVente),
    PRIMARY KEY (DateOffre, Email, IdVente)
);