# Système de Vente aux Enchères - Projet Bases de Données

## Introduction

Ce projet fait partie du cursus de deuxième année à l'Ensimag, visant à développer une application de gestion de ventes aux enchères pour la société fictive **Baie-électronique**. L'objectif est de mettre en œuvre des compétences en bases de données relationnelles et en programmation avec l'API JDBC en Java.

## Description de l'Application

L'application permet aux utilisateurs de :
- **Mettre en vente des produits** avec des caractéristiques spécifiques.
- **Enchérir ou acheter des produits** mis en vente par d'autres utilisateurs.
- Gérer des **salles de vente** organisées par catégories, chacune contenant une sélection de produits.

### Types de Ventes
- **Montantes** : Les enchères augmentent à chaque offre.
- **Descendantes** : Le prix diminue progressivement jusqu'à ce qu'une offre soit faite.
- Les ventes peuvent être :
  - **Révocables** : Annulées si le prix de revient n'est pas atteint.
  - **À durée limitée** : Avec une date et heure de fin précises.
  - **Autoriser plusieurs enchères** : Un même utilisateur peut enchérir plusieurs fois, sauf indication contraire.

## Travail Réalisé

Le projet est structuré en quatre étapes principales :

### 1. Modélisation du Problème
- Analyse des contraintes (fonctionnelles, de valeur, de multiplicité, contextuelles).
- Création d'un **schéma Entités/Associations (UML)**, avec justifications détaillées.
- Traduction en **schéma relationnel** avec précisions sur les formes normales.

### 2. Implantation de la Base de Données
- Création et mise en œuvre du schéma relationnel sur **Oracle**.
- Insertion de données pertinentes pour les tests.

### 3. Analyse des Fonctionnalités
- Définition des transactions SQL pour :
  - Création des salles de vente.
  - Gestion des enchères utilisateurs.
  - Détermination des gagnants, selon les règles d'enchères.

### 4. Démonstrateur Java
- Développement d'une application Java avec **JDBC**.
- Interface en ligne de commande pour l'interaction avec la base de données.

## Technologies Utilisées
- **Java** : Programmation de l'application avec l'API JDBC.
- **Oracle** : Système de gestion de bases de données pour les tables et transactions.
- **Outils de Modélisation** : Dia pour les schémas UML, SQL*Plus pour tester les requêtes.

## Installation et Utilisation

1. **Configuration de la Base de Données** :
   - Exécuter les scripts SQL pour créer et peupler la base de données sur le serveur Oracle.
2. **Lancer l'Application Java** :
   - Compiler et exécuter l'application depuis un terminal ou un IDE.
3. **Test des Fonctionnalités** :
   - Vérifier les différents scénarios d'enchères et de gestion des ventes.

## Documentation

- L'analyse des contraintes, les schémas UML, la conception relationnelle, et les requêtes SQL sont détaillés dans la documentation fournie.
- Un guide d'utilisation rapide de l'application est également inclus.

## Organisation du Projet
- Projet réalisé en équipe de cinq, avec 15 heures de séances encadrées.
- Suivi intermédiaire et soutenance finale pour évaluer le travail effectué.

---

