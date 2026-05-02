# Rapport de projet - Jeu de reflexion (Memory)

## 1. Introduction

Dans le cadre du projet de developpement web JEE, notre binome a realise une application de jeu de memoire (Memory) nommee **Jeu Reflexion**.  
L'objectif principal est de proposer une experience ludique simple, avec gestion des comptes utilisateurs, suivi des scores et sauvegarde/reprise des parties.

Ce document presente :
- une description du jeu choisi,
- l'architecture detaillee de l'application developpee,
- les choix techniques,
- les forces et limites du systeme,
- des pistes d'amelioration.

---

## 2. Description du jeu choisi

Le jeu implemente est un **Memory** : le joueur doit retrouver des paires de cartes identiques en retournant deux cartes a la fois.

### 2.1 Regles principales

- Le plateau est compose de paires de cartes melangees aleatoirement.
- A chaque coup, le joueur retourne deux cartes.
- Si les cartes sont identiques, elles restent visibles.
- Sinon, elles sont remises face cachee, et le compteur d'erreurs augmente.
- La partie est terminee lorsque toutes les paires sont trouvees.

### 2.2 Niveaux de difficulte

Le jeu propose trois niveaux :
- **EASY**
- **MEDIUM**
- **HARD**

Le niveau influence notamment le nombre de cartes a retrouver, et donc la difficulte globale de la partie.

### 2.3 Fonctionnalites utilisateur

- Creation de compte (inscription)
- Connexion / deconnexion
- Lancement d'une nouvelle partie
- Sauvegarde d'une partie en cours
- Reprise d'une sauvegarde
- Consultation des meilleurs scores

---

## 3. Technologies utilisees

L'application repose sur une architecture Java web classique autour de Spring et Thymeleaf.

- **Langage** : Java 17
- **Framework web** : Spring MVC 6
- **Moteur de templates** : Thymeleaf
- **Acces donnees** : Spring Data JPA + Hibernate
- **Base de donnees** : H2 (mode fichier)
- **Build** : Maven
- **Packaging** : WAR
- **Serveur cible** : Apache Tomcat 10

Fichier de build principal : `pom.xml`

---

## 4. Architecture detaillee de l'application

L'application adopte une architecture en couches, qui separe clairement les responsabilites.

## 4.1 Vue d'ensemble des couches

1. **Couche presentation (View)**
   - Pages HTML Thymeleaf
   - CSS/JS statiques
2. **Couche controle (Controller)**
   - Reception des requetes HTTP
   - Validation de session
   - Orchestration des appels metier
3. **Couche metier (Service)**
   - Regles du jeu
   - Gestion des comptes
   - Calcul des scores et sauvegardes
4. **Couche persistance (Repository)**
   - Acces aux entites en base via JPA
5. **Couche donnees (Model)**
   - Entites persistantes (`Player`, `SavedGame`)
   - Etat de partie en memoire (`GameState`)

---

## 5. Structure du projet

### 5.1 Arborescence principale

- `src/main/java/com/jeu/reflexion/config`  
  Configuration Spring MVC, JPA, transactions, initialisation web.

- `src/main/java/com/jeu/reflexion/controller`  
  Controleurs HTTP (`AuthController`, `GameController`).

- `src/main/java/com/jeu/reflexion/service`  
  Logique metier (`PlayerService`, `GameService`).

- `src/main/java/com/jeu/reflexion/repository`  
  Interfaces d'acces base (`PlayerRepository`, `SavedGameRepository`).

- `src/main/java/com/jeu/reflexion/model`  
  Objets metier et entites (`Player`, `SavedGame`, `GameState`).

- `src/main/resources/templates`  
  Vues Thymeleaf (`login`, `register`, `home`, `game`, `scores`).

- `src/main/resources/static`  
  Ressources statiques (`css`, `js`, `images`).

### 5.2 Ressources visuelles du jeu

Les cartes sont gerees par des images SVG :
- `card_1.svg` a `card_18.svg`
- `card_back.svg`

Elles sont stockees dans `src/main/resources/static/images`.

---

## 6. Description des composants backend

### 6.1 Configuration

- `WebAppInitializer` : demarrage de l'application web et initialisation du contexte Spring.
- `WebMvcConfig` : configuration MVC, resolvers de vues et mapping des ressources statiques (`/css/**`, `/js/**`, `/images/**`).
- `PersistenceConfig` : DataSource H2, EntityManagerFactory, transactions.
- `AppRootConfig` : scan des composants metier.

### 6.2 Controleurs

#### AuthController

Gere l'authentification :
- `GET /login`
- `POST /login`
- `GET /register`
- `POST /register`
- `GET /logout`
- redirection de `/` selon etat de session

#### GameController

Gere le cycle de vie de la partie :
- `GET /home`
- `GET /game/new`
- `GET /game`
- `POST /game/flip`
- `POST /game/save`
- `GET /game/load/{id}`
- `POST /game/delete/{id}`
- `GET /scores`

### 6.3 Services

#### PlayerService
- Inscription utilisateur
- Verification login/mot de passe
- Mise a jour du meilleur score
- Extraction du leaderboard

#### GameService
- Initialisation d'une partie selon niveau
- Verification des coups
- Calcul du score
- Sauvegarde/reprise (serialisation de l'etat de jeu)

### 6.4 Repositories

- `PlayerRepository` : operations CRUD et requetes sur les joueurs.
- `SavedGameRepository` : gestion des sauvegardes de parties.

---

## 7. Modele de donnees

### 7.1 Entite Player

Table `players` :
- `id`
- `username` (unique)
- `password`
- `bestScore`
- `bestLevel`

### 7.2 Entite SavedGame

Table `saved_games` :
- `id`
- `player_id` (relation ManyToOne vers `Player`)
- `level`
- `moves`
- `wrongMoves`
- `score`
- `boardState` (etat serialize)
- `savedAt`

### 7.3 Objet GameState

`GameState` n'est pas une entite JPA : c'est un objet metier serialisable qui contient l'etat courant d'une partie (cartes, paires trouvees, coups, score, etc.).  
Il est conserve en session HTTP pendant le jeu, puis serialize lors de la sauvegarde.

---

## 8. Frontend et experience utilisateur

### 8.1 Templates Thymeleaf

- `login.html` : formulaire de connexion
- `register.html` : inscription
- `home.html` : tableau de bord (lancer partie, sauvegardes, mini classement)
- `game.html` : plateau de jeu et interactions
- `scores.html` : classement global

### 8.2 Composants statiques

- `static/css/style.css` : charte graphique et mise en page
- `static/js/game.js` : gestion des interactions client (retournement, timer visuel, envoi des actions)
- `static/images/*.svg` : visuels des cartes

---

## 9. Flux fonctionnel global

1. L'utilisateur s'inscrit puis se connecte.
2. Le serveur cree une session applicative.
3. Depuis l'accueil, l'utilisateur choisit un niveau et demarre une partie.
4. A chaque action, le controleur transmet au service metier qui met a jour l'etat de jeu.
5. L'etat est renvoye vers la vue `game.html`.
6. En fin de partie, le score est compare puis potentiellement enregistre comme meilleur score.
7. L'utilisateur peut sauvegarder et reprendre une partie.
8. Les scores globaux sont consultables via la page classement.

---

## 10. Build, execution et deploiement

### 10.1 Build Maven

Commande classique :
- `mvn clean package`

Le resultat est un fichier WAR deployable sur Tomcat.

### 10.2 Script d'execution local

Le script `run-game.bat` automatise :
- l'arret de Tomcat,
- la compilation/package Maven,
- la copie du WAR,
- le redemarrage du serveur.

URL cible locale : `http://localhost:8080/login`

---

## 11. Evaluation technique

### 11.1 Points forts

- Separation nette des responsabilites (MVC + services + repositories)
- Code lisible et bien structure pour un projet pedagogique
- Fonctionnalites completes (auth, jeu, sauvegarde, classement)
- Utilisation coherente de Spring + JPA + Thymeleaf

### 11.2 Limites identifiees

- Mot de passe stocke en clair (absence de hash bcrypt/argon2)
- Pas de Spring Security (authentification artisanale)
- Peu de protection avancee (ex. CSRF)
- Etat de partie persiste via serialisation Java Base64, peu interoperable
- Absence de tests automatises detectes

### 11.3 Axes d'amelioration

- Integrer Spring Security et hash des mots de passe
- Ajouter tests unitaires et tests d'integration MVC
- Remplacer `boardState` binaire par un format JSON versionnable
- Externaliser davantage la configuration selon les environnements
- Enrichir la supervision (logs, suivi erreurs, metriques)

---

## 12. Conclusion

Le projet **Jeu Reflexion** repond au besoin d'une application web JEE complete a echelle pedagogique.  
Il combine une logique metier claire (jeu Memory) avec une architecture technique solide en couches, facilitant maintenance et evolution.

La base actuelle est saine et fonctionnelle ; les evolutions prioritaires concernent surtout la securite et la qualite logicielle (tests, robustesse, industrialisation).

# Rapport de projet - Jeu de reflexion (Memory)

## 1. Introduction

Dans le cadre du projet de developpement web JEE, notre binome a realise une application de jeu de memoire (Memory) nommee **Jeu Reflexion**.  
L'objectif principal est de proposer une experience ludique simple, avec gestion des comptes utilisateurs, suivi des scores et sauvegarde/reprise des parties.

Ce document presente :
- une description du jeu choisi,
- l'architecture detaillee de l'application developpee,
- les choix techniques,
- les forces et limites du systeme,
- des pistes d'amelioration.

---

## 2. Description du jeu choisi

Le jeu implemente est un **Memory** : le joueur doit retrouver des paires de cartes identiques en retournant deux cartes a la fois.

### 2.1 Regles principales

- Le plateau est compose de paires de cartes melangees aleatoirement.
- A chaque coup, le joueur retourne deux cartes.
- Si les cartes sont identiques, elles restent visibles.
- Sinon, elles sont remises face cachee, et le compteur d'erreurs augmente.
- La partie est terminee lorsque toutes les paires sont trouvees.

### 2.2 Niveaux de difficulte

Le jeu propose trois niveaux :
- **EASY**
- **MEDIUM**
- **HARD**

Le niveau influence notamment le nombre de cartes a retrouver, et donc la difficulte globale de la partie.

### 2.3 Fonctionnalites utilisateur

- Creation de compte (inscription)
- Connexion / deconnexion
- Lancement d'une nouvelle partie
- Sauvegarde d'une partie en cours
- Reprise d'une sauvegarde
- Consultation des meilleurs scores

---

## 3. Technologies utilisees

L'application repose sur une architecture Java web classique autour de Spring et Thymeleaf.

- **Langage** : Java 17
- **Framework web** : Spring MVC 6
- **Moteur de templates** : Thymeleaf
- **Acces donnees** : Spring Data JPA + Hibernate
- **Base de donnees** : H2 (mode fichier)
- **Build** : Maven
- **Packaging** : WAR
- **Serveur cible** : Apache Tomcat 10

Fichier de build principal : `pom.xml`

---

## 4. Architecture detaillee de l'application

L'application adopte une architecture en couches, qui separe clairement les responsabilites.

## 4.1 Vue d'ensemble des couches

1. **Couche presentation (View)**
   - Pages HTML Thymeleaf
   - CSS/JS statiques
2. **Couche controle (Controller)**
   - Reception des requetes HTTP
   - Validation de session
   - Orchestration des appels metier
3. **Couche metier (Service)**
   - Regles du jeu
   - Gestion des comptes
   - Calcul des scores et sauvegardes
4. **Couche persistance (Repository)**
   - Acces aux entites en base via JPA
5. **Couche donnees (Model)**
   - Entites persistantes (`Player`, `SavedGame`)
   - Etat de partie en memoire (`GameState`)

---

## 5. Structure du projet

### 5.1 Arborescence principale

- `src/main/java/com/jeu/reflexion/config`  
  Configuration Spring MVC, JPA, transactions, initialisation web.

- `src/main/java/com/jeu/reflexion/controller`  
  Controleurs HTTP (`AuthController`, `GameController`).

- `src/main/java/com/jeu/reflexion/service`  
  Logique metier (`PlayerService`, `GameService`).

- `src/main/java/com/jeu/reflexion/repository`  
  Interfaces d'acces base (`PlayerRepository`, `SavedGameRepository`).

- `src/main/java/com/jeu/reflexion/model`  
  Objets metier et entites (`Player`, `SavedGame`, `GameState`).

- `src/main/resources/templates`  
  Vues Thymeleaf (`login`, `register`, `home`, `game`, `scores`).

- `src/main/resources/static`  
  Ressources statiques (`css`, `js`, `images`).

### 5.2 Ressources visuelles du jeu

Les cartes sont gerees par des images SVG :
- `card_1.svg` a `card_18.svg`
- `card_back.svg`

Elles sont stockees dans `src/main/resources/static/images`.

---

## 6. Description des composants backend

### 6.1 Configuration

- `WebAppInitializer` : demarrage de l'application web et initialisation du contexte Spring.
- `WebMvcConfig` : configuration MVC, resolvers de vues et mapping des ressources statiques (`/css/**`, `/js/**`, `/images/**`).
- `PersistenceConfig` : DataSource H2, EntityManagerFactory, transactions.
- `AppRootConfig` : scan des composants metier.

### 6.2 Controleurs

#### AuthController

Gere l'authentification :
- `GET /login`
- `POST /login`
- `GET /register`
- `POST /register`
- `GET /logout`
- redirection de `/` selon etat de session

#### GameController

Gere le cycle de vie de la partie :
- `GET /home`
- `GET /game/new`
- `GET /game`
- `POST /game/flip`
- `POST /game/save`
- `GET /game/load/{id}`
- `POST /game/delete/{id}`
- `GET /scores`

### 6.3 Services

#### PlayerService
- Inscription utilisateur
- Verification login/mot de passe
- Mise a jour du meilleur score
- Extraction du leaderboard

#### GameService
- Initialisation d'une partie selon niveau
- Verification des coups
- Calcul du score
- Sauvegarde/reprise (serialisation de l'etat de jeu)

### 6.4 Repositories

- `PlayerRepository` : operations CRUD et requetes sur les joueurs.
- `SavedGameRepository` : gestion des sauvegardes de parties.

---

## 7. Modele de donnees

### 7.1 Entite Player

Table `players` :
- `id`
- `username` (unique)
- `password`
- `bestScore`
- `bestLevel`

### 7.2 Entite SavedGame

Table `saved_games` :
- `id`
- `player_id` (relation ManyToOne vers `Player`)
- `level`
- `moves`
- `wrongMoves`
- `score`
- `boardState` (etat serialize)
- `savedAt`

### 7.3 Objet GameState

`GameState` n'est pas une entite JPA : c'est un objet metier serialisable qui contient l'etat courant d'une partie (cartes, paires trouvees, coups, score, etc.).  
Il est conserve en session HTTP pendant le jeu, puis serialize lors de la sauvegarde.

---

## 8. Frontend et experience utilisateur

### 8.1 Templates Thymeleaf

- `login.html` : formulaire de connexion
- `register.html` : inscription
- `home.html` : tableau de bord (lancer partie, sauvegardes, mini classement)
- `game.html` : plateau de jeu et interactions
- `scores.html` : classement global

### 8.2 Composants statiques

- `static/css/style.css` : charte graphique et mise en page
- `static/js/game.js` : gestion des interactions client (retournement, timer visuel, envoi des actions)
- `static/images/*.svg` : visuels des cartes

---

## 9. Flux fonctionnel global

1. L'utilisateur s'inscrit puis se connecte.
2. Le serveur cree une session applicative.
3. Depuis l'accueil, l'utilisateur choisit un niveau et demarre une partie.
4. A chaque action, le controleur transmet au service metier qui met a jour l'etat de jeu.
5. L'etat est renvoye vers la vue `game.html`.
6. En fin de partie, le score est compare puis potentiellement enregistre comme meilleur score.
7. L'utilisateur peut sauvegarder et reprendre une partie.
8. Les scores globaux sont consultables via la page classement.

---

## 10. Build, execution et deploiement

### 10.1 Build Maven

Commande classique :
- `mvn clean package`

Le resultat est un fichier WAR deployable sur Tomcat.

### 10.2 Script d'execution local

Le script `run-game.bat` automatise :
- l'arret de Tomcat,
- la compilation/package Maven,
- la copie du WAR,
- le redemarrage du serveur.

URL cible locale : `http://localhost:8080/login`

---

## 11. Evaluation technique

### 11.1 Points forts

- Separation nette des responsabilites (MVC + services + repositories)
- Code lisible et bien structure pour un projet pedagogique
- Fonctionnalites completes (auth, jeu, sauvegarde, classement)
- Utilisation coherente de Spring + JPA + Thymeleaf

### 11.2 Limites identifiees

- Mot de passe stocke en clair (absence de hash bcrypt/argon2)
- Pas de Spring Security (authentification artisanale)
- Peu de protection avancee (ex. CSRF)
- Etat de partie persiste via serialisation Java Base64, peu interoperable
- Absence de tests automatises detectes

### 11.3 Axes d'amelioration

- Integrer Spring Security et hash des mots de passe
- Ajouter tests unitaires et tests d'integration MVC
- Remplacer `boardState` binaire par un format JSON versionnable
- Externaliser davantage la configuration selon les environnements
- Enrichir la supervision (logs, suivi erreurs, metriques)

---

## 12. Conclusion

Le projet **Jeu Reflexion** repond au besoin d'une application web JEE complete a echelle pedagogique.  
Il combine une logique metier claire (jeu Memory) avec une architecture technique solide en couches, facilitant maintenance et evolution.

La base actuelle est saine et fonctionnelle ; les evolutions prioritaires concernent surtout la securite et la qualite logicielle (tests, robustesse, industrialisation).

